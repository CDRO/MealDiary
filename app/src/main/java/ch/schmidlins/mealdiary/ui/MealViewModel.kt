package ch.schmidlins.mealdiary.ui

import androidx.lifecycle.*
import ch.schmidlins.mealdiary.data.entities.BowelMovement
import ch.schmidlins.mealdiary.data.entities.Meal
import ch.schmidlins.mealdiary.data.repository.BMRepository
import ch.schmidlins.mealdiary.data.repository.MealRepository
import kotlinx.coroutines.launch

class MealViewModel(
    private val mealRepository: MealRepository,
    private val bmRepository: BMRepository
) : ViewModel() {

    val meals: LiveData<List<Meal>> = mealRepository.allMeals.asLiveData()
    val bms: LiveData<List<BowelMovement>> = bmRepository.allBMs.asLiveData()

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
    private val bmRepository: BMRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MealViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MealViewModel(mealRepository, bmRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
