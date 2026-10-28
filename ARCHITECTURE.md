# Architecture — CoilTester

This document describes the architecture of CoilTester. It complements
[README.md](README.md) and [AGENTS.md](AGENTS.md).

## 1. Overview

CoilTester is a multi-module Android application following Clean Architecture
with an MVI presentation layer. The domain layer is pure Kotlin (KMP-ready),
the data layer provides Room + DataStore implementations, and the UI layer is
Jetpack Compose + Material 3.

The app is fully offline: no `INTERNET` permission, no network layer.

## 2. Modules

```
CoilTester/
├── :core            # Pure Kotlin (commonMain + jvmMain + androidMain)
├── :data            # Implementations (commonMain + androidMain + Room)
├── :app             # Android app (Compose UI, ViewModels, DI)
└── :build-logic:convention  # Gradle convention plugins
```

### :core (pure Kotlin, no Android SDK)

- **model/** — entities (`MagneticFieldReading`, `DetectionConfig`, `AppSettings`,
  `CalibrationData`), `Result<T, E>` typed errors, sealed error classes.
- **repository/** — repository interfaces (`DetectionRepository`, `SettingsRepository`).
- **usecase/** — single-responsibility use cases with `invoke()` operator.
- **sensor/** — `DetectionEngine` (pure Kotlin, no Android deps).

Constraint: `:core` never depends on the Android SDK. Testable on JVM.

### :data (KMP + Room)

- **db/** — Room entities, DAOs, `CoilTesterDatabase`, type converters.
- **prefs/** — DataStore (Proto) for settings.
- **repository/** — repository implementations (Room + DataStore).
- **embedded/** — build-time DB generator (CSV → `assets/databases/`).

Depends on `:core`. Implements repository interfaces.

### :app (Android)

- **ui/** — Compose M3 screens, theme, navigation, components.
- **viewmodel/** — MVI ViewModels (`StateFlow<State>`, sealed `Intent`).
- **di/** — Koin modules (`core`, `data`, `platform`, `ui`).
- **platform/** — `SensorProvider`, `HapticService`, `SoundService`, `PermissionHelper`.

Depends on `:core` + `:data`.

## 3. Architecture patterns

### Clean Architecture

Dependencies point inward: `:app` → `:data` → `:core`. `:core` has no
outward dependencies. Repository interfaces live in `:core`; implementations
in `:data`.

### MVI (Model-View-Intent)

- Each screen has a `ViewModel` holding `StateFlow<State>`.
- User actions are modeled as a sealed `Intent` class.
- The ViewModel reduces intents into new state via `update { }`.
- UI collects state with `collectAsStateWithLifecycle()`.

Example flow:

```
User tap → Intent → ViewModel.reduce → StateFlow<State> → Compose recomposition
```

### Typed errors

All fallible operations return `Result<T, E>` (sealed). Exceptions are reserved
for programmer errors. Cancellation is never swallowed (coroutine `finally`
blocks for cleanup).

### Dependency injection

Koin 4.0.0. Modules are split by concern:
- `coreModule` — use cases, domain services.
- `dataModule` — repositories, Room database, DataStore.
- `platformModule` — sensor, haptic, sound, permissions.
- `uiModule` — ViewModels, navigation.

## 4. Data flow

```
SensorManager (Android)
   ↓
SensorProvider (DetectionRepository impl)
   ↓ Flow<Result<MagneticFieldReading, DetectionError>>
DetectionEngine (core, pure Kotlin)
   ↓
DetectionViewModel (MVI, StateFlow<DetectionState>)
   ↓
Compose UI (collectAsStateWithLifecycle)
```

Settings flow:

```
DataStore (Proto) ←→ SettingsRepository ←→ SettingsViewModel ←→ SettingsScreen
```

## 5. Database

- **Room** 2.6.1, `exportSchema = true`, schemas in `data/schemas/`.
- Pre-packaged database via `createFromAsset()` (`app/src/main/assets/databases/`).
- Migrations are additive only (never edit a shipped migration).
- DAOs use `suspend` functions and `Flow` for reactive queries.

## 6. UI / Compose

- Jetpack Compose + Material 3 (BOM 2024.12.01).
- Material You dynamic color (light/dark/pure-black AMOLED).
- Adaptive layouts for phone and tablet.
- Edge-to-edge with `WindowInsets`.
- `FLAG_SECURE` on the detection screen.
- Trilingual: `values/` (FR default), `values-en/`, `values-ar/` (RTL, `supportsRtl=true`).

### Compose stability

UI state classes (`DetectionState`, `SettingsState`, `LanguageOption`) are
annotated `@Immutable`. Core classes that cannot depend on Compose runtime are
listed in `app/compose-stability.conf`. Compose compiler reports and metrics
are emitted to `app/build/compose_compiler/`.

## 7. Concurrency

- Kotlin Coroutines + `StateFlow` / `SharedFlow`.
- `SupervisorJob` for scoped failure isolation.
- `UnconfinedTestDispatcher` in tests (JUnit5 + Turbine).

## 8. Testing

| Layer | Framework |
|---|---|
| Domain (`:core`) | JUnit5 + Turbine + AssertK |
| Data (`:data`) | Room Testing + Robolectric + Truth |
| UI (`:app`) | Compose UI Test + Robot pattern |
| Coverage | JaCoCo (`:app:jacocoTestReport`) |

## 9. Quality gates

- **Detekt** 1.23.7 — config `config/detekt/detekt.yml`, baseline `app/detekt-baseline.xml`.
- **Ktlint** 12.2.0 — applied to `:app`.
- **JaCoCo** 0.8.12 — HTML/XML reports.
- CI: `.github/workflows/ci.yml` runs `test`, `lint`, `detekt`, `ktlintCheck`, `assembleDebug`.

## 10. Security

- No `INTERNET` permission; fully offline.
- `android:usesCleartextTraffic="false"`, network security config.
- `allowBackup="false"`; data extraction rules exclude DB and DataStore.
- R8 full mode + resource shrinking on release.
- Encrypted DataStore (Proto) for settings.

## 11. Build & release

- Versioning driven by `version.properties` (CI runs `updateVersionProperties`).
- Release signing via GitHub Secrets (`KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`).
- Release workflow (`.github/workflows/release.yml`) builds signed AAB + APK on `v*` tag push.
- ProGuard rules in `app/proguard-rules.pro`.

## 12. Conventions

- English only in code, comments, KDoc, commits, docs.
- Conventional Commits (`feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`).
- Version tags: `v<major>.<minor>.<patch>`.
- No AI/tool attribution in shipped content.