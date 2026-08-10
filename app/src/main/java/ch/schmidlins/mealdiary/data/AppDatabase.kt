package ch.schmidlins.mealdiary.data

import androidx.room.Database
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
}
