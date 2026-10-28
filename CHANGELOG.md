# Changelog

All notable changes to CoilTester are documented in this file. Versions follow
[Semantic Versioning](https://semver.org/). Full release history is available on
[GitHub Releases](https://github.com/HcmDz/Coil_Tester/releases).

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
- JaCoCo coverage reporting.

### Security
- Fully offline: no `INTERNET` permission, no accounts, no tracking.
- `allowBackup="false"`; Room DB and DataStore excluded from backup.
- Cleartext traffic disabled; network security config in place.
- R8 full mode + resource shrinking on release builds.

### Build
- AGP 8.7.3, Kotlin 2.1.0, KSP 2.1.0-1.0.29, Gradle 8.13, JDK 17.
- Compose BOM 2024.12.01, Material 3 1.3.1.
- Koin 4.0.0, Room 2.6.1, DataStore 1.1.1, Coroutines 1.9.0.

[Unreleased]: https://github.com/HcmDz/Coil_Tester/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/HcmDz/Coil_Tester/releases/tag/v1.0.0