package com.example.puffy.myapplication.todo.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.puffy.myapplication.todo.data.MealLocal

@Dao
interface MealDao {
    @Query("SELECT * from meals")
    fun getAll(): LiveData<List<MealLocal>>

    @Query("SELECT * FROM meals WHERE id=:id ")
    fun getById(id: Int): LiveData<MealLocal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meal: MealLocal)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(meal: MealLocal)

    @Query("DELETE FROM meals")
    suspend fun deleteAll()
}