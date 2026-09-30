package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incident_logs")
data class IncidentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val targetCode: String,
    val threatType: String,
    val threatLevel: String,
    val designation: String,
    val governorate: String,
    val sector: String,
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Int,
    val speedKmh: Int,
    val frequencyBand: String,
    val timestamp: Long = System.currentTimeMillis(),
    val detectionSensor: String,
    val operatorNotes: String = "",
    val isResolved: Boolean = false,
    val recordedBy: String = "مديرية الأمن السيبراني الجنوبي"
)
