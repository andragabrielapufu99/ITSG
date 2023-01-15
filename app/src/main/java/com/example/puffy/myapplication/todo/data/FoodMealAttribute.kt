package com.example.puffy.myapplication.todo.data

data class FoodMealAttribute(
    var id: Int,
    var food_id : Int,
    var name: String,
    var calories: Float,
    var fat: Float,
    var protein: Float,
    var carbs: Float,
    var serving_size : Float?
){
    override fun toString(): String {
        var s: String =
            "Name : $name\nCalories : $calories\nProtein: ${protein}\nCarbs: ${carbs}";
        if (serving_size != null) s = "$s\nServing size: ${serving_size}"
        return s
    }
}
