# Cricket Draft OS Mobile

Native Android Jetpack Compose client for the Laravel Cricket Draft OS backend.

## Current milestone

The foundation currently includes:

- Kotlin and Jetpack Compose Gradle configuration.
- Hilt application and network dependency module.
- Laravel Sanctum login, session restore, logout, and encrypted token storage.
- Typed public tournament collection models with pagination metadata.
- A signed-in Compose dashboard that fetches public tournaments from Laravel.
- Design architecture and API audit documents in the Laravel project.

## API configuration

The app targets the hosted Laravel API over HTTPS by default:

```text
https://cricket.careerinpak.com/api/v1/
```

The app supports the Laravel API client slug `cricket-draft-android`, but the slug is sent only when it is explicitly configured at build time. This is intentional because the currently hosted database rejected that slug as unregistered during verification. The login payload always includes `email`, `password`, and `device_name`; `client_slug` is added when API client governance is enabled.

For a local Laravel server running on the Android emulator, override both values explicitly. `10.0.2.2` is only appropriate for emulator-to-host networking and local HTTP requires an explicit network-security configuration; the hosted HTTPS default avoids the cleartext failure shown in the previous logcat:

```bash
./gradlew assembleDebug \\
  -PAPI_BASE_URL=http://10.0.2.2:8000/api/v1/
```

For the hosted environment, the normal debug build is sufficient:

```bash
./gradlew assembleDebug \\
  -PAPI_BASE_URL=https://cricket.careerinpak.com/api/v1/
```

After creating and activating the API client in Super Admin, enforce it in the app build:

```bash
./gradlew assembleDebug \\
  -PAPI_BASE_URL=https://cricket.careerinpak.com/api/v1/ \\
  -PAPI_CLIENT_SLUG=cricket-draft-android
```

Never commit real credentials or tokens. The app stores the Sanctum token only after a successful login using encrypted Android preferences.

## Local build prerequisites

The sandbox currently has Java 21 but does not have the Android SDK, `adb`, or a system Gradle installation. Build and emulator verification should therefore be performed on an Android development machine with Android Studio, SDK Platform 35, and an emulator or physical device. The project is structured to add the Gradle wrapper once the Android SDK/Gradle toolchain is available.

## Planned next screens

The next implementation slice adds public tournament detail, fixtures, standings, live draft polling, and the role-aware bottom navigation shell. Captain, scorer, admin, and Super Admin feature modules follow the existing Laravel API audit and architecture documents.


## UX and code-quality upgrade

The app now uses a shared Cricket Draft design system with semantic colors, readable typography, consistent cards, status chips, loading states, retry states, and plain-language empty states. The authenticated shell provides Home, Live, Tournaments, and Profile navigation, while role-aware workspace actions lead to captain, admin, scorer, and Super Admin surfaces without exposing fake match or player IDs.

The live draft polling loop now performs one cancellable request at a time, preserves the last good state during transient network failures, and exposes reconnecting status. Live scorecards use the same last-good-state behavior. Admin pagination response parsing now matches Laravel's nested paginator envelope. Release HTTP logging is disabled; only debug builds use basic request logging, and network timeouts are bounded.

## Android build check

The Kotlin Compose compiler plugin and Gradle wrapper are included. A real Gradle configuration check reached the Android build step successfully, but this sandbox has no Android SDK or `ANDROID_HOME`, so `:app:compileDebugKotlin` cannot complete here. Open the project in Android Studio, install Android SDK Platform 35, and run `./gradlew assembleDebug`.


## Completion-phase API wiring

The client now includes typed JSON reports for public, captain, and admin audiences, plus a captain playing-XI submission contract. Tournament detail fixture rows can open real match scorecards when Laravel returns a `match_id`.

Before a release build, pass the deployed API URL explicitly:

```bash
./gradlew assembleRelease \\
  -PAPI_BASE_URL=https://cricket.careerinpak.com/api/v1/
```

After the Super Admin creates the active `cricket-draft-android` client, pass `-PAPI_CLIENT_SLUG=cricket-draft-android` to enforce the registered client. The hosted HTTPS URL is the safe default for both debug and release builds.
