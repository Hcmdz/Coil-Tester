# Security Policy

## Supported Versions

| Version | Supported |
|---|---|
| 1.0.x | ✅ |
| < 1.0 | ❌ |

## Reporting a Vulnerability

CoilTester is a fully offline app with no network layer and no user accounts. If you
believe you have found a security issue (e.g., a way to extract data from the device,
a flaw in the sensor permission flow, or a build/signing weakness), please report it
privately rather than opening a public issue.

- **Private reporting**: open a private report via GitHub's [private vulnerability reporting](https://github.com/Hcmdz/Coil-Tester/security/advisories/new).
- **Response time**: best effort within 7 days.
- **Disclosure**: coordinated disclosure preferred; please allow reasonable time to fix before public disclosure.

Please do not include exploit code in the initial report. Provide steps to reproduce and
the affected version.

## Build-time hardening

- The Gradle build cache is disabled (`org.gradle.caching=false` in `gradle.properties`)
  as defense-in-depth against CVE-2026-53914 (unsafe deserialization in the Kotlin
  build cache, affecting `org.jetbrains.kotlin:kotlin-gradle-plugin` < 2.4.20-Beta1).
- The project targets Kotlin 2.3.21; upgrading to the patched 2.4.20-Beta1+ would
  require a matching KSP release and is not currently planned. The vulnerability is
  build-time only and fully mitigated by disabling the cache.