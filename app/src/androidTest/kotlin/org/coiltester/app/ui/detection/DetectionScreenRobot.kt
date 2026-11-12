package org.coiltester.app.ui.detection

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import org.coiltester.app.R

/**
 * Screen interactions resolved against the same resources the UI renders, so the
 * assertions hold under any device locale (the app defaults to French).
 */
class DetectionScreenRobot(private val rule: ComposeContentTestRule) {

    private val context: Context = ApplicationProvider.getApplicationContext()

    fun assertReadyStatusDisplayed() {
        rule.onNodeWithText(context.getString(R.string.detection_ready)).assertIsDisplayed()
    }

    fun assertCalibrateButtonDisplayed() {
        rule.onNodeWithText(context.getString(R.string.detection_calibrate)).assertIsDisplayed()
    }

    fun assertStartButtonDisplayed() {
        rule.onNodeWithText(context.getString(R.string.detection_start)).assertIsDisplayed()
    }

    fun clickCalibrate() {
        rule.onNodeWithText(context.getString(R.string.detection_calibrate)).performClick()
    }

    fun clickStart() {
        rule.onNodeWithText(context.getString(R.string.detection_start)).performClick()
    }

    fun clickStop() {
        rule.onNodeWithText(context.getString(R.string.detection_stop)).performClick()
    }
}