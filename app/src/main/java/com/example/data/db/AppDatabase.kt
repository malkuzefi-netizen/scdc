package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.IncidentDao
import com.example.data.dao.TacticalMessageDao
import com.example.data.dao.UserAccountDao
import com.example.data.entity.IncidentEntity
import com.example.data.entity.TacticalMessageEntity
import com.example.data.entity.UserAccountEntity

@Database(entities = [IncidentEntity::class, UserAccountEntity::class, TacticalMessageEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun incidentDao(): IncidentDao
    abstract fun userAccountDao(): UserAccountDao
    abstract fun tacticalMessageDao(): TacticalMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "southern_cyber_defense.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
