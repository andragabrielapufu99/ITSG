package com.example.puffy.myapplication.todo.data.remote

import android.graphics.Bitmap
import com.example.puffy.myapplication.common.Api
import com.example.puffy.myapplication.todo.data.Meal
import retrofit2.http.*

object MealApi {
    interface Service{

        @GET("/api/meals")
        @Headers("Accept: application/json")
        suspend fun getAll() : List<Meal>

        @Multipart
        @POST("/api/meals")
//        @Headers("Content-Type: multipart/form-data", "Accept: application/json")
//        suspend fun add(@Body meal : Meal) : Meal
        suspend fun add(@Part("meal") meal: Meal, @Part("file") file: Bitmap) : Meal

        @PUT("/api/meals/{id}")
        @Headers("Content-Type: application/json", "Accept: application/json")
        suspend fun update(@Path("id") id : Int, @Body meal : Meal) : Meal
    }

    val service : Service = Api.retrofit.create(Service::class.java)
}