package ch.schmidlins.mealdiary.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import ch.schmidlins.mealdiary.MealDiaryApp
import ch.schmidlins.mealdiary.data.entities.Meal
import ch.schmidlins.mealdiary.data.repository.BMRepository
import ch.schmidlins.mealdiary.data.repository.MealRepository
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
        every { mealRepo.allMeals } returns flowOf(emptyList<Meal>())
        every { bmRepo.allBMs } returns flowOf(emptyList())
        
        val viewModel = MealViewModel(mealRepo, bmRepo)

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
}
