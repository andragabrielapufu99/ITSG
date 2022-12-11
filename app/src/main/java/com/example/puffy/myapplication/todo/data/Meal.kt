package com.example.puffy.myapplication.todo.data

data class Meal(
    var id: Int,
    var category: String,
    var served_on: String,
    var created_at: String?,
    var updated_at: String?,
    var foods: ArrayList<FoodMealAttribute>,
    var calories: Float,
    var pathImage: String?
) {
    override fun toString(): String {
        var s: String = "Category : $category\nCalories: $calories\nServed On : $served_on"
        if(created_at != null) s = "$s\nCreated At : $created_at"
        if(updated_at != null) s = "$s\nUpdated At : $updated_at"
        if(!foods.isEmpty()) {
            s = "$s\n\nFoods Attributes"
            for(food in foods){
                s = "$s\n\n$food"
            }
        }
        return s
    }
//    override fun toString(): String =
//        "Category : $category\nServed On : $served_on\nCreated At : $created_at\nUpdated At : $updated_at\nFood Attributes: ${foods.toString()}"
}