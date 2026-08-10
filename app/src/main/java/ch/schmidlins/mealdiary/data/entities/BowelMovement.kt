package ch.schmidlins.mealdiary.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bowel_movements")
data class BowelMovement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val notes: String? = null
)
