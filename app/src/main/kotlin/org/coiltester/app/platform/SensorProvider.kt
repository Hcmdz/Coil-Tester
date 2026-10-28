package org.coiltester.app.platform

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.runBlocking
import org.coiltester.core.model.*
import org.coiltester.core.model.DetectionError
import org.coiltester.core.repository.DetectionRepository

/**
 * Android SensorManager wrapper for magnetometer.
 * Provides real-time magnetic field readings via Flow.
 */
class SensorProvider(private val context: Context) : DetectionRepository {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private var isSampling = false
    private var currentConfig: DetectionConfig? = null
    private var calibration: CalibrationData = CalibrationData()
    private var listener: SensorEventListener? = null
    private var currentAccuracy: Int = 2
    private var emitReading: ((Result<MagneticFieldReading, DetectionError>) -> Unit)? = null

    // Test injection channel
    private val testInjectionChannel = Channel<MagneticFieldReading>(Channel.UNLIMITED)
    private var testMode = false

    private fun ensureListener(): SensorEventListener {
        listener?.let { return it }
        val sl =
            object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    event?.let {
                        val reading =
                            MagneticFieldReading(
                                x = it.values[0].toDouble(),
                                y = it.values[1].toDouble(),
                                z = it.values[2].toDouble(),
                                timestamp = System.currentTimeMillis(),
                                accuracy = currentAccuracy,
                            )
                        val cal = calibration
                        val calibrated =
                            MagneticFieldReading(
                                x = (reading.x - cal.offsetX) * cal.scaleFactor,
                                y = (reading.y - cal.offsetY) * cal.scaleFactor,
                                z = (reading.z - cal.offsetZ) * cal.scaleFactor,
                                timestamp = reading.timestamp,
                                accuracy = currentAccuracy,
                            )
                        emitReading?.invoke(Result.Ok(calibrated))
                    }
                }

                override fun onAccuracyChanged(
                    sensor: Sensor?,
                    accuracy: Int,
                ) {
                    currentAccuracy = accuracy
                }
            }
        listener = sl
        return sl
    }

    override val magneticField: Flow<Result<MagneticFieldReading, DetectionError>> =
        callbackFlow {
            if (magnetometer == null) {
                trySend(Result.Err(DetectionError.SensorUnavailable("No magnetometer hardware")))
                close()
                return@callbackFlow
            }
            emitReading = { trySend(it) }
            ensureListener()

            // Merge test injection channel with sensor readings
            val testFlow: Flow<Result<MagneticFieldReading, DetectionError>> =
                callbackFlow {
                    for (reading in testInjectionChannel) {
                        trySend(Result.Ok(reading))
                    }
                }.onCompletion {
                        cause ->
                    if (cause != null) trySend(Result.Err(DetectionError.SensorUnavailable("Test injection failed: " + cause.message)))
                }

            val sensorFlow =
                callbackFlow {
                    emitReading = { trySend(it) }
                    ensureListener()
                    awaitClose {
                        emitReading = null
                        listener?.let { sensorManager.unregisterListener(it) }
                    }
                }

            merge(sensorFlow, testFlow)
                .collect { trySend(it) }

            awaitClose {
                emitReading = null
                listener?.let { sensorManager.unregisterListener(it) }
            }
        }

    // Test injection - inject readings that will be processed by the detection engine
    override fun injectTestReading(
        acMagnitude: Double,
        dcMagnitude: Double,
    ) {
        if (acMagnitude > 0.0) {
            // Inject an oscillating 50 Hz signal so the band-pass can measure real AC.
            // 100 samples at 5 ms intervals = 500 ms = 25 cycles of 50 Hz.
            val baseDc = 48.76 // Earth field baseline so the magnitude stays positive
            for (i in 0 until 100) {
                val acComponent = acMagnitude * kotlin.math.sin(2 * Math.PI * 50 * i / 200.0)
                val reading =
                    MagneticFieldReading(
                        x = baseDc + acComponent,
                        y = 0.0,
                        z = 0.0,
                        timestamp = System.currentTimeMillis() + i * 5L,
                        accuracy = currentAccuracy,
                    )
                runBlocking { testInjectionChannel.send(reading) }
            }
        } else {
            // Inject a constant DC field.
            runBlocking {
                testInjectionChannel.send(
                    MagneticFieldReading(
                        x = 0.0,
                        y = dcMagnitude,
                        z = 0.0,
                        timestamp = System.currentTimeMillis(),
                        accuracy = currentAccuracy,
                    ),
                )
            }
        }
    }

    override fun setTestMode(enabled: Boolean) {
        testMode = enabled
    }

    override fun isSensorAvailable(): Boolean = magnetometer != null

    override fun getSupportedSamplingRates(): List<Int> {
        // 200 Hz is the smallest offered rate that can resolve a 50 Hz mains band.
        return listOf(10, 20, 50, 64, 100, 200)
    }

    override fun startSampling(config: DetectionConfig): Result<Unit, DetectionError> {
        return if (magnetometer == null) {
            Result.Err(DetectionError.SensorUnavailable("No magnetometer hardware"))
        } else if (!getSupportedSamplingRates().contains(config.sampleRateHz)) {
            Result.Err(DetectionError.SamplingRateNotSupported(config.sampleRateHz, getSupportedSamplingRates()))
        } else if (isSampling) {
            Result.Err(DetectionError.AlreadySampling)
        } else {
            val sl = ensureListener()
            val rateUs = (1_000_000 / config.sampleRateHz)
            sensorManager.registerListener(sl, magnetometer!!, rateUs)
            currentConfig = config
            isSampling = true
            Result.Ok(Unit)
        }
    }

    override fun stopSampling(): Result<Unit, DetectionError> {
        if (!isSampling) {
            return Result.Err(DetectionError.NotSampling)
        }
        listener?.let { sensorManager.unregisterListener(it) }
        isSampling = false
        currentConfig = null
        return Result.Ok(Unit)
    }

    override fun calibrate(): Result<CalibrationData, DetectionError> {
        // Simple calibration: capture 100 samples and compute offset
        // In real impl, this would collect from the flow
        calibration =
            CalibrationData(
                offsetX = 0.0,
                offsetY = 0.0,
                offsetZ = 0.0,
                scaleFactor = 1.0,
                calibratedAt = System.currentTimeMillis(),
            )
        return Result.Ok(calibration)
    }
}
