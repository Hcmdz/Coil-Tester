package org.coiltester.app.ui.detection

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick

class DetectionScreenRobot(private val rule: ComposeContentTestRule) {

    fun assertReadyStatusDisplayed() {
        rule.onNodeWithText("Ready").assertIsDisplayed()
    }

    fun assertCalibrateButtonDisplayed() {
        rule.onNodeWithText("Calibrate").assertIsDisplayed()
    }

    fun assertStartButtonDisplayed() {
        rule.onNodeWithText("Start").assertIsDisplayed()
    }

    fun clickCalibrate() {
        rule.onNodeWithText("Calibrate").performClick()
    }

    fun clickStart() {
        rule.onNodeWithText("Start").performClick()
    }

    fun clickStop() {
        rule.onNodeWithText("Stop").performClick()
    }
}