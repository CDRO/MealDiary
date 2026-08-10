package ch.schmidlins.mealdiary.ui

import androidx.lifecycle.*
import ch.schmidlins.mealdiary.data.entities.BowelMovement
import ch.schmidlins.mealdiary.data.entities.Meal
import ch.schmidlins.mealdiary.data.repository.BMRepository
import ch.schmidlins.mealdiary.data.repository.MealRepository
import ch.schmidlins.mealdiary.data.repository.WeightRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

sealed class FeedItem {
    abstract val id: Long
    abstract val timestamp: Long

    data class MealItem(val meal: Meal) : FeedItem() {
        override val id: Long = meal.id
        override val timestamp: Long = meal.timestamp
    }

    data class BMItem(val bm: BowelMovement) : FeedItem() {
        override val id: Long = bm.id
        override val timestamp: Long = bm.timestamp
    }
}

class MealViewModel(
    private val mealRepository: MealRepository,
    private val bmRepository: BMRepository,
    private val weightRepository: WeightRepository
) : ViewModel() {

    val meals: LiveData<List<Meal>> = mealRepository.allMeals.asLiveData()
    val bms: LiveData<List<BowelMovement>> = bmRepository.allBMs.asLiveData()

    private val ticker = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(60_000) // Update every minute
        }
    }

    val timeSinceLastBM: LiveData<String?> = combine(
        bmRepository.lastBMTimestamp,
        ticker
    ) { lastBM, now ->
        if (lastBM == null) return@combine null
        val diff = now - lastBM
        val hours = diff / (60 * 60 * 1000)
        val mins = (diff % (60 * 60 * 1000)) / (60 * 1000)
        "${hours}h ${mins}m since last BM"
    }.asLiveData()

    val unifiedFeed: LiveData<List<FeedItem>> = combine(
        mealRepository.allMeals,
        bmRepository.allBMs
    ) { meals, bms ->
        val items = mutableListOf<FeedItem>()
        items.addAll(meals.map { FeedItem.MealItem(it) })
        items.addAll(bms.map { FeedItem.BMItem(it) })
        items.sortByDescending { it.timestamp }
        items
    }.asLiveData()

    val shouldAskAboutBM: LiveData<Boolean> = combine(
        mealRepository.firstMealTimestamp,
        bmRepository.lastBMTimestamp
    ) { firstMeal, lastBM ->
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L
        
        if (firstMeal == null) return@combine false
        
        val passedFirstMealThreshold = (now - firstMeal) > oneDayMillis
        if (!passedFirstMealThreshold) return@combine false
        
        val noRecentBM = lastBM == null || (now - lastBM) > oneDayMillis
        noRecentBM
    }.asLiveData()

    fun addMeal(description: String) {
        viewModelScope.launch {
            val meal = Meal(
                timestamp = System.currentTimeMillis(),
                description = description
            )
            mealRepository.insertMeal(meal)
        }
    }

    fun addBowelMovement() {
        viewModelScope.launch {
            val bm = BowelMovement(
                timestamp = System.currentTimeMillis()
            )
            bmRepository.insertBM(bm)
        }
    }
}

class MealViewModelFactory(
    private val mealRepository: MealRepository,
    private val bmRepository: BMRepository,
    private val weightRepository: WeightRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MealViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MealViewModel(mealRepository, bmRepository, weightRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
