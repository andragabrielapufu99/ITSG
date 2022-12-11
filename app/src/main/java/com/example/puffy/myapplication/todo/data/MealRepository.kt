package com.example.puffy.myapplication.todo.data

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.puffy.myapplication.common.Api
import com.example.puffy.myapplication.common.EventType
import com.example.puffy.myapplication.common.MyResult
import com.example.puffy.myapplication.todo.data.local.FoodAttributeDao
import com.example.puffy.myapplication.todo.data.local.MealDao
import com.example.puffy.myapplication.todo.data.remote.MealApi
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception

object MealRepository {
    private lateinit var mealDao: MealDao
    private lateinit var attributeDao: FoodAttributeDao
    lateinit var mealsLocal : LiveData<List<MealLocal>>
    lateinit var attributesLocal : LiveData<List<FoodMealAttributeLocal>>
//    lateinit var items : MutableLiveData<List<Meal>>
    lateinit var items : MutableLiveData<List<Meal>> // TODO: here is the problem

    var addedLocal : MutableList<Meal> = ArrayList()
    var updatedLocal : MutableList<Meal> = ArrayList()
    var needWorkers : Boolean = addedLocal.size > 0 || updatedLocal.size > 0
    private var networkStatus : Boolean = false

    fun setItems() {
        println(this.mealsLocal.value)
        val meals = this.convertToMealList(this.mealsLocal.value)
        println(meals)
        items = MutableLiveData<List<Meal>>().apply { value = meals }
        println("items repository ${items.value}")
//        items.apply { value = meals }
//        items.value = meals
    }

    fun setDao(mealDao : MealDao, attributeDao: FoodAttributeDao){
        this.mealDao = mealDao
        this.attributeDao = attributeDao
        this.mealsLocal = this.mealDao.getAll()
        this.attributesLocal = this.attributeDao.getAll()
        println("before set items")
        this.setItems()
        println("after set items")
    }

    fun convertToMealLocal(meal: Meal): MealLocal {
        var created_at: String = ""
        var updated_at: String = ""
        var pathImage: String = ""
        if(meal.created_at != null) created_at = meal.created_at!!
        if(meal.updated_at != null) updated_at = meal.updated_at!!
        if(meal.pathImage != null) pathImage = meal.pathImage!!
        return MealLocal(
            meal.id,
            meal.category,
            meal.served_on,
            created_at,
            updated_at,
            meal.calories,
            pathImage
        )
    }

    fun convertToFoodAttributeLocal(meal_id: Int, attribute: FoodMealAttribute): FoodMealAttributeLocal {
        var serving_size: Float = 0F;
        if(attribute.serving_size != null) serving_size = attribute.serving_size!!
        return FoodMealAttributeLocal(
            attribute.id,
            meal_id,
            attribute.food_id,
            attribute.name,
            attribute.calories,
            attribute.fat,
            attribute.protein,
            attribute.carbs,
            serving_size
        )
    }

    fun convertToFoodAttributeLocalList(meal_id: Int, attributes: ArrayList<FoodMealAttribute>): ArrayList<FoodMealAttributeLocal> {
        val result: ArrayList<FoodMealAttributeLocal> = arrayListOf<FoodMealAttributeLocal>()
        attributes.forEach { attribute ->
            result.add(this.convertToFoodAttributeLocal(meal_id, attribute))
        }
        return result
    }

    fun convertToMeal(meal: MealLocal): Meal {
        val attributes = this.attributeDao.getByMealId(meal.id)
        val attributesConverted = this.convertFromAttributesLocal(attributes.value)
        return Meal(
            meal.id,
            meal.category,
            meal.served_on,
            meal.created_at,
            meal.updated_at,
            attributesConverted,
            meal.calories,
            meal.pathImage
        )
    }

    fun convertToMealList(meals: List<MealLocal>?): ArrayList<Meal> {
        val result: ArrayList<Meal> = arrayListOf()
        println(result)
        if(meals == null) return result
        meals.forEach { meal -> result.add(this.convertToMeal(meal)) }
        return result
    }

    fun convertFromAttributesLocal(attributes: List<FoodMealAttributeLocal>?): ArrayList<FoodMealAttribute> {
        val result: ArrayList<FoodMealAttribute> = arrayListOf<FoodMealAttribute>()
        if(attributes == null) return result
        attributes.forEach { attributeLocal ->
            val attribute = FoodMealAttribute(
                attributeLocal.id,
                attributeLocal.food_id,
                attributeLocal.name,
                attributeLocal.calories,
                attributeLocal.fat,
                attributeLocal.protein,
                attributeLocal.carbs,
                attributeLocal.serving_size
            )
            result.add(attribute)
        }
        return result
    }

    fun setNetworkStatus(status : Boolean){
        networkStatus = status
    }

    fun getNetworkStatus() : Boolean{
        return networkStatus
    }

    suspend fun refresh() : MyResult<Boolean> {
        try{
            println("meals")
            val items = MealApi.service.getAll() //date de pe server
//            println(items)
            this.refreshLocal(items)
//            for (item in items) {
//                val mealLocal = this.convertToMealLocal(item)
//                this.mealDao.insert(mealLocal)
//                val attributesLocal = this.convertToFoodAttributeLocalList(item.id, item.foods)
//                attributesLocal.forEach { attribute -> this.attributeDao.insert(attribute) }
//            }
//            this.setItems()
            return MyResult.Success(true)
        }catch (ex : Exception){
            if(ex is HttpException){
                val message : String? = ex.response()?.errorBody()?.string()
                println("code")
                ex.response()?.let { println(it.code()) }
                val e = Exception(message)
                return MyResult.Error(e)
            }
            return MyResult.Error(ex)
        }
    }

    fun getOne(id : Int) : LiveData<Meal> {
        var meal: Meal? = null
        this.items.value?.forEach {
            if(it.id == id){
                meal = it
            }
        }
//        val mealLocal: LiveData<MealLocal> = this.mealDao.getById(id);
//        val meal: Meal = this.convertToMeal(mealLocal.value!!)
        return MutableLiveData<Meal>().apply { value = meal}
    }

    suspend fun add(item: Meal, file: Bitmap, context: Context) : MyResult<Meal> {
        try{
            if(networkStatus){
//                val result = MealApi.service.add(item, file)
                if(file != null){
                    this.PostImage(item, file, context);
                }
                return MyResult.Success(item)
            }
            addLocal(item)
            addedLocal.add(item)
            return MyResult.Success(item)
        }catch(ex : Exception){
            if(ex is HttpException){
                var errCode: Int? = ex.response()?.code()
                if(errCode == 409){
                    return MyResult.Error(ex)
                }
                val message : String? = ex.response()?.errorBody()?.string()
                val e = Exception(message)
                return MyResult.Error(e)
            }
            return MyResult.Error(ex)
        }
    }

    suspend fun addLocal(meal : Meal) {
        val mealLocal = this.convertToMealLocal(meal)
        this.mealDao.insert(mealLocal)
        val attributesLocal = this.convertToFoodAttributeLocalList(meal.id, meal.foods)
        attributesLocal.forEach { attribute -> this.attributeDao.insert(attribute)
        }
        needWorkers = true
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun removeMealListLocal(meal : Meal, eventType : EventType){
        if(eventType == EventType.CREATED){
            println("Size before : ${updatedLocal.size}")
            addedLocal.remove(meal)
            println("Size after : ${updatedLocal.size}")
        }else if(eventType == EventType.UPDATED){
            println("Size before : ${updatedLocal.size}")
            updatedLocal.remove(meal)
            println("Size after : ${updatedLocal.size}")
        }
        needWorkers = addedLocal.size > 0 || updatedLocal.size > 0
    }

    suspend fun updateLocal(meal : Meal){
        val mealLocal = this.convertToMealLocal(meal)
        this.mealDao.update(mealLocal)
        val attributesLocal = this.convertToFoodAttributeLocalList(meal.id, meal.foods)
        attributesLocal.forEach { attribute -> this.attributeDao.update(attribute)
        }
        needWorkers = true
    }

    suspend fun update(id : Int, meal : Meal) : MyResult<Meal> {
        try{
            if(networkStatus){
                val result = MealApi.service.update(id, meal)
                return MyResult.Success(result)
            }
            updateLocal(meal)
            updatedLocal.add(meal)
            refresh()
            return MyResult.Success(meal)
        }catch(ex : Exception){
            if(ex is HttpException){
                val message : String? = ex.response()?.errorBody()?.string()
                val e = Exception(message)
                return MyResult.Error(e)
            }
            return MyResult.Error(ex)
        }
    }

    suspend fun deleteAllLocal(){
        this.attributeDao.deleteAll()
        this.mealDao.deleteAll()
    }

    suspend fun refreshLocal(items: List<Meal>) {
        this.items.value = items
//        println("meals local ${this.mealDao.getAll().value}")
//        for (item in items) {
//            val mealLocal = this.convertToMealLocal(item)
//            println("meal local ${mealLocal}")
//            this.mealDao.insert(mealLocal)
//            val attributesLocal = this.convertToFoodAttributeLocalList(item.id, item.foods)
//            attributesLocal.forEach { attribute -> this.attributeDao.insert(attribute) }
//        }
//        this.mealsLocal = this.mealDao.getAll()
//        this.attributesLocal = this.attributeDao.getAll()
//        println("meals local ${this.mealDao.getAll().value}")
//        this.setItems()
    }

    private fun saveToInternalStorage(bitmapImage: Bitmap, imageName: String, context: Context): String? {
        val cw = ContextWrapper(context)
        // path to /data/data/yourapp/app_data/imageDir
        val directory: File = cw.getDir("file_image", Context.MODE_PRIVATE)
        // Create imageDir
        val mypath = File(directory, imageName)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(mypath)
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return directory.getAbsolutePath()
    }

    fun PostImage(item: Meal, bitmapImage: Bitmap, context: Context) {

        // Take image form your ImageView
        saveToInternalStorage(bitmapImage, "food.png", context)
        val cw = ContextWrapper(context)
        // path to /data/data/yourapp/app_data/imageDir
        val path: File = cw.getDir("file_image", Context.MODE_PRIVATE)
        val file = File(path, "food.png")

        val requestBody =
            MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("id", item.id.toString())
                .addFormDataPart("category", item.category.toString())
                .addFormDataPart("served_on", item.served_on.toString())
                .addFormDataPart("created_at", item.created_at.toString())
                .addFormDataPart("updated_at", item.updated_at.toString())
                // Upload parameters
                .addFormDataPart(
                    "photo",
                    file?.name,
                    file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                ) // Upload files
                .build()
        var request = Request.Builder().url("http://${Api.baseURL}/api/meals").post(requestBody).build()
        var client = OkHttpClient()
        client
            .newCall(request)
            .enqueue(
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {}

                    override fun onResponse(call: Call, response: Response) {
                        runBlocking {
                            refresh()
                        }
                    }
                }
            )
    }
}
