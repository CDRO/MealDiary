package ch.schmidlins.mealdiary.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ch.schmidlins.mealdiary.data.dao.BowelMovementDao
import ch.schmidlins.mealdiary.data.dao.MealDao
import ch.schmidlins.mealdiary.data.dao.WeightEntryDao
import ch.schmidlins.mealdiary.data.entities.BowelMovement
import ch.schmidlins.mealdiary.data.entities.Meal
import ch.schmidlins.mealdiary.data.entities.WeightEntry

@Database(entities = [Meal::class, BowelMovement::class, WeightEntry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun bowelMovementDao(): BowelMovementDao
    abstract fun weightEntryDao(): WeightEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "meal_diary_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
