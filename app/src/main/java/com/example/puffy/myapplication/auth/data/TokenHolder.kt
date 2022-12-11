package com.example.puffy.myapplication.auth.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tokens")
data class TokenHolder (
   @PrimaryKey @ColumnInfo(name = "id") val id :Int,
   @ColumnInfo(name = "email") val email : String?,
   @ColumnInfo(name = "access_token") val access_token : String,
   @ColumnInfo(name = "token_type") val token_type : String,
   @ColumnInfo(name = "expires_in") val expires_in : String,
   @ColumnInfo(name = "created_at") val created_at : String?
)