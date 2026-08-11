package ch.schmidlins.mealdiary.ui.settings

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.schmidlins.mealdiary.data.repository.UserPreferencesRepository
import ch.schmidlins.mealdiary.ui.MealViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class SettingsActivityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<MealViewModel>(relaxed = true)
    private val prefsRepo = mockk<UserPreferencesRepository>(relaxed = true)

    @Before
    fun setup() {
        every { prefsRepo.bmPromptIntervalHours } returns flowOf(24)
        every { prefsRepo.isReminderEnabled } returns flowOf(true)
        every { prefsRepo.isWeightTrackingEnabled } returns flowOf(false)
    }

    @Test
    fun testExportButtonExists() {
        composeTestRule.setContent {
            SettingsScreen(prefsRepo, viewModel)
        }
        
        composeTestRule.onNodeWithText("Export Data to CSV").assertIsDisplayed()
    }
}
