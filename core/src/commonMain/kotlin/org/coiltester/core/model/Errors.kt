package org.coiltester.core.model

import kotlinx.serialization.Serializable

/**
 * Repository layer errors
 */
@Serializable
sealed interface RepositoryError {
    @Serializable
    data class DbError(val message: String, val cause: String? = null) : RepositoryError

    @Serializable
    data class NotFound(val entity: String, val id: String) : RepositoryError

    @Serializable
    data class SerializationError(val message: String, val cause: String? = null) : RepositoryError

    @Serializable
    object IoError : RepositoryError

    @Serializable
    data class CacheMiss(val key: String) : RepositoryError
}

/**
 * Detection / sensor layer errors
 */
@Serializable
sealed interface DetectionError {
    @Serializable
    data class SensorUnavailable(val reason: String) : DetectionError

    @Serializable
    data class PermissionDenied(val sensor: String) : DetectionError

    @Serializable
    data class CalibrationFailed(val message: String, val cause: String? = null) : DetectionError

    @Serializable
    data class NoiseInterference(val level: Double, val threshold: Double) : DetectionError

    @Serializable
    data class SamplingRateNotSupported(val requested: Int, val supported: List<Int>) : DetectionError

    @Serializable
    object AlreadySampling : DetectionError

    @Serializable
    object NotSampling : DetectionError
}

/**
 * UseCase errors
 */
@Serializable
sealed interface UseCaseError {
    @Serializable
    data class ValidationFailed(val field: String, val message: String) : UseCaseError

    @Serializable
    data class NotInitialized(val component: String) : UseCaseError

    @Serializable
    data class RepositoryErr(val error: RepositoryError) : UseCaseError

    @Serializable
    data class DetectionErr(val error: DetectionError) : UseCaseError
}