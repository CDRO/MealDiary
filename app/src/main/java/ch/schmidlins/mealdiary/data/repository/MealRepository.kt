package ch.schmidlins.mealdiary.data.repository

import ch.schmidlins.mealdiary.data.dao.MealDao
import ch.schmidlins.mealdiary.data.entities.Meal
import kotlinx.coroutines.flow.Flow

class MealRepository(private val mealDao: MealDao) {
    val allMeals: Flow<List<Meal>> = mealDao.getAllMealsFlow()

    suspend fun insertMeal(meal: Meal) {
        mealDao.insertMeal(meal)
    }

    suspend fun deleteMeal(meal: Meal) {
        mealDao.deleteMeal(meal)
    }
}
