package com.example.puffy.myapplication.todo.meal

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.puffy.myapplication.auth.data.AuthRepository
import com.example.puffy.myapplication.common.Api
import com.example.puffy.myapplication.common.MyResult
import com.example.puffy.myapplication.todo.data.Meal
import com.example.puffy.myapplication.todo.data.MealRepository
import com.example.puffy.myapplication.todo.data.local.TodoDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.*

class MealEditViewModel(application: Application) : AndroidViewModel(application) {

    private val mutableCompleted = MutableLiveData<Boolean>().apply { value = false }
    private val mutableFetching = MutableLiveData<Boolean>().apply { value = false }
    private val mutableException = MutableLiveData<Exception>().apply { value = null }
    private val tagName: String = "MealEditViewModel"
    private val entity: String = "Meal";
    val completed : LiveData<Boolean> = mutableCompleted
    val fetching : LiveData<Boolean> = mutableFetching
    val exception : LiveData<Exception> = mutableException

    val tokenDao = TodoDatabase.getDatabase(application,viewModelScope).tokenDao()
    private val applicationContext = application.applicationContext
    init{
        val mealDao = TodoDatabase.getDatabase(application, viewModelScope).mealDao()
        val attributeDao = TodoDatabase.getDatabase(application, viewModelScope).foodAttributeDao()
    }

    fun getById(id: Int) : LiveData<Meal> {
        Log.v(tagName, "get${entity}ById")
        return MealRepository.getOne(id)
    }

    fun update(item: Meal) {
        viewModelScope.launch {
            Log.v(tagName, "update${entity}...")
            mutableFetching.value = true
            mutableException.value = null
            val result: MyResult<Meal>
            if (item.id != -1) {
                result = MealRepository.update(item.id, item)
                when (result) {
                    is MyResult.Success -> {
                        Log.d(tagName, "update${entity} succeeded")
                    }
                    is MyResult.Error -> {
                        Log.w(tagName, "update${entity} failed", result.exception)
                        mutableException.value = result.exception
                    }
                }
            }

            mutableCompleted.value = true
            mutableFetching.value = false

        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun add(item: Meal, image: Bitmap) {
        viewModelScope.launch {
            Log.v(tagName, "add${entity}...")
            mutableFetching.value = true
            mutableException.value = null
            val result: MyResult<Meal>
            if (item.id == -1) {
//                var file: File? = null;
//                if(item.pathImage != ""){
//                    file = File(item.pathImage)
//                    var uri : Uri?
//                    file.also {
//                        uri = it?.let { it1 ->
//                            applicationContext?.let { it2 ->
//                                FileProvider.getUriForFile(
//                                    it2,
//                                    "com.example.puffy.myapplication.fileprovider",
//                                    it1
//                                )
//                            }
//                        }
//                    }
//                }
                result = MealRepository.add(item, image, applicationContext)
                when (result) {
                    is MyResult.Success -> {
                        Log.d(tagName, "add${entity} succeeded")
//                        when (val result2 = MealRepository.refresh()) {
//                            is MyResult.Success -> {
//                                Log.d(tagName, "Refresh succeeded")
//                            }
//                            is MyResult.Error -> {
//                                Log.w(tagName, "Refresh failed", result2.exception);
//                                mutableException.value = result2.exception
//                            }
//                        }
//                        mutableCompleted.value = true
//                        mutableFetching.value = false
                    }
                    is MyResult.Error -> {
                        Log.w(tagName, "add${entity} failed", result.exception)
                        mutableException.value = result.exception
                    }
                }
            }

            mutableCompleted.value = true
            mutableFetching.value = false

        }
    }

    fun logout(){
        runBlocking {
            tokenDao.deleteAll()
            MealRepository.deleteAllLocal()
            AuthRepository.token = null
            Api.tokenInterceptor.token = null
        }

    }
}