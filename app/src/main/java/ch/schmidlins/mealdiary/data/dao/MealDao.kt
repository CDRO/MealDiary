package ch.schmidlins.mealdiary.data.dao

import androidx.room.*
import ch.schmidlins.mealdiary.data.entities.Meal
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meals ORDER BY timestamp DESC")
    fun getAllMealsFlow(): Flow<List<Meal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: Meal)

    @Delete
    suspend fun deleteMeal(meal: Meal)

    @Query("SELECT MIN(timestamp) FROM meals")
    fun getFirstMealTimestampFlow(): Flow<Long?>

    @Query("SELECT description FROM meals GROUP BY description HAVING COUNT(*) >= 5")
    fun getRecurringMealDescriptionsFlow(): Flow<List<String>>
}
