package org.coiltester.core.sensor

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.coiltester.core.model.*
import org.coiltester.core.repository.DetectionRepository

/**
 * Pure Kotlin detection engine - no Android dependencies.
 * Implements band-pass filtering for 50/60Hz AC detection and DC offset detection.
 */
class DetectionEngine(
    private val config: DetectionConfig,
    private val calibration: CalibrationData
) {
    /** Current detection state */
    private val _state = MutableStateFlow(DetectionResult(
        status = DetectionStatus.IDLE,
        fieldReading = null,
        detectionType = null,
        confidence = 0.0,
        config = config,
        calibration = calibration,
        error = null
    ))

    val state: StateFlow<DetectionResult> = _state

    /** Rolling buffer for AC component detection */
    private val recentReadings = mutableListOf<MagneticFieldReading>()
    private val bufferSize = 100 // ~2 seconds at 50Hz

    fun onSensorReading(reading: MagneticFieldReading) {
        val calibrated = applyCalibration(reading)
        recentReadings.add(calibrated)
        if (recentReadings.size > bufferSize) {
            recentReadings.removeAt(0)
        }
        processReading(calibrated)
    }

    private fun applyCalibration(reading: MagneticFieldReading): MagneticFieldReading {
        return MagneticFieldReading(
            x = (reading.x - calibration.offsetX) * calibration.scaleFactor,
            y = (reading.y - calibration.offsetY) * calibration.scaleFactor,
            z = (reading.z - calibration.offsetZ) * calibration.scaleFactor,
            timestamp = reading.timestamp
        )
    }

    private fun processReading(reading: MagneticFieldReading) {
        if (recentReadings.size < 20) {
            // Not enough data yet
            _state.value = DetectionResult(
                status = DetectionStatus.DETECTING,
                fieldReading = reading,
                detectionType = null,
                confidence = 0.0,
                config = config,
                calibration = calibration,
                error = null
            )
            return
        }

        val acRms = extractACRms(recentReadings)
        val dcMean = extractDCMean(recentReadings)

        val (detectedType, confidence) = when {
            acRms > config.acThreshold && dcMean < config.dcThreshold ->
                DetectionType.AC to calculateConfidence(acRms, config.acThreshold)
            dcMean > config.dcThreshold && acRms < config.acThreshold ->
                DetectionType.DC to calculateConfidence(dcMean, config.dcThreshold)
            acRms > config.acThreshold && dcMean > config.dcThreshold ->
                // Both AC and DC present - pick dominant
                if (acRms > dcMean) DetectionType.AC to calculateConfidence(acRms, config.acThreshold)
                else DetectionType.DC to calculateConfidence(dcMean, config.dcThreshold)
            else -> null to 0.0
        }

        val newStatus = when (detectedType) {
            DetectionType.AC -> DetectionStatus.CONFIRMED_AC
            DetectionType.DC -> DetectionStatus.CONFIRMED_DC
            null -> DetectionStatus.DETECTING
        }

        _state.value = DetectionResult(
            status = newStatus,
            fieldReading = reading,
            detectionType = detectedType,
            confidence = confidence,
            config = config,
            calibration = calibration,
            error = null,
            acRms = acRms,
            dcMean = dcMean
        )
    }

    /**
     * Extract AC component using a band-pass filter centered at 50/60Hz.
     */
    private fun extractACRms(readings: List<MagneticFieldReading>): Double {
        if (readings.size < 10) return 0.0

        val magnitudes = readings.map { it.magnitude }
        val sampleRate = config.sampleRateHz.toDouble()
        val centerFreq = config.bandPassFreqHz.toDouble()

        // A band-pass at or above Nyquist is not representable: the coefficients diverge and
        // every reading would read as AC. No measurable energy is the honest answer.
        if (centerFreq <= 0.0 || centerFreq >= sampleRate / 2.0) return 0.0

        val w0 = 2.0 * kotlin.math.PI * centerFreq / sampleRate
        val bw = 10.0

        // Constant 0 dB peak gain form: sin(w0) * sinh(...) divides by sin(w0), which is zero
        // when the centre frequency sits on 0 Hz or Nyquist and yields NaN for every reading.
        val alpha = kotlin.math.sin(w0) * bw / (2.0 * centerFreq)
        val cosw0 = kotlin.math.cos(w0)

        val b0 = alpha
        val b2 = -alpha
        val a0 = 1.0 + alpha
        val a1 = -2.0 * cosw0
        val a2 = 1.0 - alpha

        val filtered = DoubleArray(magnitudes.size)
        var px1 = 0.0; var px2 = 0.0
        var py1 = 0.0; var py2 = 0.0

        for (i in magnitudes.indices) {
            val cx = magnitudes[i]
            val cy = (b0 * cx + b2 * px2 - a1 * py1 - a2 * py2) / a0
            filtered[i] = cy
            px2 = px1; px1 = cx
            py2 = py1; py1 = cy
        }

        // The filter restarts from zero state on every call, so the buffer's first sample is a
        // step of the whole DC level: its transient would be measured as AC forever. Only the
        // settled second half carries signal.
        val settled = filtered.drop(filtered.size / 2)
        val sumSquares = settled.sumOf { it * it }
        val rms = kotlin.math.sqrt(sumSquares / settled.size)
        return if (rms.isFinite()) rms else 0.0
    }

    private fun extractDCMean(readings: List<MagneticFieldReading>): Double {
        if (readings.isEmpty()) return 0.0
        return readings.map { it.magnitude }.average()
    }

    private fun calculateConfidence(value: Double, threshold: Double): Double {
        if (threshold <= 0) return 0.0
        return kotlin.math.min(1.0, (value / threshold) * 0.8 + 0.2)
    }

    fun updateConfig(newConfig: DetectionConfig) {
        // Config changes apply to next readings
    }

    fun reset() {
        recentReadings.clear()
        _state.value = DetectionResult(
            status = DetectionStatus.IDLE,
            fieldReading = null,
            detectionType = null,
            confidence = 0.0,
            config = config,
            calibration = calibration,
            error = null
        )
    }
}

/**
 * Signal processing utilities
 */
object SignalProcessing {
    fun movingAverage(samples: List<Double>, window: Int): List<Double> {
        if (samples.size < window) return samples
        return samples.windowed(window).map { it.average() }
    }

    fun bandPass(samples: List<Double>, sampleRateHz: Int, centerFreqHz: Int, bandwidthHz: Int): Any {
        if (samples.size < 10) return samples
        // Above Nyquist the recursive coefficients diverge; the signal cannot be band-limited.
        if (centerFreqHz <= 0 || centerFreqHz >= sampleRateHz / 2) return samples

        val w0 = 2.0 * kotlin.math.PI * centerFreqHz / sampleRateHz
        // sin(w0) is 0 at 0 Hz and at Nyquist, which made the sinh form produce NaN.
        val alpha = kotlin.math.sin(w0) * bandwidthHz / (2.0 * centerFreqHz)
        val cosw0 = kotlin.math.cos(w0)

        val a0 = 1.0 + alpha
        val a1 = -2.0 * cosw0
        val a2 = 1.0 - alpha
        val b0 = alpha
        val b2 = -alpha

        val filtered = DoubleArray(samples.size)
        var prevX1 = 0.0; var prevX2 = 0.0
        var prevY1 = 0.0; var prevY2 = 0.0

        for (i in samples.indices) {
            val currX = samples[i]
            val currY = (b0 * currX + b2 * prevX2 - a1 * prevY1 - a2 * prevY2) / a0
            filtered[i] = currY
            prevX2 = prevX1; prevX1 = currX
            prevY2 = prevY1; prevY1 = currY
        }

        return filtered.toList()
    }

    fun detectMainsFrequency(samples: List<Double>, sampleRateHz: Int): Pair<Int, Double> {
        val energy50 = computeEnergyAtFrequency(samples, sampleRateHz, 50)
        val energy60 = computeEnergyAtFrequency(samples, sampleRateHz, 60)
        return if (energy50 >= energy60) 50 to energy50 else 60 to energy60
    }

    private fun computeEnergyAtFrequency(samples: List<Double>, sampleRateHz: Int, freqHz: Int): Double {
        val n = samples.size
        val k = (n * freqHz / sampleRateHz).toInt()
        val w = 2.0 * kotlin.math.PI * k / n
        val cosw = kotlin.math.cos(w)
        val coeff = 2.0 * cosw

        var s0 = 0.0
        var s1 = 0.0
        var s2 = 0.0

        for (i in 0 until n) {
            s0 = samples[i] + coeff * s1 - s2
            s2 = s1
            s1 = s0
        }

        val power = s1 * s1 + s2 * s2 - coeff * s1 * s2
        return kotlin.math.sqrt(power) / n
    }

    fun rms(samples: List<Double>): Double {
        if (samples.isEmpty()) return 0.0
        val sumSquares = samples.map { it * it }.sum()
        return kotlin.math.sqrt(sumSquares / samples.size)
    }

    fun mean(samples: List<Double>): Double {
        if (samples.isEmpty()) return 0.0
        return samples.average()
    }

    fun variance(samples: List<Double>): Double {
        if (samples.size < 2) return 0.0
        val m = samples.average()
        return samples.map { (it - m) * (it - m) }.average()
    }
}
