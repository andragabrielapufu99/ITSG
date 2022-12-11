package com.example.puffy.myapplication.auth.data

import java.math.BigInteger
import java.util.Date

data class User(
//    val username : String,
//    val password : String,
    val id: Int,
    val email: String,
    val encrypted_password: String,
    val remember_created_at: Date,
    val created_at: Date,
    val updated_at: Date,
    val first_name: String,
    val last_name: String,
    val gender: GenderType,
    val activity_type: ActivityType,
    val height: Float,
    val weight: Float,
    val date_of_birth: Date,
    val activity_metabolic_rate: Float
)