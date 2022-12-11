package com.example.puffy.myapplication.todo.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.puffy.myapplication.auth.data.TokenHolder

@Dao
interface TokenDao {
    @Query("SELECT * FROM tokens LIMIT 1")
    suspend fun getToken() : TokenHolder

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(token: TokenHolder)

    @Query("DELETE FROM tokens")
    suspend fun deleteAll()
}