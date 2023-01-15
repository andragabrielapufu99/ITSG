package com.example.puffy.myapplication.common

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Api {
//    const val baseURL = "192.168.250.184:3000" // FSEGA
//    const val baseURL = "192.168.1.106:3000" //Cluj
    //const val baseURL = "192.168.1.102:3000" //Mioveni
    //const val baseURL = "192.168.43.94:3000" //hotspot
    const val baseURL = "6d19-188-27-128-35.eu.ngrok.io/";

    private const val URL = "http://$baseURL"

    val tokenInterceptor = TokenInterceptor()

    private val client: OkHttpClient = OkHttpClient.Builder()
        .apply { this.addInterceptor(tokenInterceptor) }
        .build()

    private var gson = GsonBuilder()
        .setLenient()
        .create()

    val retrofit = Retrofit.Builder()
        .baseUrl(URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(client)
        .build()
}