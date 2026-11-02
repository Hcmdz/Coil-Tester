package org.coiltester.app.ui.detection

import android.content.Context
import org.coiltester.app.platform.HapticService
import org.coiltester.app.platform.SoundService

class FakeHapticService(context: Context) : HapticService(context) {
    override fun triggerDetectionFeedback(type: org.coiltester.core.model.DetectionType) = Unit
    override fun triggerCalibrationFeedback() = Unit
    override fun triggerErrorFeedback() = Unit
}

class FakeSoundService(context: Context) : SoundService(context) {
    override fun playDetectionSound(type: org.coiltester.core.model.DetectionType) = Unit
    override fun playCalibrationSound() = Unit
    override fun playErrorSound() = Unit
}