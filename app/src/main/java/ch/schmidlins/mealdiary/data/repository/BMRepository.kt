package ch.schmidlins.mealdiary.data.repository

import ch.schmidlins.mealdiary.data.dao.BowelMovementDao
import ch.schmidlins.mealdiary.data.entities.BowelMovement
import kotlinx.coroutines.flow.Flow

class BMRepository(private val bmDao: BowelMovementDao) {
    val allBMs: Flow<List<BowelMovement>> = bmDao.getAllBMsFlow()
    val lastBMTimestamp: Flow<Long?> = bmDao.getLastBMTimestampFlow()

    suspend fun insertBM(bm: BowelMovement) {
        bmDao.insertBM(bm)
    }

    suspend fun deleteBM(bm: BowelMovement) {
        bmDao.deleteBM(bm)
    }
}
