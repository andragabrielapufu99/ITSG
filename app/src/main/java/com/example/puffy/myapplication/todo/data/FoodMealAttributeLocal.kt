package com.example.puffy.myapplication.todo.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attributes")
data class FoodMealAttributeLocal(
    @PrimaryKey @ColumnInfo(name = "id") var id: Int,
    @ColumnInfo(name = "meal_id") var meal_id: Int,
    @ColumnInfo(name = "food_id") var food_id : Int,
    @ColumnInfo(name = "name") var name : String,
    @ColumnInfo(name = "calories") var calories : Float,
    @ColumnInfo(name = "fat") var fat : Float,
    @ColumnInfo(name = "protein") var protein : Float,
    @ColumnInfo(name = "carbs") var carbs : Float,
    @ColumnInfo(name = "serving_size") var serving_size : Float
)