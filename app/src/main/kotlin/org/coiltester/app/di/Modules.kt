package org.coiltester.app.di

import android.content.Context
import org.coiltester.app.platform.*
import org.coiltester.app.viewmodel.*
import org.coiltester.core.model.*
import org.coiltester.core.repository.*
import org.coiltester.core.usecase.*
import org.coiltester.data.db.CoilTesterDatabase
import org.coiltester.data.db.dao.*
import org.coiltester.data.repository.*
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val coreModule = module {
    single<CalibrationUseCase> { CalibrationUseCase(get(), get()) }
    single<SetSensitivityUseCase> { SetSensitivityUseCase(get()) }
    single<SetLanguageUseCase> { SetLanguageUseCase(get()) }
    single<SetThemeUseCase> { SetThemeUseCase(get()) }
    single<SetAdvancedOptionsUseCase> { SetAdvancedOptionsUseCase(get()) }
    single<SetAcThresholdUseCase> { SetAcThresholdUseCase(get()) }
    single<SetDcThresholdUseCase> { SetDcThresholdUseCase(get()) }
    single<SetFrequencyBandUseCase> { SetFrequencyBandUseCase(get()) }
    single<SetSoundVolumeUseCase> { SetSoundVolumeUseCase(get()) }
    single<ConfigureDetectionUseCase> { ConfigureDetectionUseCase(get(), get()) }
    single<StopDetectionUseCase> { StopDetectionUseCase(get()) }
}

val dataModule = module {
    single<CoilTesterDatabase> { CoilTesterDatabase.getInstance(androidContext()) }
    single<ProductDao> { get<CoilTesterDatabase>().productDao() }
    single<SparePartDao> { get<CoilTesterDatabase>().sparePartDao() }
    single<TroubleshootingDao> { get<CoilTesterDatabase>().troubleshootingDao() }
    single<RefrigerantDao> { get<CoilTesterDatabase>().refrigerantDao() }
    single<CampaignDao> { get<CoilTesterDatabase>().campaignDao() }
    single<DbInfoDao> { get<CoilTesterDatabase>().dbInfoDao() }
    single<ProductRepository> { ProductRepositoryImpl(get(), get(), get()) }
    single<ReferenceRepository> { ReferenceRepositoryImpl(get(), get()) }
    single<androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>> {
        androidx.datastore.preferences.core.PreferenceDataStoreFactory.create(
            produceFile = { java.io.File(androidContext().filesDir, "coil_tester_settings.preferences_pb") }
        )
    }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
}

val platformModule = module {
    single<kotlinx.coroutines.CoroutineScope> { kotlinx.coroutines.MainScope() }
    single<DetectionRepository> { SensorProvider(androidContext()) }
    single<SensorProvider> { SensorProvider(androidContext()) }
    single<PermissionHelper> { PermissionHelper(androidContext() as android.app.Activity) }
    single<HapticService> { HapticService(androidContext()) }
    single<SoundService> { SoundService(androidContext()) }
}

val uiModule = module {
    viewModel { DetectionViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}

fun startKoin(context: Context) {
    try {
        startKoin {
            androidLogger()
            androidContext(context)
            modules(listOf(coreModule, dataModule, platformModule, uiModule))
        }
    } catch (e: IllegalStateException) {
        // Koin already started, ignore
        if (!e.message?.contains("already started")!!) throw e
    }
}
