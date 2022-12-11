package com.example.puffy.myapplication.auth.data

import java.math.BigInteger
import java.util.*

data class UserAuth(
    val grant_type: String,
    val email: String,
    val password: String,
    val client_id: String,
    val client_secret: String
)