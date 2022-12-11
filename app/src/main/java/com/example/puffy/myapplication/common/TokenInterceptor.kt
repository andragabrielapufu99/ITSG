package com.example.puffy.myapplication.common

import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor constructor() : Interceptor {
    var token : String? = null
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val originalUrl = original.url

        if(token == null){
            println("original request")
            return chain.proceed(original)
        }
        println("token $token")
        val requestBuilder = original.newBuilder()
            .addHeader("Authorization","Bearer $token")
            .url(originalUrl)
        val request = requestBuilder.build()
        println("request $request")
        println("headers ${request.headers.get("Authorization")}")
        return chain.proceed(request)
    }
}