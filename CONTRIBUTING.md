# Contributing to CoilTester

Thanks for your interest. CoilTester is an offline-first Android app; contributions are welcome.

## Getting Started

- Fork the repository
- Clone your fork
- Open in Android Studio (latest stable)
- Sync Gradle and build

## Requirements

- Android Studio latest stable
- JDK 17+
- Android SDK 35+ (compileSdk 35)
- Device or emulator running Android 7.0+ (minSdk 24)

## Build

```bash
# Debug build
./gradlew assembleDebug

# Release build (requires signing config in gradle.properties)
./gradlew assembleRelease
```

## Code Style

- Kotlin with official conventions
- Jetpack Compose for UI
- Material 3 design system
- Follow existing patterns in the codebase
- Detekt (`./gradlew detekt`) and Ktlint (`./gradlew ktlintCheck`) must pass

## Pull Requests

- Create a feature branch from `main`
- Make your changes
- Run `./gradlew lint` and fix any issues
- Write clear commit messages (Conventional Commits: `feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`)
- Open a PR with a description of changes

## Issues

- Use the provided issue templates
- Include device model, Android version, and app version
- Steps to reproduce for bug reports

## General

- One logical change per PR.
- Include tests for new behavior; keep `./gradlew check` green.
- Do not commit secrets, keystores, or `local.properties`.