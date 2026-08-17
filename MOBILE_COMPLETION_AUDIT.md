# Mobile completion audit

## Current status

The Android app has a solid Compose shell, authenticated session handling, public tournament/detail/scorecard screens, captain draft foundation, player profile/registration, scorer foundation, admin tournament lifecycle controls, and Super Admin dashboard/health views.

## Remaining completion work

| Area | Current state | Completion target |
|---|---|---|
| Navigation | Home tabs and several role entry states exist; tournament detail does not yet pass live fixture/match actions into all role screens | Add real tournament-to-fixture-to-match navigation, role-aware workspace menus, and safe ID/slug route arguments |
| Captain match operations | No captain playing-XI/toss endpoints in the mobile API | Add permission-scoped captain APIs or expose the existing authorized match operations through a dedicated captain controller |
| Reports | Web/PDF reports exist; mobile has no typed JSON report endpoint | Add public and authenticated report JSON endpoints and Compose report cards/download actions |
| Admin operations | Tournament lifecycle is wired; fixtures, match creation, overs, lineups, toss, result approval are not wired in Compose | Add typed admin fixture/match APIs and guided forms/actions |
| Super Admin operations | Dashboard and health are wired; users, API clients, sessions, audit logs, and fleet detail are not wired | Add paginated governance screens and guarded actions |
| Notifications | No push-token registration or notification delivery contract | Add token registration endpoint and local in-app sync notifications first; keep FCM provider integration configurable |
| Reliability | Polling and auth errors have been hardened; offline cache and session-expiry event handling are still basic | Add connectivity state, last-known cache, 401 session reset, retry actions, and production URL validation |
| Build | Gradle wrapper and Compose compiler plugin are present; sandbox lacks Android SDK | Validate in Android Studio/CI with SDK Platform 35 and produce the APK there |

## Server-authoritative rules

The mobile client must never advance draft picks, timers, innings, results, or tournament status locally. It submits an action with the latest revision where supported and renders the returned server state. Polling must preserve the last good state and show a reconnecting indicator during transient failures.
