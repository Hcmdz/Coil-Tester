package org.coiltester.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class DetectionConfigTest {

    @Test
    @DisplayName("fromSettings keeps the sensitivity defaults while advanced options are off")
    fun ignoresCustomThresholdsWhenAdvancedOff() {
        val settings = AppSettings(
            sensitivity = Sensitivity.MEDIUM,
            advancedOptionsEnabled = false,
            acThreshold = 3.0,
            dcThreshold = 188.0
        )

        val config = DetectionConfig.fromSettings(settings)

        assertEquals(0.5, config.acThreshold, 0.001)
        assertEquals(2.0, config.dcThreshold, 0.001)
    }

    @Test
    @DisplayName("fromSettings applies the stored thresholds once advanced options are on")
    fun appliesCustomThresholdsWhenAdvancedOn() {
        val settings = AppSettings(
            sensitivity = Sensitivity.MEDIUM,
            advancedOptionsEnabled = true,
            acThreshold = 3.0,
            dcThreshold = 188.0
        )

        val config = DetectionConfig.fromSettings(settings)

        assertEquals(3.0, config.acThreshold, 0.001)
        assertEquals(188.0, config.dcThreshold, 0.001)
    }

    @Test
    @DisplayName("Default sampling rate resolves the mains band")
    fun defaultSampleRateResolvesMainsBand() {
        val config = DetectionConfig()

        // A band-pass at or above Nyquist is discarded, so the default must sample faster.
        assertTrue(config.sampleRateHz / 2.0 > config.bandPassFreqHz)
    }
}
