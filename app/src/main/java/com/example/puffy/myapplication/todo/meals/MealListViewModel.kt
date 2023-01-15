package com.example.puffy.myapplication.todo.meals

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.puffy.myapplication.auth.data.AuthRepository
import com.example.puffy.myapplication.common.Api
import com.example.puffy.myapplication.common.EventType
import com.example.puffy.myapplication.common.MyResult
import com.example.puffy.myapplication.common.RemoteDataSource
import com.example.puffy.myapplication.todo.data.*
import com.example.puffy.myapplication.todo.data.local.TodoDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

@RequiresApi(Build.VERSION_CODES.O)
class MealListViewModel(application: Application) : AndroidViewModel(application) {
    private val mutableLoading = MutableLiveData<Boolean>().apply { value = false }
    private val mutableException = MutableLiveData<Exception>().apply { value = null }
    private val mutableNetworkStatus = MutableLiveData<Boolean>()
    private val tagName: String = "MealListViewModel"
    private val tokenDao = TodoDatabase.getDatabase(application,viewModelScope).tokenDao()

    //public
    var items: LiveData<List<Meal>>
    val loading : LiveData<Boolean> = mutableLoading
    val loadingError : LiveData<Exception> = mutableException
    val networkStatus : LiveData<Boolean> = mutableNetworkStatus

    init {
        Log.v(tagName,"init")
        val mealDao = TodoDatabase.getDatabase(application, viewModelScope).mealDao()
        Log.v(tagName,"meal dao")
        val attributeDao = TodoDatabase.getDatabase(application, viewModelScope).foodAttributeDao()
        Log.v(tagName,"attribute dao")
        MealRepository.setDao(mealDao, attributeDao)
        Log.v(tagName,"set dao")
        MealRepository.setItems()
        items = MealRepository.items
        Log.v(tagName,"init items")
        if (networkStatus.value == true){
            CoroutineScope(Dispatchers.Main).launch { ws() }
            MealRepository.setNetworkStatus(true)
        }else{
            MealRepository.setNetworkStatus(false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setNetworkStatus(status : Boolean){
        mutableNetworkStatus.postValue(status)
        MealRepository.setNetworkStatus(status)
        if (networkStatus.value == true){
            CoroutineScope(Dispatchers.Main).launch { ws() }
        }
    }

    fun refreshLocal() : List<Meal>? {
        runBlocking { MealRepository.refreshLocal2() }
        return MealRepository.items.value
    }

    fun refresh() {
        viewModelScope.launch {
            Log.v(tagName, "Refresh");
            mutableLoading.value = true
            mutableException.value = null
            when (val result = MealRepository.refresh()) {
                is MyResult.Success -> {
                    Log.d(tagName, "Refresh succeeded")
                    println("items ${items.value}")
                }
                is MyResult.Error -> {
                    Log.w(tagName, "Refresh failed", result.exception);
                    mutableException.value = result.exception
                }
            }
            mutableLoading.value = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun ws(){
        while (true){
            val event = RemoteDataSource.eventChannel.receive()
            val jsonObject = JSONObject(event)
            val eventType = jsonObject.get("event")
            val payload = jsonObject.get("payload")
            val itemObj = JSONObject(payload.toString())
            var item : Meal? = null
            val attributes: ArrayList<FoodMealAttribute> = emptyList<FoodMealAttribute>() as ArrayList<FoodMealAttribute>
            val jsonAttributes = itemObj.getJSONArray("foods")
            item = Meal(
                itemObj.getInt("id"),
                itemObj.getString("category"),
                itemObj.getString("served_on"),
                itemObj.getString("created_at"),
                itemObj.getString("updated_at"),
                attributes,
                itemObj.getString("calories").toFloat(),
                itemObj.getString("pathImage")
            )

            if(eventType == EventType.CREATED){
                MealRepository.addLocal(item)
            }
            else if(eventType == EventType.UPDATED){
                MealRepository.updateLocal(item)
            }
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