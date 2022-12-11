package com.example.puffy.myapplication.auth.login

import android.app.Application
import androidx.lifecycle.*
import com.example.puffy.myapplication.auth.data.AuthRepository
import com.example.puffy.myapplication.auth.data.TokenHolder
import com.example.puffy.myapplication.common.Api
import com.example.puffy.myapplication.common.MyResult
import com.example.puffy.myapplication.todo.data.local.TodoDatabase
import com.example.puffy.myapplication.todo.data.local.TokenDao
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application){
    private val mutableLoginError = MutableLiveData<Exception>()
    val loginError : LiveData<Exception> = mutableLoginError

    private val mutableLoginResult = MutableLiveData<TokenHolder>()
    val loginResult : LiveData<TokenHolder> = mutableLoginResult

    val tokenDao : TokenDao = TodoDatabase.getDatabase(application,viewModelScope).tokenDao()

    fun checkToken(){
        viewModelScope.launch {
            val tokenHolder = tokenDao.getToken()
            if(tokenHolder != null){
                Api.tokenInterceptor.token = tokenHolder.access_token
                AuthRepository.token = tokenHolder.access_token
                mutableLoginResult.value = tokenHolder
            }
        }
    }
    fun login(email : String,password : String){
//        TODO: encrypt password
        val encrypted_password = password
        viewModelScope.launch {
            val result = AuthRepository.login(email, encrypted_password)
            if(result is MyResult.Success){
                tokenDao.insert(result.data)
                mutableLoginResult.value = result.data
            }else if(result is MyResult.Error){
                mutableLoginError.value = result.exception
            }

        }
    }

    fun loginDataChanged(email : String, password : String){
        var errors : String = ""
        val blacklist : CharSequence = "/?,|:"
        var str = ""
        blacklist.forEach { character -> str.plus(character).plus(" ") }
        str.plus("\\")
        if(email.isEmpty()){
            errors += "Email field cannot be empty!"
        }else if(email.contains(blacklist) || email.contains("\\")){
            errors += "Email field cannot contains the next characters $str !"
        }
        else if(password.isEmpty()){
            errors += "Password field cannot be empty!"
        }else if(password.contains(blacklist) || password.contains("\\")){
            errors += "Password field cannot contains the next characters $str !"
        }
        if(errors.isNotEmpty()){
            mutableLoginError.value = Exception(errors)
        }else{
            mutableLoginError.value = null
        }
    }
}