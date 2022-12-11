package com.example.puffy.myapplication.todo.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class MealLocal(
    @PrimaryKey @ColumnInfo(name = "id") var id: Int,
    @ColumnInfo(name = "category") var category: String,
    @ColumnInfo(name = "served_on") var served_on: String,
    @ColumnInfo(name = "created_at") var created_at: String,
    @ColumnInfo(name = "updated_at") var updated_at: String,
    @ColumnInfo(name = "calories") var calories: Float,
    @ColumnInfo(name = "pathImage") var pathImage: String
)