package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.IncidentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentDao {
    @Query("SELECT * FROM incident_logs ORDER BY timestamp DESC")
    fun getAllIncidents(): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incident_logs WHERE governorate = :governorate ORDER BY timestamp DESC")
    fun getIncidentsByGovernorate(governorate: String): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incident_logs WHERE threatType = :threatType ORDER BY timestamp DESC")
    fun getIncidentsByType(threatType: String): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incident_logs WHERE id = :id LIMIT 1")
    suspend fun getIncidentById(id: Long): IncidentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(incidents: List<IncidentEntity>)

    @Query("UPDATE incident_logs SET operatorNotes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String)

    @Query("UPDATE incident_logs SET isResolved = :isResolved WHERE id = :id")
    suspend fun updateStatus(id: Long, isResolved: Boolean)

    @Query("DELETE FROM incident_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM incident_logs")
    suspend fun clearAll()
}
