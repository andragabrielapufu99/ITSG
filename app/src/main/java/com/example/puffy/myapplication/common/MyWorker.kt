package com.example.puffy.myapplication.common

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.puffy.myapplication.todo.data.FoodMealAttribute
import com.example.puffy.myapplication.todo.data.Meal
import com.example.puffy.myapplication.todo.data.MealRepository
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

class MyWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams){
    @RequiresApi(Build.VERSION_CODES.O)
    override fun doWork(): Result {
        val jsonObj = JSONObject(inputData.getString("data"))
        val eventType = jsonObj.getString("eventType")
        val itemObj = jsonObj.getJSONObject("item")
        var item : Meal? = null
        item = Meal(
            itemObj.getInt("id"),
            itemObj.getString("category"),
            itemObj.getString("served_on"),
            itemObj.getString("created_at"),
            itemObj.getString("updated_at"),
            emptyList<FoodMealAttribute>() as ArrayList<FoodMealAttribute>,
            itemObj.getString("calories").toFloat(),
            itemObj.getString("pathImage")
        )
//        if(itemObj.has("pathImage")){
//            if(itemObj.has("latitude") && itemObj.has("longitude")){
//                item = Item(
//                    itemObj.getInt("id"),
//                    itemObj.getString("title"),
//                    itemObj.getString("artist"),
//                    itemObj.getInt("year"),
//                    itemObj.getString("genre"),
//                    itemObj.getString("userId"),
//                    itemObj.getString("pathImage"),
//                    itemObj.getDouble("latitude"),
//                    itemObj.getDouble("longitude"))
//            }else{
//                item = Item(
//                    itemObj.getInt("id"),
//                    itemObj.getString("title"),
//                    itemObj.getString("artist"),
//                    itemObj.getInt("year"),
//                    itemObj.getString("genre"),
//                    itemObj.getString("userId"),
//                    itemObj.getString("pathImage"),
//                    null,
//                    null)
//            }
//        }else{
//            if(itemObj.has("latitude") && itemObj.has("longitude")){
//                item = Item(
//                    itemObj.getInt("id"),
//                    itemObj.getString("title"),
//                    itemObj.getString("artist"),
//                    itemObj.getInt("year"),
//                    itemObj.getString("genre"),
//                    itemObj.getString("userId"),
//                    null,
//                    itemObj.getDouble("latitude"),
//                    itemObj.getDouble("longitude"))
//            }else{
//                item = Item(
//                    itemObj.getInt("id"),
//                    itemObj.getString("title"),
//                    itemObj.getString("artist"),
//                    itemObj.getInt("year"),
//                    itemObj.getString("genre"),
//                    itemObj.getString("userId"),
//                    null,
//                    null,
//                    null)
//            }
//        }
        println("Woker : eventType $eventType")
        if(eventType == EventType.CREATED.toString()){
            runBlocking {
                val result: MyResult<Meal>
//                result = MealRepository.add(item, null)
//                when (result) {
//                    is MyResult.Success -> {
//                        Log.d("MyWorker", "createdItem succeeded")
//                        MealRepository.removeMealListLocal(item, EventType.CREATED)
//                    }
//                    is MyResult.Error -> {
//                        return@runBlocking Result.failure()
//                    }
//                    else -> {}
//                }
            }
        }else if(eventType == EventType.UPDATED.toString()){
            runBlocking {
                val result: MyResult<Meal>
                result = MealRepository.update(item.id, item)
                when (result) {
                    is MyResult.Success -> {
                        Log.d("MyWorker", "updateItem succeeded")
                        MealRepository.removeMealListLocal(item, EventType.UPDATED)
                    }
                    is MyResult.Error -> {
                        return@runBlocking Result.failure()
                    }
                    else -> {}
                }
            }
        }
        return Result.success()
    }
}