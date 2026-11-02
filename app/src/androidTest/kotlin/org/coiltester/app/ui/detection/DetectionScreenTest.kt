package org.coiltester.app.ui.detection

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.coiltester.app.viewmodel.DetectionViewModel
import org.coiltester.core.usecase.CalibrationUseCase
import org.coiltester.core.usecase.ConfigureDetectionUseCase
import org.coiltester.core.usecase.SetSensitivityUseCase
import org.coiltester.core.usecase.StopDetectionUseCase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DetectionScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeDetectionRepo = FakeDetectionRepository()
    private val fakeSettingsRepo = org.coiltester.app.viewmodel.FakeSettingsRepository()
    private val context: Context = ApplicationProvider.getApplicationContext()

    private val viewModel by lazy {
        DetectionViewModel(
            detectionRepository = fakeDetectionRepo,
            configureDetectionUseCase = ConfigureDetectionUseCase(fakeDetectionRepo, fakeSettingsRepo),
            stopDetectionUseCase = StopDetectionUseCase(fakeDetectionRepo),
            calibrationUseCase = CalibrationUseCase(fakeDetectionRepo, fakeSettingsRepo),
            setSensitivityUseCase = SetSensitivityUseCase(fakeSettingsRepo),
            soundService = FakeSoundService(context),
            hapticService = FakeHapticService(context),
            settingsRepository = fakeSettingsRepo,
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
        )
    }

    @Test
    fun detectionScreen_showsReadyStatus() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            DetectionScreen(viewModel = viewModel, onNavigate = {})
        }
        composeTestRule.mainClock.advanceTimeBy(1000)
        val robot = DetectionScreenRobot(composeTestRule)
        robot.assertReadyStatusDisplayed()
    }

    @Test
    fun detectionScreen_showsCalibrateButton() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            DetectionScreen(viewModel = viewModel, onNavigate = {})
        }
        composeTestRule.mainClock.advanceTimeBy(1000)
        val robot = DetectionScreenRobot(composeTestRule)
        robot.assertCalibrateButtonDisplayed()
    }

    @Test
    fun detectionScreen_showsStartButton() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            DetectionScreen(viewModel = viewModel, onNavigate = {})
        }
        composeTestRule.mainClock.advanceTimeBy(1000)
        val robot = DetectionScreenRobot(composeTestRule)
        robot.assertStartButtonDisplayed()
    }
}