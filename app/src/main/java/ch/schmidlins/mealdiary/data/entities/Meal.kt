package ch.schmidlins.mealdiary.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val description: String,
    val photoUri: String? = null,
    val notes: String? = null
)
