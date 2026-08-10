package ch.schmidlins.mealdiary.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ch.schmidlins.mealdiary.DataOverviewScreen
import ch.schmidlins.mealdiary.MealDiaryApp
import ch.schmidlins.mealdiary.data.entities.Meal
import ch.schmidlins.mealdiary.data.repository.BMRepository
import ch.schmidlins.mealdiary.data.repository.MealRepository
import ch.schmidlins.mealdiary.data.repository.UserPreferencesRepository
import ch.schmidlins.mealdiary.data.repository.WeightRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MealLogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mealRepo = mockk<MealRepository>(relaxed = true)
    private val bmRepo = mockk<BMRepository>(relaxed = true)
    private val weightRepo = mockk<WeightRepository>(relaxed = true)
    private val prefsRepo = mockk<UserPreferencesRepository>(relaxed = true)

    @Before
    fun setup() {
        every { mealRepo.allMeals } returns flowOf(emptyList())
        every { mealRepo.firstMealTimestamp } returns flowOf(null)
        every { bmRepo.allBMs } returns flowOf(emptyList())
        every { bmRepo.lastBMTimestamp } returns flowOf(null)
        every { weightRepo.allWeightEntries } returns flowOf(emptyList())
        every { prefsRepo.bmPromptIntervalHours } returns flowOf(24)
        every { prefsRepo.isWeightTrackingEnabled } returns flowOf(false)
        every { prefsRepo.weightSuggestionDismissed } returns flowOf(false)
        every { prefsRepo.isReminderEnabled } returns flowOf(true)
    }

    @Test
    fun testMealLoggingFlow() {
        val viewModel = MealViewModel(mealRepo, bmRepo, weightRepo, prefsRepo)

        composeTestRule.setContent {
            MealDiaryApp(viewModel, onNavigateToOverview = {})
        }

        composeTestRule.onNodeWithText("What did you eat?").performTextInput("Pizza")
        composeTestRule.onNodeWithText("Log Meal").performClick()
        composeTestRule.onNodeWithText("Pizza").assertDoesNotExist()
    }

    @Test
    fun testBMPromptVisibility() {
        val now = System.currentTimeMillis()
        every { mealRepo.firstMealTimestamp } returns flowOf(now - (25 * 60 * 60 * 1000))
        
        val viewModel = MealViewModel(mealRepo, bmRepo, weightRepo, prefsRepo)

        composeTestRule.setContent {
            MealDiaryApp(viewModel, onNavigateToOverview = {})
        }

        composeTestRule.onNodeWithText("Have you had a bowel movement in the last 24h?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Yes, Log now").performClick()
    }

    @Test
    fun testWeightSuggestionVisibility() {
        val now = System.currentTimeMillis()
        every { mealRepo.firstMealTimestamp } returns flowOf(now - (8 * 24 * 60 * 60 * 1000L))

        val viewModel = MealViewModel(mealRepo, bmRepo, weightRepo, prefsRepo)

        composeTestRule.setContent {
            MealDiaryApp(viewModel, onNavigateToOverview = {})
        }

        composeTestRule.onNodeWithText("You've been using MealDiary for a week! Would you like to track your weight as well?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Yes, Enable").performClick()
    }

    @Test
    fun testWeightLoggingFlow() {
        every { prefsRepo.isWeightTrackingEnabled } returns flowOf(true)

        val viewModel = MealViewModel(mealRepo, bmRepo, weightRepo, prefsRepo)

        composeTestRule.setContent {
            MealDiaryApp(viewModel, onNavigateToOverview = {})
        }

        composeTestRule.onNodeWithText("Weight (kg)").performTextInput("75.5")
        composeTestRule.onNodeWithText("Log").performClick()
        composeTestRule.onNodeWithText("75.5").assertDoesNotExist()
    }

    @Test
    fun testNavigationToOverview() {
        val viewModel = MealViewModel(mealRepo, bmRepo, weightRepo, prefsRepo)

        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "main") {
                composable("main") {
                    MealDiaryApp(viewModel, onNavigateToOverview = { navController.navigate("overview") })
                }
                composable("overview") {
                    DataOverviewScreen(viewModel, onBack = { navController.popBackStack() })
                }
            }
        }

        // Click Overview icon (represented by Info icon)
        composeTestRule.onNodeWithContentDescription("Overview").performClick()

        // Verify Overview screen is shown
        composeTestRule.onNodeWithText("Daily History").assertIsDisplayed()
        
        // Click Back
        composeTestRule.onNodeWithContentDescription("Back").performClick()

        // Verify back on main screen
        composeTestRule.onNodeWithText("What did you eat?").assertIsDisplayed()
    }
}
