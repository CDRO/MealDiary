package ch.schmidlins.mealdiary.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_entries")
data class WeightEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val weight: Double,
    val unit: String = "kg"
)
