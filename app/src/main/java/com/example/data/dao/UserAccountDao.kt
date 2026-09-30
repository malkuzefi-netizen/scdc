package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.UserAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts ORDER BY isMasterAdmin DESC, createdAt ASC")
    fun getAllAccounts(): Flow<List<UserAccountEntity>>

    @Query("SELECT * FROM user_accounts WHERE username = :usernameOrName OR fullName = :usernameOrName LIMIT 1")
    suspend fun findByUsernameOrName(usernameOrName: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Long): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE (username = :usernameOrName OR fullName = :usernameOrName) AND password = :password LIMIT 1")
    suspend fun authenticate(usernameOrName: String, password: String): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: UserAccountEntity): Long

    @Update
    suspend fun updateAccount(account: UserAccountEntity)

    @Query("DELETE FROM user_accounts WHERE id = :id AND isMasterAdmin = 0")
    suspend fun deleteAccount(id: Long)

    @Query("SELECT COUNT(*) FROM user_accounts")
    suspend fun getAccountCount(): Int

    @Query("DELETE FROM user_accounts WHERE isMasterAdmin = 0")
    suspend fun clearNonMasterAccounts()
}
