# Contributing to CoilTester

Thanks for your interest. CoilTester is a small, offline-first Android app; contributions
are welcome.

## Getting started

1. Fork and clone the repository.
2. Open in Android Studio (JDK 17, Android SDK 35).
3. Run `./gradlew test` to confirm the baseline is green.
4. Create a feature branch from `main`.

## Code style

- Kotlin official code style (`kotlin.code.style=official`).
- Detekt (`./gradlew detekt`) and Ktlint (`./gradlew ktlintCheck`) must pass.
- New public APIs need KDoc explaining *why*, not *what*.

## Pull requests

- One logical change per PR.
- Conventional Commit messages (`feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`).
- Include tests for new behavior; keep `./gradlew check` green.
- Do not commit secrets, keystores, or `local.properties`.

## Issues

Use the issue templates (bug report / feature request) and include reproduction steps.