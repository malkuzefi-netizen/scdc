package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_accounts",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val fullName: String,
    val title: String,
    val password: String,
    val militaryRank: String,
    val role: String, // "COMMANDER", "OFFICER", "SOLDIER"
    val assignedSector: String,
    val isMasterAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
