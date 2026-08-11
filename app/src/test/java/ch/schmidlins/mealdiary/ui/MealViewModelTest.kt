package ch.schmidlins.mealdiary.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import ch.schmidlins.mealdiary.data.entities.BowelMovement
import ch.schmidlins.mealdiary.data.entities.Meal
import ch.schmidlins.mealdiary.data.entities.WeightEntry
import ch.schmidlins.mealdiary.data.repository.BMRepository
import ch.schmidlins.mealdiary.data.repository.MealRepository
import ch.schmidlins.mealdiary.data.repository.UserPreferencesRepository
import ch.schmidlins.mealdiary.data.repository.WeightRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class MealViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var mealRepository: MealRepository
    private lateinit var bmRepository: BMRepository
    private lateinit var weightRepository: WeightRepository
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var viewModel: MealViewModel

    private val mealsFlow = MutableStateFlow<List<Meal>>(emptyList())
    private val bmsFlow = MutableStateFlow<List<BowelMovement>>(emptyList())
    private val weightsFlow = MutableStateFlow<List<WeightEntry>>(emptyList())
    private val firstMealTimestampFlow = MutableStateFlow<Long?>(null)
    private val lastBMTimestampFlow = MutableStateFlow<Long?>(null)
    private val bmIntervalFlow = MutableStateFlow(24)
    private val isWeightTrackingEnabledFlow = MutableStateFlow(false)
    private val weightSuggestionDismissedFlow = MutableStateFlow(false)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mealRepository = mockk(relaxed = true)
        bmRepository = mockk(relaxed = true)
        weightRepository = mockk(relaxed = true)
        userPreferencesRepository = mockk(relaxed = true)

        every { mealRepository.allMeals } returns mealsFlow
        every { bmRepository.allBMs } returns bmsFlow
        every { weightRepository.allWeightEntries } returns weightsFlow
        every { mealRepository.firstMealTimestamp } returns firstMealTimestampFlow
        every { bmRepository.lastBMTimestamp } returns lastBMTimestampFlow
        every { userPreferencesRepository.bmPromptIntervalHours } returns bmIntervalFlow
        every { userPreferencesRepository.isWeightTrackingEnabled } returns isWeightTrackingEnabledFlow
        every { userPreferencesRepository.weightSuggestionDismissed } returns weightSuggestionDismissedFlow

        viewModel = MealViewModel(mealRepository, bmRepository, weightRepository, userPreferencesRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `dailySummaries correctly groups items by date`() {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val todayMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val yesterdayMillis = yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        mealsFlow.value = listOf(
            Meal(1, todayMillis + 1000, "Breakfast"),
            Meal(2, todayMillis + 2000, "Lunch"),
            Meal(3, yesterdayMillis + 1000, "Dinner")
        )
        bmsFlow.value = listOf(
            BowelMovement(1, todayMillis + 3000),
            BowelMovement(2, yesterdayMillis + 2000)
        )

        val observer = mockk<Observer<List<DailySummary>>>(relaxed = true)
        viewModel.dailySummaries.observeForever(observer)

        val expected = listOf(
            DailySummary(today, 2, 1),
            DailySummary(yesterday, 1, 1)
        )
        verify { observer.onChanged(expected) }
    }

    @Test
    fun `todayTimeline only contains items from today`() {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val todayMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val yesterdayMillis = yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        mealsFlow.value = listOf(
            Meal(1, todayMillis + 1000, "Today's Meal"),
            Meal(2, yesterdayMillis + 1000, "Yesterday's Meal")
        )

        val observer = mockk<Observer<List<FeedItem>>>(relaxed = true)
        viewModel.todayTimeline.observeForever(observer)

        val captured = mutableListOf<List<FeedItem>>()
        verify { observer.onChanged(capture(captured)) }
        
        val todayItems = captured.last()
        assert(todayItems.all { 
            java.time.Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate() == today 
        })
        assert(todayItems.size == 1)
    }

    @Test
    fun `shouldAskAboutBM is false when no meals exist`() {
        firstMealTimestampFlow.value = null
        lastBMTimestampFlow.value = null

        val observer = mockk<Observer<Boolean>>(relaxed = true)
        viewModel.shouldAskAboutBM.observeForever(observer)

        verify { observer.onChanged(false) }
    }

    @Test
    fun `shouldAskAboutBM is false when first meal was less than 24h ago`() {
        val now = System.currentTimeMillis()
        firstMealTimestampFlow.value = now - (23 * 60 * 60 * 1000) // 23h ago
        lastBMTimestampFlow.value = null

        val observer = mockk<Observer<Boolean>>(relaxed = true)
        viewModel.shouldAskAboutBM.observeForever(observer)

        verify { observer.onChanged(false) }
    }

    @Test
    fun `shouldAskAboutBM is true when first meal was more than 24h ago and no BM exists`() {
        val now = System.currentTimeMillis()
        firstMealTimestampFlow.value = now - (25 * 60 * 60 * 1000) // 25h ago
        lastBMTimestampFlow.value = null

        val observer = mockk<Observer<Boolean>>(relaxed = true)
        viewModel.shouldAskAboutBM.observeForever(observer)

        verify { observer.onChanged(true) }
    }

    @Test
    fun `shouldAskAboutBM respects configured 12h interval`() {
        val now = System.currentTimeMillis()
        firstMealTimestampFlow.value = now - (25 * 60 * 60 * 1000) // 25h ago
        lastBMTimestampFlow.value = now - (13 * 60 * 60 * 1000) // 13h ago
        bmIntervalFlow.value = 12

        val observer = mockk<Observer<Boolean>>(relaxed = true)
        viewModel.shouldAskAboutBM.observeForever(observer)

        verify { observer.onChanged(true) }
    }

    @Test
    fun `shouldAskAboutBM respects configured 48h interval`() {
        val now = System.currentTimeMillis()
        firstMealTimestampFlow.value = now - (60 * 60 * 60 * 1000) // long ago
        lastBMTimestampFlow.value = now - (25 * 60 * 60 * 1000) // 25h ago
        bmIntervalFlow.value = 48

        val observer = mockk<Observer<Boolean>>(relaxed = true)
        viewModel.shouldAskAboutBM.observeForever(observer)

        verify { observer.onChanged(false) }
    }

    @Test
    fun `shouldShowWeightSuggestion is true after 7 days if not enabled or dismissed`() {
        val now = System.currentTimeMillis()
        firstMealTimestampFlow.value = now - (8 * 24 * 60 * 60 * 1000L) // 8 days ago
        isWeightTrackingEnabledFlow.value = false
        weightSuggestionDismissedFlow.value = false

        val observer = mockk<Observer<Boolean>>(relaxed = true)
        viewModel.shouldShowWeightSuggestion.observeForever(observer)

        verify { observer.onChanged(true) }
    }

    @Test
    fun `shouldShowWeightSuggestion is false before 7 days`() {
        val now = System.currentTimeMillis()
        firstMealTimestampFlow.value = now - (6 * 24 * 60 * 60 * 1000L) // 6 days ago
        isWeightTrackingEnabledFlow.value = false
        weightSuggestionDismissedFlow.value = false

        val observer = mockk<Observer<Boolean>>(relaxed = true)
        viewModel.shouldShowWeightSuggestion.observeForever(observer)

        verify { observer.onChanged(false) }
    }

    @Test
    fun `shouldShowWeightSuggestion is false if weight tracking is already enabled`() {
        val now = System.currentTimeMillis()
        firstMealTimestampFlow.value = now - (8 * 24 * 60 * 60 * 1000L)
        isWeightTrackingEnabledFlow.value = true
        weightSuggestionDismissedFlow.value = false

        val observer = mockk<Observer<Boolean>>(relaxed = true)
        viewModel.shouldShowWeightSuggestion.observeForever(observer)

        verify { observer.onChanged(false) }
    }

    @Test
    fun `shouldShowWeightSuggestion is false if suggestion was dismissed`() {
        val now = System.currentTimeMillis()
        firstMealTimestampFlow.value = now - (8 * 24 * 60 * 60 * 1000L)
        isWeightTrackingEnabledFlow.value = false
        weightSuggestionDismissedFlow.value = true

        val observer = mockk<Observer<Boolean>>(relaxed = true)
        viewModel.shouldShowWeightSuggestion.observeForever(observer)

        verify { observer.onChanged(false) }
    }

    @Test
    fun `statistics correctly calculates avg BM frequency and weight delta`() {
        val now = System.currentTimeMillis()
        val twoDaysAgo = now - (2 * 24 * 60 * 60 * 1000L)
        
        mealsFlow.value = listOf(Meal(1, twoDaysAgo, "First Meal"))
        bmsFlow.value = listOf(
            BowelMovement(1, now),
            BowelMovement(2, now - 1000)
        )
        
        val weights = listOf(
            WeightEntry(1, now, 70.0),
            WeightEntry(2, twoDaysAgo, 72.0)
        )
        weightsFlow.value = weights

        val observer = mockk<Observer<Statistics>>(relaxed = true)
        viewModel.statistics.observeForever(observer)

        val captured = mutableListOf<Statistics>()
        verify { observer.onChanged(capture(captured)) }
        
        val stats = captured.last()
        // 2 BMs over 2 days = 1.0 per day
        assertEquals(1.0, stats.avgBMFrequency, 0.1)
        assertEquals(-2.0, stats.weightDelta!!, 0.1)
    }
}
