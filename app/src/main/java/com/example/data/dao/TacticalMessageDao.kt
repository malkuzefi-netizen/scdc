package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.TacticalMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TacticalMessageDao {

    @Query("SELECT * FROM tactical_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<TacticalMessageEntity>>

    @Query("SELECT * FROM tactical_messages WHERE channel = :channel ORDER BY timestamp ASC")
    fun getMessagesByChannel(channel: String): Flow<List<TacticalMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessage(message: TacticalMessageEntity): Long

    @Update
    suspend fun updateMessage(message: TacticalMessageEntity)

    @Query("SELECT * FROM tactical_messages WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): TacticalMessageEntity?

    @Query("SELECT * FROM tactical_messages WHERE clientUuid = :clientUuid LIMIT 1")
    suspend fun findByClientUuid(clientUuid: String): TacticalMessageEntity?

    @Query("SELECT * FROM tactical_messages WHERE remoteId = :remoteId LIMIT 1")
    suspend fun findByRemoteId(remoteId: String): TacticalMessageEntity?

    @Query("SELECT * FROM tactical_messages WHERE syncState != 'SYNCED' ORDER BY timestamp ASC")
    suspend fun getPendingMessages(): List<TacticalMessageEntity>

    @Query("DELETE FROM tactical_messages WHERE id = :id")
    suspend fun deleteMessage(id: Long)

    @Query("DELETE FROM tactical_messages WHERE remoteId = :remoteId")
    suspend fun deleteByRemoteId(remoteId: String)

    @Query("DELETE FROM tactical_messages")
    suspend fun clearAllMessages()

    @Query("SELECT COUNT(*) FROM tactical_messages")
    suspend fun getMessageCount(): Int
}
