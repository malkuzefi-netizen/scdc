package com.example.data.repository

import android.content.Context
import com.example.data.dao.TacticalMessageDao
import com.example.data.entity.TacticalMessageEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Shared real-time messaging layer.
 * Firestore synchronizes messages between devices; Room remains the offline cache.
 */
class FirebaseMessagingRepository(
    context: Context,
    private val dao: TacticalMessageDao
) {
    private val appReady = FirebaseApp.initializeApp(context) != null
    private val auth: FirebaseAuth? = if (appReady) FirebaseAuth.getInstance() else null
    private val firestore: FirebaseFirestore? = if (appReady) FirebaseFirestore.getInstance() else null
    private var listener: ListenerRegistration? = null

    val isConfigured: Boolean get() = appReady

    suspend fun ensureSignedIn(): Boolean {
        if (!appReady) return false
        val firebaseAuth = auth ?: return false
        if (firebaseAuth.currentUser != null) return true
        return suspendCancellableCoroutine { cont ->
            firebaseAuth.signInAnonymously()
                .addOnCompleteListener { task -> if (cont.isActive) cont.resume(task.isSuccessful) }
        }
    }

    suspend fun startRealtimeSync(scope: CoroutineScope, onError: (String) -> Unit = {}): Boolean {
        if (!ensureSignedIn()) {
            onError("تعذر تسجيل الاتصال بخدمة Firebase")
            return false
        }
        val db = firestore ?: return false
        listener?.remove()
        listener = db.collection(COLLECTION)
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.localizedMessage ?: "خطأ في مزامنة الرسائل")
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                scope.launch(Dispatchers.IO) {
                    snapshot.documentChanges.forEach { change ->
                        val doc = change.document
                        val remoteId = doc.id
                        if (change.type.name == "REMOVED") {
                            dao.deleteByRemoteId(remoteId)
                            return@forEach
                        }

                        val clientUuid = doc.getString("clientUuid") ?: remoteId
                        val existing = dao.findByClientUuid(clientUuid) ?: dao.findByRemoteId(remoteId)
                        val remote = TacticalMessageEntity(
                            id = existing?.id ?: 0L,
                            clientUuid = clientUuid,
                            remoteId = remoteId,
                            senderId = doc.getLong("senderId") ?: 0L,
                            senderName = doc.getString("senderName") ?: "مستخدم",
                            senderRank = doc.getString("senderRank") ?: "",
                            senderRole = doc.getString("senderRole") ?: "",
                            receiverId = doc.getLong("receiverId"),
                            receiverName = doc.getString("receiverName") ?: "غرفة العمليات المشتركة (تعميم عام)",
                            channel = doc.getString("channel") ?: "OPS_ROOM",
                            classification = doc.getString("classification") ?: "سري للغاية",
                            content = doc.getString("content") ?: "",
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                            isUrgent = doc.getBoolean("isUrgent") ?: false,
                            cipherCode = doc.getString("cipherCode") ?: "SEC-0000",
                            syncState = "SYNCED"
                        )
                        if (existing == null) dao.insertMessage(remote) else dao.updateMessage(remote)
                    }
                }
            }

        retryPendingMessages()
        return true
    }

    suspend fun send(message: TacticalMessageEntity): Result<String> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firebase غير مهيأ"))
        if (!ensureSignedIn()) return@withContext Result.failure(IllegalStateException("تعذر تسجيل Firebase"))

        val localId = if (message.id == 0L) dao.insertMessage(message.copy(syncState = "PENDING")) else message.id
        val local = message.copy(id = if (localId > 0) localId else message.id, syncState = "PENDING")
        val payload = local.toFirestoreMap()

        val result = suspendCancellableCoroutine<Result<String>> { cont ->
            db.collection(COLLECTION).document(local.clientUuid).set(payload)
                .addOnSuccessListener { if (cont.isActive) cont.resume(Result.success(local.clientUuid)) }
                .addOnFailureListener { e -> if (cont.isActive) cont.resume(Result.failure(e)) }
        }
        result.onSuccess { remoteId ->
            val current = dao.findByClientUuid(local.clientUuid)
            if (current != null) dao.updateMessage(current.copy(remoteId = remoteId, syncState = "SYNCED"))
        }.onFailure {
            val current = dao.findByClientUuid(local.clientUuid)
            if (current != null) dao.updateMessage(current.copy(syncState = "FAILED"))
        }
        result
    }

    suspend fun retryPendingMessages() {
        val pending = dao.getPendingMessages()
        pending.forEach { send(it) }
    }

    suspend fun delete(message: TacticalMessageEntity): Result<Unit> = withContext(Dispatchers.IO) {
        val remoteId = message.remoteId ?: message.clientUuid
        val db = firestore
        if (db == null || !ensureSignedIn()) {
            dao.deleteMessage(message.id)
            return@withContext Result.failure(IllegalStateException("Firebase غير متاح"))
        }
        val result = suspendCancellableCoroutine<Result<Unit>> { cont ->
            db.collection(COLLECTION).document(remoteId).delete()
                .addOnSuccessListener { if (cont.isActive) cont.resume(Result.success(Unit)) }
                .addOnFailureListener { e -> if (cont.isActive) cont.resume(Result.failure(e)) }
        }
        if (result.isSuccess) dao.deleteMessage(message.id)
        result
    }

    suspend fun clearAll(): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firebase غير مهيأ"))
        if (!ensureSignedIn()) return@withContext Result.failure(IllegalStateException("تعذر تسجيل Firebase"))

        val snapshotResult = suspendCancellableCoroutine<Result<com.google.firebase.firestore.QuerySnapshot>> { cont ->
            db.collection(COLLECTION).get()
                .addOnSuccessListener { if (cont.isActive) cont.resume(Result.success(it)) }
                .addOnFailureListener { e -> if (cont.isActive) cont.resume(Result.failure(e)) }
        }
        val snapshot = snapshotResult.getOrElse { return@withContext Result.failure(it) }
        snapshot.documents.chunked(450).forEach { chunk ->
            val batch = db.batch()
            chunk.forEach { batch.delete(it.reference) }
            val batchResult = suspendCancellableCoroutine<Result<Unit>> { cont ->
                batch.commit()
                    .addOnSuccessListener { if (cont.isActive) cont.resume(Result.success(Unit)) }
                    .addOnFailureListener { e -> if (cont.isActive) cont.resume(Result.failure(e)) }
            }
            if (batchResult.isFailure) return@withContext batchResult
        }
        dao.clearAllMessages()
        Result.success(Unit)
    }

    fun stop() {
        listener?.remove()
        listener = null
    }

    private fun TacticalMessageEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "clientUuid" to clientUuid,
        "senderId" to senderId,
        "senderName" to senderName,
        "senderRank" to senderRank,
        "senderRole" to senderRole,
        "receiverId" to receiverId,
        "receiverName" to receiverName,
        "channel" to channel,
        "classification" to classification,
        "content" to content,
        "timestamp" to timestamp,
        "isUrgent" to isUrgent,
        "cipherCode" to cipherCode
    )

    companion object {
        private const val COLLECTION = "tactical_messages"
    }
}
