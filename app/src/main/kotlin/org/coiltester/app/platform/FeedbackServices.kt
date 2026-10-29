package org.coiltester.app.platform

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator

/**
 * Haptic feedback service (simplified)
 */
class HapticService(private val context: Context) {
    @Volatile
    var enabled: Boolean = true

    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    @Suppress("DEPRECATION", "java/deprecated-call", "UnusedParameter")
    fun triggerDetectionFeedback(type: org.coiltester.core.model.DetectionType) {
        if (!enabled) return
        if (!vibrator.hasVibrator()) return
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
            vibrator.vibrate(effect)
        } else {
            // codeql[java/deprecated-call]
            vibrator.vibrate(50)
        }
    }

    @Suppress("DEPRECATION")
    fun triggerCalibrationFeedback() {
        if (!enabled) return
        if (!vibrator.hasVibrator()) return
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
            vibrator.vibrate(effect)
        } else {
            // codeql[java/deprecated-call]
            vibrator.vibrate(50)
        }
    }

    @Suppress("DEPRECATION")
    fun triggerErrorFeedback() {
        if (!enabled) return
        if (!vibrator.hasVibrator()) return
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100), -1)
            vibrator.vibrate(effect)
        } else {
            // codeql[java/deprecated-call]
            vibrator.vibrate(200)
        }
    }
}

/**
 * Sound feedback service — plays short WAV cues from res/raw/.
 */
class SoundService(private val context: Context) {
    @Volatile
    var enabled: Boolean = true

    @Volatile
    var volume: Float = 1.0f

    @Suppress("TooGenericExceptionCaught")
    private fun play(resId: Int) {
        if (!enabled) return
        try {
            val mp = android.media.MediaPlayer.create(context, resId) ?: return
            mp.setVolume(volume, volume)
            mp.setOnCompletionListener { it.release() }
            mp.setOnErrorListener { player, _, _ ->
                player.release()
                true
            }
            mp.start()
        } catch (e: Exception) {
            android.util.Log.w("SoundService", "play failed: ${e.message}")
        }
    }

    fun playDetectionSound(type: org.coiltester.core.model.DetectionType) {
        val resId =
            when (type) {
                org.coiltester.core.model.DetectionType.AC -> org.coiltester.app.R.raw.detect_ac
                org.coiltester.core.model.DetectionType.DC -> org.coiltester.app.R.raw.detect_dc
            }
        play(resId)
    }

    fun playCalibrationSound() {
        play(org.coiltester.app.R.raw.calibration_done)
    }

    fun playErrorSound() {
        play(org.coiltester.app.R.raw.error)
    }

    fun release() { /* MediaPlayer instances self-release on completion */ }
}
