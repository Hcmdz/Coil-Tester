package org.coiltester.app.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.coiltester.core.model.AppSettings
import org.coiltester.core.model.Result
import org.coiltester.core.model.RepositoryError
import org.coiltester.core.repository.SettingsRepository

class FakeSettingsRepository : SettingsRepository {
    private val _settings = MutableStateFlow(AppSettings())
    override val settings: StateFlow<AppSettings> = _settings
    override suspend fun updateSettings(transform: (AppSettings) -> AppSettings): Result<Unit, RepositoryError> {
        _settings.value = transform(_settings.value)
        return Result.Ok(Unit)
    }
    override suspend fun resetCalibration(): Result<Unit, RepositoryError> = Result.Ok(Unit)
}