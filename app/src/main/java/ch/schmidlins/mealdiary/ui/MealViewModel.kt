package ch.schmidlins.mealdiary.ui

import androidx.lifecycle.*
import ch.schmidlins.mealdiary.AnalysisEngine
import ch.schmidlins.mealdiary.data.entities.BowelMovement
import ch.schmidlins.mealdiary.data.entities.Meal
import ch.schmidlins.mealdiary.data.entities.WeightEntry
import ch.schmidlins.mealdiary.data.repository.BMRepository
import ch.schmidlins.mealdiary.data.repository.MealRepository
import ch.schmidlins.mealdiary.data.repository.UserPreferencesRepository
import ch.schmidlins.mealdiary.data.repository.WeightRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class DailySummary(
    val date: LocalDate,
    val mealCount: Int,
    val bmCount: Int
)

data class Statistics(
    val avgBMFrequency: Double,
    val weightDelta: Double?,
    val avgWeight: Double?,
    val topFoods: List<Pair<String, Int>>,
    val weightHistory: List<WeightEntry>
)

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

    data class WeightItem(val weightEntry: WeightEntry) : FeedItem() {
        override val id: Long = weightEntry.id
        override val timestamp: Long = weightEntry.timestamp
    }
}

class MealViewModel(
    private val mealRepository: MealRepository,
    private val bmRepository: BMRepository,
    private val weightRepository: WeightRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val analysisEngine: AnalysisEngine = AnalysisEngine()
) : ViewModel() {

    private val _mealInputText = MutableStateFlow("")
    val mealInputText: StateFlow<String> = _mealInputText

    fun updateMealInputText(text: String) {
        _mealInputText.value = text
    }

    val mealSuggestions: LiveData<List<String>> = combine(
        mealRepository.recurringMealDescriptions,
        _mealInputText
    ) { recurring, input ->
        if (input.isBlank()) emptyList<String>()
        else recurring.filter { it.contains(input, ignoreCase = true) && it != input }.take(5)
    }.asLiveData()

    val meals: LiveData<List<Meal>> = mealRepository.allMeals.asLiveData()
    val bms: LiveData<List<BowelMovement>> = bmRepository.allBMs.asLiveData()

    val isWeightTrackingEnabled: LiveData<Boolean> = userPreferencesRepository.isWeightTrackingEnabled.asLiveData()
    val widgetOrder: LiveData<List<String>> = userPreferencesRepository.widgetOrder.asLiveData()
    val enabledWidgets: LiveData<Set<String>> = userPreferencesRepository.enabledWidgets.asLiveData()

    val shouldShowWeightSuggestion: LiveData<Boolean> = combine(
        userPreferencesRepository.isWeightTrackingEnabled,
        userPreferencesRepository.weightSuggestionDismissed,
        mealRepository.firstMealTimestamp
    ) { enabled, dismissed, firstMeal ->
        val now = System.currentTimeMillis()
        val sevenDaysMillis = 7 * 24 * 60 * 60 * 1000L
        
        !enabled && !dismissed && firstMeal != null && (now - firstMeal) > sevenDaysMillis
    }.asLiveData()

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
        bmRepository.allBMs,
        weightRepository.allWeightEntries
    ) { meals, bms, weights ->
        val items = mutableListOf<FeedItem>()
        items.addAll(meals.map { FeedItem.MealItem(it) })
        items.addAll(bms.map { FeedItem.BMItem(it) })
        items.addAll(weights.map { FeedItem.WeightItem(it) })
        items.sortByDescending { it.timestamp }
        items
    }.asLiveData()

    val dailySummaries: LiveData<List<DailySummary>> = combine(
        mealRepository.allMeals,
        bmRepository.allBMs
    ) { meals, bms ->
        val summaries = mutableMapOf<LocalDate, Pair<Int, Int>>()
        
        meals.forEach { meal ->
            val date = Instant.ofEpochMilli(meal.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            val current = summaries.getOrDefault(date, Pair(0, 0))
            summaries[date] = current.copy(first = current.first + 1)
        }
        
        bms.forEach { bm ->
            val date = Instant.ofEpochMilli(bm.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            val current = summaries.getOrDefault(date, Pair(0, 0))
            summaries[date] = current.copy(second = current.second + 1)
        }
        
        summaries.entries.map { (date, counts) ->
            DailySummary(date, counts.first, counts.second)
        }.sortedByDescending { it.date }
    }.asLiveData()

    val weeklySummary: LiveData<DailySummary> = dailySummaries.map { summaries ->
        val last7Days = summaries.take(7)
        DailySummary(
            date = LocalDate.now(), // Represents current week context
            mealCount = last7Days.sumOf { it.mealCount },
            bmCount = last7Days.sumOf { it.bmCount }
        )
    }

    val statistics: LiveData<Statistics> = combine(
        mealRepository.allMeals,
        bmRepository.allBMs,
        weightRepository.allWeightEntries
    ) { meals, bms, weights ->
        val totalDays: Double = if (meals.isEmpty()) 1.0 else {
            val firstMeal = meals.minOf { it.timestamp }
            val diff = System.currentTimeMillis() - firstMeal
            (diff / (24 * 60 * 60 * 1000L)).coerceAtLeast(1).toDouble()
        }

        val avgBM = bms.size.toDouble() / totalDays
        
        val delta = if (weights.size >= 2) {
            val latest = weights.first().weight
            val first = weights.last().weight
            latest - first
        } else null

        val avgWeight = if (weights.isNotEmpty()) weights.map { it.weight }.average() else null

        val topFoods = meals.groupBy { it.description }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(5)

        Statistics(avgBM, delta, avgWeight, topFoods, weights.sortedBy { it.timestamp })
    }.asLiveData()

    val insights: LiveData<List<String>> = combine(
        mealRepository.allMeals,
        bmRepository.allBMs
    ) { meals, bms ->
        if (meals.size < 5) return@combine emptyList<String>()
        analysisEngine.analyze(meals, bms)
    }.asLiveData()

    val todayTimeline: LiveData<List<FeedItem>> = unifiedFeed.asFlow().map { items ->
        val today = LocalDate.now()
        items.filter { item ->
            Instant.ofEpochMilli(item.timestamp).atZone(ZoneId.systemDefault()).toLocalDate() == today
        }
    }.asLiveData()

    val shouldAskAboutBM: LiveData<Boolean> = combine(
        mealRepository.firstMealTimestamp,
        bmRepository.lastBMTimestamp,
        userPreferencesRepository.bmPromptIntervalHours
    ) { firstMeal, lastBM, intervalHours ->
        val now = System.currentTimeMillis()
        val intervalMillis = intervalHours * 60 * 60 * 1000L
        val oneDayMillis = 24 * 60 * 60 * 1000L // First meal threshold remains 24h
        
        if (firstMeal == null) return@combine false
        
        val passedFirstMealThreshold = (now - firstMeal) > oneDayMillis
        if (!passedFirstMealThreshold) return@combine false
        
        val noRecentBM = lastBM == null || (now - lastBM) > intervalMillis
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

    fun deleteMeal(meal: Meal) {
        viewModelScope.launch {
            mealRepository.deleteMeal(meal)
        }
    }

    fun deleteBM(bm: BowelMovement) {
        viewModelScope.launch {
            bmRepository.deleteBM(bm)
        }
    }

    fun updateBM(bm: BowelMovement) {
        viewModelScope.launch {
            bmRepository.insertBM(bm) // Room @Insert(onConflict = REPLACE) handles update
        }
    }

    fun deleteWeight(entry: WeightEntry) {
        viewModelScope.launch {
            weightRepository.deleteWeight(entry)
        }
    }

    fun addWeightEntry(weight: Double) {
        viewModelScope.launch {
            val entry = WeightEntry(
                timestamp = System.currentTimeMillis(),
                weight = weight
            )
            weightRepository.insertWeight(entry)
        }
    }

    fun dismissWeightSuggestion() {
        viewModelScope.launch {
            userPreferencesRepository.dismissWeightSuggestion()
        }
    }

    fun enableWeightTracking() {
        viewModelScope.launch {
            userPreferencesRepository.updateWeightTrackingEnabled(true)
        }
    }

    fun updateWidgetOrder(order: List<String>) {
        viewModelScope.launch {
            userPreferencesRepository.updateWidgetOrder(order)
        }
    }

    fun updateEnabledWidgets(enabled: Set<String>) {
        viewModelScope.launch {
            userPreferencesRepository.updateEnabledWidgets(enabled)
        }
    }

    suspend fun getCSVData(): String {
        val meals = mealRepository.allMeals.first()
        val bms = bmRepository.allBMs.first()
        val weights = weightRepository.allWeightEntries.first()

        val sb = StringBuilder()
        sb.append("Timestamp,Type,Value,Notes\n")

        val escape = { s: String? ->
            if (s == null) "\"\""
            else "\"${s.replace("\"", "\"\"")}\""
        }

        meals.forEach { meal ->
            sb.append("${meal.timestamp},MEAL,${escape(meal.description)},${escape(meal.notes)}\n")
        }

        bms.forEach { bm ->
            val extInfo = if (bm.consistency != null) "Bristol: ${bm.consistency}; Pain: ${bm.painLevel}; Duration: ${bm.durationMinutes}m" else ""
            sb.append("${bm.timestamp},BM,${escape(extInfo)},${escape(bm.notes)}\n")
        }

        weights.forEach { weight ->
            sb.append("${weight.timestamp},WEIGHT,${escape("${weight.weight} ${weight.unit}")},\"\"\n")
        }

        return sb.toString()
    }
}

class MealViewModelFactory(
    private val mealRepository: MealRepository,
    private val bmRepository: BMRepository,
    private val weightRepository: WeightRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MealViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MealViewModel(mealRepository, bmRepository, weightRepository, userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
