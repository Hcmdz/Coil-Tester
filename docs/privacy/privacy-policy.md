# Privacy Policy — CoilTester

**Last updated:** 2026-10-28

## Summary

CoilTester is a fully offline Android application. It does not collect, transmit,
store, or share any personal data. There are no user accounts, no analytics, no
advertising SDKs, and no network requests.

## Data handled on device

- Sensor readings (magnetometer) are processed in memory and never persisted
  beyond the current session.
- Settings (sensitivity, language, theme, thresholds, calibration) are stored
  locally in a DataStore on the device and never leave it.
- Calibration data is stored locally and can be reset from Settings.

## Permissions

CoilTester requests only the permissions needed for its core function:

- BODY_SENSORS / HIGH_SAMPLING_RATE_SENSORS — magnetometer access.
- VIBRATE — haptic feedback.
- WAKE_LOCK — keep screen awake during detection.
- FOREGROUND_SERVICE / FOREGROUND_SERVICE_SENSOR — foreground sensor service.

No INTERNET permission is requested. The app cannot make network requests.

## Third parties

No third-party SDKs that collect data are included. See THIRD_PARTY.md.

## Backup

allowBackup is set to false. The Room database and DataStore preferences are
excluded from Android backup.

## Contact

Report privacy questions via GitHub Issues:
https://github.com/HcmDz/Coil_Tester/issues