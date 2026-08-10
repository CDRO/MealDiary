package ch.schmidlins.mealdiary.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import ch.schmidlins.mealdiary.data.repository.BMRepository
import ch.schmidlins.mealdiary.data.repository.MealRepository
import ch.schmidlins.mealdiary.data.repository.WeightRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
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
    private lateinit var viewModel: MealViewModel

    private val firstMealTimestampFlow = MutableStateFlow<Long?>(null)
    private val lastBMTimestampFlow = MutableStateFlow<Long?>(null)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mealRepository = mockk(relaxed = true)
        bmRepository = mockk(relaxed = true)
        weightRepository = mockk(relaxed = true)

        every { mealRepository.allMeals } returns flowOf(emptyList())
        every { bmRepository.allBMs } returns flowOf(emptyList())
        every { mealRepository.firstMealTimestamp } returns firstMealTimestampFlow
        every { bmRepository.lastBMTimestamp } returns lastBMTimestampFlow

        viewModel = MealViewModel(mealRepository, bmRepository, weightRepository)
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
    fun `shouldAskAboutBM is true when first meal and last BM were more than 24h ago`() {
        val now = System.currentTimeMillis()
        firstMealTimestampFlow.value = now - (48 * 60 * 60 * 1000) // 2 days ago
        lastBMTimestampFlow.value = now - (25 * 60 * 60 * 1000) // 25h ago

        val observer = mockk<Observer<Boolean>>(relaxed = true)
        viewModel.shouldAskAboutBM.observeForever(observer)

        verify { observer.onChanged(true) }
    }

    @Test
    fun `shouldAskAboutBM is false when last BM was less than 24h ago`() {
        val now = System.currentTimeMillis()
        firstMealTimestampFlow.value = now - (48 * 60 * 60 * 1000) // 2 days ago
        lastBMTimestampFlow.value = now - (23 * 60 * 60 * 1000) // 23h ago

        val observer = mockk<Observer<Boolean>>(relaxed = true)
        viewModel.shouldAskAboutBM.observeForever(observer)

        verify { observer.onChanged(false) }
    }
}
