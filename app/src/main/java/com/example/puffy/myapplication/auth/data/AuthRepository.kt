package com.example.puffy.myapplication.auth.data

import com.example.puffy.myapplication.auth.data.remote.AuthApi
import com.example.puffy.myapplication.common.Api
import com.example.puffy.myapplication.common.MyResult
import retrofit2.HttpException
import java.lang.Exception

object AuthRepository {
    var token : String? = null
    var grant_type: String = "password";
    var client_id: String = "vuSx6z9e0DOkg5UblX48gdXTKqCbf0OmPVzdPAGlnZ8";
    var client_secret: String = "njqBh_jJfiEVAqC63p39IWBFpVr2E1BfB9M6obDqHBo";

    val isAuthenticated: Boolean
        get() = this.token != null

    init {
        this.token = Api.tokenInterceptor.token
        println(this.token)
    }

    suspend fun login(email: String, password: String): MyResult<TokenHolder> {
        val user = UserAuth(this.grant_type, email, password, this.client_id, this.client_secret)
        try {
            val result = AuthApi.login(user)
            this.token = result.access_token
            Api.tokenInterceptor.token = result.access_token
            return MyResult.Success(result)
        } catch (ex: Exception) {
            if (ex is HttpException) {
                val message: String? = ex.response()?.errorBody()?.string()
                val e = Exception(message)
                return MyResult.Error(e)
            }
            return MyResult.Error(ex)
        }
    }
}