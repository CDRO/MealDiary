package ch.schmidlins.mealdiary.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import ch.schmidlins.mealdiary.MealDiaryApp
import ch.schmidlins.mealdiary.data.entities.Meal
import ch.schmidlins.mealdiary.data.repository.BMRepository
import ch.schmidlins.mealdiary.data.repository.MealRepository
import ch.schmidlins.mealdiary.data.repository.UserPreferencesRepository
import ch.schmidlins.mealdiary.data.repository.WeightRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test

class MealLogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testMealLoggingFlow() {
        val mealRepo = mockk<MealRepository>(relaxed = true)
        val bmRepo = mockk<BMRepository>(relaxed = true)
        val weightRepo = mockk<WeightRepository>(relaxed = true)
        
        every { mealRepo.allMeals } returns flowOf(emptyList<Meal>())
        every { mealRepo.firstMealTimestamp } returns flowOf(null)
        every { bmRepo.allBMs } returns flowOf(emptyList())
        every { bmRepo.lastBMTimestamp } returns flowOf(null)
        val prefsRepo = mockk<UserPreferencesRepository>(relaxed = true)
        every { prefsRepo.bmPromptIntervalHours } returns flowOf(24)
        
        val viewModel = MealViewModel(mealRepo, bmRepo, weightRepo, prefsRepo)

        composeTestRule.setContent {
            MealDiaryApp(viewModel)
        }

        // Enter meal text
        composeTestRule.onNodeWithText("What did you eat?").performTextInput("Pizza")
        
        // Click Log Meal
        composeTestRule.onNodeWithText("Log Meal").performClick()

        // Verify the text is cleared (basic check for interaction)
        composeTestRule.onNodeWithText("Pizza").assertDoesNotExist()
    }

    @Test
    fun testBMPromptVisibility() {
        val mealRepo = mockk<MealRepository>(relaxed = true)
        val bmRepo = mockk<BMRepository>(relaxed = true)
        val weightRepo = mockk<WeightRepository>(relaxed = true)
        
        val now = System.currentTimeMillis()
        // First meal was 25h ago -> should prompt
        every { mealRepo.allMeals } returns flowOf(emptyList())
        every { mealRepo.firstMealTimestamp } returns flowOf(now - (25 * 60 * 60 * 1000))
        every { bmRepo.allBMs } returns flowOf(emptyList())
        every { bmRepo.lastBMTimestamp } returns flowOf(null)
        val prefsRepo = mockk<UserPreferencesRepository>(relaxed = true)
        every { prefsRepo.bmPromptIntervalHours } returns flowOf(24)
        
        val viewModel = MealViewModel(mealRepo, bmRepo, weightRepo, prefsRepo)

        composeTestRule.setContent {
            MealDiaryApp(viewModel)
        }

        // Verify prompt is visible
        composeTestRule.onNodeWithText("Have you had a bowel movement in the last 24h?").assertIsDisplayed()
        
        // Click Yes
        composeTestRule.onNodeWithText("Yes, Log now").performClick()
    }
}
