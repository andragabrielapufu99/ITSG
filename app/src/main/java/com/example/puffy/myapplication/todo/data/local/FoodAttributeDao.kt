package com.example.puffy.myapplication.todo.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.puffy.myapplication.todo.data.FoodMealAttributeLocal

@Dao
interface FoodAttributeDao {
    @Query("SELECT * from attributes")
    fun getAll(): LiveData<List<FoodMealAttributeLocal>>

    @Query("SELECT * FROM attributes WHERE meal_id=:meal_id AND food_id=:food_id ")
    fun getByMealIdFoodId(meal_id: Int, food_id: Int): LiveData<FoodMealAttributeLocal>

    @Query("SELECT * FROM attributes WHERE meal_id=:meal_id ")
    fun getByMealId(meal_id: Int): LiveData<List<FoodMealAttributeLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: FoodMealAttributeLocal)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(item: FoodMealAttributeLocal)

    @Query("DELETE FROM attributes")
    suspend fun deleteAll()
}