package ch.schmidlins.mealdiary.data.repository

import ch.schmidlins.mealdiary.data.dao.WeightEntryDao
import ch.schmidlins.mealdiary.data.entities.WeightEntry
import kotlinx.coroutines.flow.Flow

class WeightRepository(private val weightEntryDao: WeightEntryDao) {
    val allWeightEntries: Flow<List<WeightEntry>> = weightEntryDao.getAllWeightEntriesFlow()

    suspend fun insertWeight(entry: WeightEntry) {
        weightEntryDao.insertWeight(entry)
    }

    suspend fun deleteWeight(entry: WeightEntry) {
        weightEntryDao.deleteWeight(entry)
    }
}
