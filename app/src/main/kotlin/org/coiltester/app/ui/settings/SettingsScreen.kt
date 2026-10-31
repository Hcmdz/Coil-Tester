package org.coiltester.app.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.coiltester.app.BuildConfig
import org.coiltester.app.constants.DetectionConstants
import org.coiltester.app.di.AppRoute
import org.coiltester.app.platform.SoundService
import org.coiltester.app.viewmodel.SettingsIntent
import org.coiltester.app.viewmodel.SettingsViewModel
import org.coiltester.core.model.Sensitivity
import org.coiltester.core.model.ThemeMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import android.content.ActivityNotFoundException

@Suppress("SwallowedException")
fun rememberOpenUrl(context: android.content.Context): (String) -> Unit {
    return { url: String ->
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Intentionally ignored - no browser available to handle the intent
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit = {},
    trailing: @Composable () -> Unit = {},
) {
    val clickable = onClick != {}
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = if (clickable) onClick else ({}),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(Locale.getDefault()),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = DetectionConstants.SECTION_LETTER_SPACING.sp,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 8.dp),
    )
}

@Composable
fun LanguageDialog(
    currentLanguage: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(org.coiltester.app.R.string.settings_language),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(
                    "fr" to stringResource(org.coiltester.app.R.string.lang_french),
                    "en" to stringResource(org.coiltester.app.R.string.lang_english),
                    "ar" to stringResource(org.coiltester.app.R.string.lang_arabic),
                ).forEach { (code, label) ->
                    TextButton(
                        onClick = { onSelect(code) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            if (currentLanguage == code) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "OK",
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    )
}

@Composable
fun SensitivityDialog(
    currentSensitivity: Sensitivity,
    onSelect: (Sensitivity) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(org.coiltester.app.R.string.settings_sensitivity),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(
                    Sensitivity.LOW to stringResource(org.coiltester.app.R.string.sensitivity_low),
                    Sensitivity.MEDIUM to stringResource(org.coiltester.app.R.string.sensitivity_medium),
                    Sensitivity.HIGH to stringResource(org.coiltester.app.R.string.sensitivity_high),
                ).forEach { (sens, label) ->
                    TextButton(
                        onClick = { onSelect(sens) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            if (currentSensitivity == sens) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "OK",
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    )
}

@Composable
fun ThemeDialog(
    currentTheme: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(org.coiltester.app.R.string.settings_theme),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(
                    ThemeMode.LIGHT to stringResource(org.coiltester.app.R.string.theme_light),
                    ThemeMode.DARK to stringResource(org.coiltester.app.R.string.theme_dark),
                    ThemeMode.PURE_BLACK to "Pure Black",
                    ThemeMode.SYSTEM to stringResource(org.coiltester.app.R.string.theme_system),
                ).forEach { (mode, label) ->
                    TextButton(
                        onClick = { onSelect(mode) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            if (currentTheme == mode) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "OK",
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    )
}

@Composable
@Suppress("UnusedParameter")
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigate: (AppRoute) -> Unit,
    onBack: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings = state.settings

    val context = LocalContext.current
    val soundService = remember { SoundService(context) }
    val openUrl = rememberOpenUrl(context)
    LaunchedEffect(settings.soundVolume) {
        soundService.volume = settings.soundVolume
    }
    var previousLanguage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(settings.language) {
        if (previousLanguage != null && previousLanguage != settings.language) {
            (context as? Activity)?.recreate()
        }
        previousLanguage = settings.language
    }

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showNoticesDialog by remember { mutableStateOf(false) }
    var showSensitivityDialog by remember { mutableStateOf(false) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(org.coiltester.app.R.string.settings_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        SettingsSectionHeader(title = stringResource(org.coiltester.app.R.string.settings_general))
        SettingItem(
            title = stringResource(org.coiltester.app.R.string.settings_language),
            subtitle =
                when (settings.language) {
                    "fr" -> stringResource(org.coiltester.app.R.string.lang_french)
                    "en" -> stringResource(org.coiltester.app.R.string.lang_english)
                    "ar" -> stringResource(org.coiltester.app.R.string.lang_arabic)
                    else -> settings.language
                },
            onClick = { showLanguageDialog = true },
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        SettingItem(
            title = stringResource(org.coiltester.app.R.string.settings_theme),
            subtitle =
                when (settings.themeMode) {
                    ThemeMode.LIGHT -> stringResource(org.coiltester.app.R.string.theme_light)
                    ThemeMode.DARK -> stringResource(org.coiltester.app.R.string.theme_dark)
                    ThemeMode.PURE_BLACK -> "Pure Black"
                    ThemeMode.SYSTEM -> stringResource(org.coiltester.app.R.string.theme_system)
                    else -> "Unknown"
                },
            onClick = { showThemeDialog = true },
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSectionHeader(title = stringResource(org.coiltester.app.R.string.settings_feedback))
        SettingItem(
            title = stringResource(org.coiltester.app.R.string.settings_haptic),
            subtitle = stringResource(org.coiltester.app.R.string.settings_haptic_subtitle),
        ) {
            Switch(
                checked = settings.hapticEnabled,
                onCheckedChange = { enabled ->
                    viewModel.sendIntent(SettingsIntent.ToggleHaptic(enabled))
                },
                colors =
                    SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    ),
            )
        }
        SettingItem(
            title = stringResource(org.coiltester.app.R.string.settings_sound),
            subtitle = stringResource(org.coiltester.app.R.string.settings_sound_subtitle),
        ) {
            Switch(
                checked = settings.soundEnabled,
                onCheckedChange = { enabled ->
                    viewModel.sendIntent(SettingsIntent.ToggleSound(enabled))
                },
                colors =
                    SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    ),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSectionHeader(title = stringResource(org.coiltester.app.R.string.settings_sensitivity))
        SettingItem(
            title = stringResource(org.coiltester.app.R.string.settings_distance_guide),
            subtitle = stringResource(org.coiltester.app.R.string.settings_distance_guide_subtitle),
        ) {
            Switch(
                checked = settings.distanceGuideEnabled,
                onCheckedChange = { enabled ->
                    viewModel.sendIntent(SettingsIntent.ToggleDistanceGuide(enabled))
                },
                colors =
                    SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    ),
            )
        }
        SettingItem(
            title = stringResource(org.coiltester.app.R.string.settings_sensitivity),
            subtitle =
                when (settings.sensitivity) {
                    Sensitivity.LOW -> stringResource(org.coiltester.app.R.string.sensitivity_low)
                    Sensitivity.MEDIUM -> stringResource(org.coiltester.app.R.string.sensitivity_medium)
                    Sensitivity.HIGH -> stringResource(org.coiltester.app.R.string.sensitivity_high)
                },
            onClick = { showSensitivityDialog = true },
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSectionHeader(title = stringResource(org.coiltester.app.R.string.settings_advanced_options))
        SettingItem(
            title = stringResource(org.coiltester.app.R.string.settings_advanced_options),
            subtitle = stringResource(org.coiltester.app.R.string.settings_advanced_options_subtitle),
        ) {
            Switch(
                checked = settings.advancedOptionsEnabled,
                onCheckedChange = { enabled ->
                    viewModel.sendIntent(SettingsIntent.SetAdvancedOptions(enabled))
                },
                colors =
                    SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    ),
            )
        }
        if (settings.advancedOptionsEnabled) {
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = settings.acThreshold.toString(),
                        onValueChange = { v ->
                            v.toDoubleOrNull()?.let { viewModel.sendIntent(SettingsIntent.SetAcThreshold(it)) }
                        },
                        label = { Text(stringResource(org.coiltester.app.R.string.settings_ac_threshold)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary,
                            ),
                    )
                    OutlinedTextField(
                        value = settings.dcThreshold.toString(),
                        onValueChange = { v ->
                            v.toDoubleOrNull()?.let { viewModel.sendIntent(SettingsIntent.SetDcThreshold(it)) }
                        },
                        label = { Text(stringResource(org.coiltester.app.R.string.settings_dc_threshold)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary,
                            ),
                    )
                    SettingItem(
                        title = stringResource(org.coiltester.app.R.string.settings_frequency_band),
                        subtitle =
                            if (settings.bandPassFreqHz == 60) {
                                stringResource(org.coiltester.app.R.string.freq_60hz)
                            } else {
                                stringResource(org.coiltester.app.R.string.freq_50hz)
                            },
                        onClick = {
                            viewModel.sendIntent(
                                SettingsIntent.SetFrequencyBand(if (settings.bandPassFreqHz == 50) 60 else 50),
                            )
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "${stringResource(
                            org.coiltester.app.R.string.settings_sound_volume,
                        )} (${(settings.soundVolume * 100).toInt()}%)",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Slider(
                        value = settings.soundVolume,
                        onValueChange = { v -> viewModel.sendIntent(SettingsIntent.SetSoundVolume(v)) },
                        colors =
                            SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSectionHeader(title = stringResource(org.coiltester.app.R.string.settings_calibration))
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column {
                SettingItem(
                    title = stringResource(org.coiltester.app.R.string.settings_calibration),
                    subtitle = stringResource(org.coiltester.app.R.string.settings_reset_calibration_subtitle),
                    onClick = {},
                ) {
                    if (settings.lastCalibrationAt != null) {
                        IconButton(
                            onClick = { viewModel.sendIntent(SettingsIntent.ResetCalibration) },
                            colors =
                                IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                ),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Restore,
                                contentDescription = stringResource(org.coiltester.app.R.string.settings_reset),
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { viewModel.sendIntent(SettingsIntent.Calibrate) },
                            colors =
                                IconButtonDefaults.iconButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary,
                                ),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Build,
                                contentDescription = stringResource(org.coiltester.app.R.string.settings_calibration),
                            )
                        }
                    }
                }
                settings.lastCalibrationAt?.let { time ->
                    SettingItem(
                        title = stringResource(org.coiltester.app.R.string.settings_calibration_last),
                        subtitle = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(time)),
                    )
                }
            }
        }

        if (settings.advancedOptionsEnabled && BuildConfig.DEBUG) {
            Spacer(modifier = Modifier.height(16.dp))
            SettingsSectionHeader(title = "Test Injection")
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column {
                    SettingItem(
                        title = "Inject AC (${DetectionConstants.INJECT_AC_LOW} µT)",
                        subtitle = "Below AC threshold (${DetectionConstants.INJECT_AC_LOW} µT) - circle should stop",
                        onClick = {
                            viewModel.sendIntent(
                                SettingsIntent.InjectTestAc(DetectionConstants.INJECT_AC_LOW),
                            )
                        },
                    )
                    SettingItem(
                        title = "Inject AC (5 µT)",
                        subtitle = "Constant value: now reads 0 AC (no rotation)",
                        onClick = { viewModel.sendIntent(SettingsIntent.InjectTestAc(5.0)) },
                    )
                    SettingItem(
                        title = "Inject DC (1 µT)",
                        subtitle = "Below DC threshold (141 µT) - circle should stop",
                        onClick = { viewModel.sendIntent(SettingsIntent.InjectTestDc(1.0)) },
                    )
                    SettingItem(
                        title = "Inject DC (150 µT)",
                        subtitle = "Above DC threshold (141 µT) - circle should rotate",
                        onClick = {
                            viewModel.sendIntent(
                                SettingsIntent.InjectTestDc(DetectionConstants.INJECT_DC_HIGH),
                            )
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSectionHeader(title = stringResource(org.coiltester.app.R.string.settings_about))
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column {
                SettingItem(
                    title = "Version",
                    subtitle = "1.0.0",
                )
                SettingItem(
                    title = "Author",
                    subtitle = "HcmDZ",
                )
                SettingItem(
                    title = "Licence",
                    subtitle = "GPL-3.0",
                    onClick = {},
                )
                SettingItem(
                    title = "Source",
                    subtitle = "github.com/Hcmdz/Coil-Tester",
                    onClick = { openUrl("https://github.com/Hcmdz/Coil-Tester") },
                )
                SettingItem(
                    title = stringResource(org.coiltester.app.R.string.settings_privacy),
                    subtitle = "hcmdz.github.io/coil-tester/privacy/",
                    onClick = { openUrl("https://hcmdz.github.io/coil-tester/privacy/") },
                )
                SettingItem(
                    title = stringResource(org.coiltester.app.R.string.settings_terms),
                    subtitle = "hcmdz.github.io/coil-tester/terms/",
                    onClick = { openUrl("https://hcmdz.github.io/coil-tester/terms/") },
                )
                SettingItem(
                    title = stringResource(org.coiltester.app.R.string.settings_open_source_notices),
                    subtitle = "Apache-2.0",
                    onClick = { showNoticesDialog = true },
                )
            }
        }

        if (showLanguageDialog) {
            LanguageDialog(
                currentLanguage = settings.language,
                onSelect = { code ->
                    viewModel.sendIntent(SettingsIntent.SetLanguage(code))
                    showLanguageDialog = false
                },
                onDismiss = { showLanguageDialog = false },
            )
        }

        if (showThemeDialog) {
            ThemeDialog(
                currentTheme = settings.themeMode,
                onSelect = { mode ->
                    viewModel.sendIntent(SettingsIntent.SetTheme(mode))
                    showThemeDialog = false
                },
                onDismiss = { showThemeDialog = false },
            )
        }

        if (showSensitivityDialog) {
            SensitivityDialog(
                currentSensitivity = settings.sensitivity,
                onSelect = { s ->
                    viewModel.sendIntent(SettingsIntent.SetSensitivity(s))
                    showSensitivityDialog = false
                },
                onDismiss = { showSensitivityDialog = false },
            )
        }

        if (showNoticesDialog) {
            val notices = remember {
                context.resources.openRawResource(org.coiltester.app.R.raw.apache_2_0)
                    .bufferedReader().use { it.readText() }
            }
            AlertDialog(
                onDismissRequest = { showNoticesDialog = false },
                title = { Text(stringResource(org.coiltester.app.R.string.settings_open_source_notices)) },
                text = {
                    Text(
                        notices,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showNoticesDialog = false }) {
                        Text(stringResource(org.coiltester.app.R.string.settings_open_source_notices_close))
                    }
                },
            )
        }
    }
}
