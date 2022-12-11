package com.example.puffy.myapplication.auth.data.remote

import com.example.puffy.myapplication.auth.data.TokenHolder
import com.example.puffy.myapplication.auth.data.User
import com.example.puffy.myapplication.auth.data.UserAuth
import com.example.puffy.myapplication.common.Api
import com.example.puffy.myapplication.common.MyResult
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.lang.Exception

object AuthApi {
    interface Service{
        @POST("/api/oauth/token")
        @Headers("Content-Type: application/json","Accept: application/json")
        suspend fun login(@Body user : UserAuth) : TokenHolder
    }

    private val service : Service =  Api.retrofit.create(Service::class.java)

    suspend fun login(user : UserAuth) : TokenHolder {
        return service.login(user)
    }
}