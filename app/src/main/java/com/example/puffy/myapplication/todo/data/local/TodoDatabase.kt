package com.example.puffy.myapplication.todo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.puffy.myapplication.auth.data.TokenHolder
import com.example.puffy.myapplication.todo.data.FoodMealAttributeLocal
import com.example.puffy.myapplication.todo.data.MealLocal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [TokenHolder::class,MealLocal::class, FoodMealAttributeLocal::class], version = 10)
abstract class TodoDatabase : RoomDatabase() {

    abstract fun tokenDao() : TokenDao
    abstract fun mealDao(): MealDao
    abstract fun foodAttributeDao(): FoodAttributeDao

    companion object {
        @Volatile
        private var INSTANCE: TodoDatabase? = null

//                @kotlinx.coroutines.InternalCoroutinesApi()
        fun getDatabase(context: Context, scope: CoroutineScope): TodoDatabase {
            val inst = INSTANCE
            if (inst != null) {
                return inst
            }
            val instance =
                Room.databaseBuilder(
                    context.applicationContext,
                    TodoDatabase::class.java,
                    "todo_db"
                )
                    .addCallback(WordDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
            INSTANCE = instance
            return instance
        }

        private class WordDatabaseCallback(private val scope: CoroutineScope) :
            RoomDatabase.Callback() {

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.mealDao(), database.foodAttributeDao(), database.tokenDao())
                    }
                }
            }
        }

        suspend fun populateDatabase(mealDao: MealDao, foodAttributeDao: FoodAttributeDao, tokenDao: TokenDao) {
        }
    }

}