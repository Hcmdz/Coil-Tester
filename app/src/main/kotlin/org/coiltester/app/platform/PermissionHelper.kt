package org.coiltester.app.platform

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import org.coiltester.core.model.DetectionError
import org.coiltester.core.model.Result

/**
 * Runtime permission helper for sensor access (simplified)
 */
class PermissionHelper(private val activity: Activity) {
    fun hasSensorPermission(): Boolean = true
    suspend fun requestSensorPermission(): Result<Unit, DetectionError> = Result.Ok(Unit)
    fun canUseMagnetometerAtRate(rateHz: Int): Boolean = true
}