package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Local cached copy of a tactical message.
 * Firestore is the shared source between devices, while Room keeps an offline copy.
 */
@Entity(
    tableName = "tactical_messages",
    indices = [
        Index(value = ["clientUuid"], unique = true),
        Index(value = ["remoteId"], unique = true)
    ]
)
data class TacticalMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clientUuid: String = UUID.randomUUID().toString(),
    val remoteId: String? = null,
    val senderId: Long,
    val senderName: String,
    val senderRank: String,
    val senderRole: String,
    val receiverId: Long? = null,
    val receiverName: String = "غرفة العمليات المشتركة (تعميم عام)",
    val channel: String = "OPS_ROOM",
    val classification: String = "سري للغاية",
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isUrgent: Boolean = false,
    val cipherCode: String = "SEC-${(1000..9999).random()}",
    val syncState: String = "PENDING" // PENDING, SYNCED, FAILED
)
