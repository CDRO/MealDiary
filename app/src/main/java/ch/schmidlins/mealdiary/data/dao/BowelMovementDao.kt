package ch.schmidlins.mealdiary.data.dao

import androidx.room.*
import ch.schmidlins.mealdiary.data.entities.BowelMovement
import kotlinx.coroutines.flow.Flow

@Dao
interface BowelMovementDao {
    @Query("SELECT * FROM bowel_movements ORDER BY timestamp DESC")
    fun getAllBMsFlow(): Flow<List<BowelMovement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBM(bm: BowelMovement)

    @Delete
    suspend fun deleteBM(bm: BowelMovement)

    @Query("SELECT MAX(timestamp) FROM bowel_movements")
    fun getLastBMTimestampFlow(): Flow<Long?>
}
