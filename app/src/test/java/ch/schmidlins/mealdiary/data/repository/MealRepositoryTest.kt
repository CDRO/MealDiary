package ch.schmidlins.mealdiary.data.repository

import ch.schmidlins.mealdiary.data.dao.MealDao
import ch.schmidlins.mealdiary.data.entities.Meal
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MealRepositoryTest {

    private lateinit var mealDao: MealDao
    private lateinit var repository: MealRepository

    @Before
    fun setup() {
        mealDao = mockk()
        every { mealDao.getAllMealsFlow() } returns flowOf(emptyList())
        repository = MealRepository(mealDao)
    }

    @Test
    fun `allMeals returns flow from dao`() = runTest {
        val meals = listOf(Meal(1, 1000, "Pizza"))
        val mockDao = mockk<MealDao>()
        every { mockDao.getAllMealsFlow() } returns flowOf(meals)
        val repo = MealRepository(mockDao)

        val result = repo.allMeals
        
        result.collect {
            assertEquals(meals, it)
        }
    }

    @Test
    fun `insertMeal calls dao insert`() = runTest {
        val meal = Meal(1, 1000, "Pizza")
        coEvery { mealDao.insertMeal(meal) } returns Unit

        repository.insertMeal(meal)

        coVerify { mealDao.insertMeal(meal) }
    }
}
