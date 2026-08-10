package ch.schmidlins.mealdiary.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
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

        every { mealRepository.allMeals } returns flowOf(emptyList())
        every { bmRepository.allBMs } returns flowOf(emptyList())
        every { weightRepository.allWeightEntries } returns flowOf(emptyList())
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
}
