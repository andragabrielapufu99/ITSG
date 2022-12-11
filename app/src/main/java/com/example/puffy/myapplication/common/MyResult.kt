package com.example.puffy.myapplication.common

sealed class MyResult<out R> {

    data class Success<out T>(val data: T) : MyResult<T>()

    data class Error(val exception: Exception) : MyResult<Nothing>()

    object Loading : MyResult<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
            Loading -> "Loading"
        }
    }
}

val MyResult<*>.succeeded
    get() = this is MyResult.Success && data != null

val MyResult<*>.failed
    get() = this is MyResult.Error

val MyResult<*>.loading
    get() = this is MyResult.Loading