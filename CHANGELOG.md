# Changelog

All notable changes to CoilTester are documented in this file. Versions follow
[Semantic Versioning](https://semver.org/). Full release history is available on
[GitHub Releases](https://github.com/Hcmdz/Coil-Tester/releases).

## [Unreleased]

### Changed
- Nothing yet.

## [1.0.0] - 2026-10-28

### Added
- Initial release of CoilTester: AC/DC coil detection via the device magnetometer.
- Real-time magnetic field readings with configurable sensitivity (Low / Medium / High).
- Haptic and audio feedback on detection, with adjustable volume.
- Calibration flow with stored offset/scale data and reset option.
- Distance guide (10 cm target) to position the sensor over the coil.
- Advanced options: custom AC/DC thresholds and mains frequency band (50 / 60 Hz).
- Trilingual UI: French (default), English, Arabic with full RTL support.
- Theme support: Light, Dark, Pure Black (AMOLED), or follow system.
- `FLAG_SECURE` on the detection screen to prevent screenshots/screen recording.
- Multi-module Clean Architecture + MVI: `:app`, `:core` (KMP), `:data`, `:build-logic:convention`.
- Detekt 1.23.7 and Ktlint 12.2.0 quality gates in CI.
- JaCoCo coverage reporting (`./gradlew :app:jacocoTestReport`).
- Secret-scanning gate (gitleaks + sensitive-filenames) in CI (`validate.yml`).
- SECURITY.md, CONTRIBUTING.md, CODE_OF_CONDUCT.md, THIRD_PARTY.md, LICENSE (GPL-3.0).

### Changed
- Dependency upgrade (Phase 1): AGP 8.5.0 → 8.7.3, Kotlin 2.0.0 → 2.1.0,
  KSP 2.0.0-1.0.24 → 2.1.0-1.0.29, Gradle 8.8 → 8.13.
- Compose BOM 2024.08.00 → 2024.12.01, Material 3 1.3.0 → 1.3.1.
- Coroutines 1.8.1 → 1.9.0, Serialization 1.6.3 → 1.7.3.
- Lifecycle 2.8.0 → 2.8.7, Activity Compose 1.9.0 → 1.9.3, Navigation 2.8.0 → 2.8.5.
- JUnit5 5.10.2 → 5.11.3, Turbine 1.0.0 → 1.2.0, Robolectric 4.11 → 4.14.1.
- Compose UI Test 1.5.1 → 1.7.5, JaCoCo 0.8.11 → 0.8.12, Detekt 1.23.4 → 1.23.7.
- Ktlint plugin 1.2.1 → 12.2.0.
- compileSdk / targetSdk 34 → 35.
- `collectAsState` → `collectAsStateWithLifecycle` (Theme, Settings, Navigation, Detection).
- Compose stability: `DetectionState`, `SettingsState`, `LanguageOption` annotated `@Immutable`;
  `compose-stability.conf` for core classes.
- README.md and LICENSE added.

### Security
- Fully offline: no `INTERNET` permission, no accounts, no tracking.
- `allowBackup="false"`; Room DB and DataStore excluded from backup.
- Cleartext traffic disabled; network security config in place.
- R8 full mode + resource shrinking on release builds.
- `FLAG_SECURE` on the detection screen.
- Gitleaks pre-push gate for secrets GitHub push protection misses.

### Build
- AGP 8.7.3, Kotlin 2.1.0, KSP 2.1.0-1.0.29, Gradle 8.13, JDK 17.
- Compose BOM 2024.12.01, Material 3 1.3.1.
- Koin 4.0.0, Room 2.6.1, DataStore 1.1.1, Coroutines 1.9.0.

[Unreleased]: https://github.com/Hcmdz/Coil-Tester/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/Hcmdz/Coil-Tester/releases/tag/v1.0.0