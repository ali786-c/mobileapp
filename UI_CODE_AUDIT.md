# Compose UI and code audit

## High-priority findings

The current app has a working API foundation, but its entry point stops at a single dashboard and does not wire the existing screens into navigation. Tournament cards are not actionable, roles are displayed as raw strings, and users have no clear home, live, matches, profile, or role-workspace destinations after login.

The screens use repeated private color constants, raw text, ad-hoc spacing, and inconsistent button/card treatment. This makes the product feel like a technical prototype rather than a user-friendly tournament app. Several interaction surfaces also lack explanatory copy, confirmation for destructive actions, disabled/loading feedback, and friendly recovery guidance.

The current live draft polling starts a new coroutine for every refresh while the polling loop continues, which can overlap requests under slow networks. The timer is rendered from the last server value but does not visibly communicate expired/paused state or synchronize a local countdown. API failures are surfaced as raw exception messages, which are not suitable for ordinary users.

The scorer room accepts raw numeric player IDs from its caller and has no player-selection UI, no wicket workflow, no visible current scorecard, and no explicit confirmation for undo. The admin and Super Admin screens expose powerful actions without permission-aware empty states or confirmation feedback. Profile and registration forms need clearer required-field guidance and success/error banners.

The Android build configuration targets the correct API structure but the debug URL is only valid for an emulator running the Laravel server on the host. Release configuration still uses a placeholder API URL and must be configured before production packaging. The sandbox lacks Android SDK/adb/compiler tooling, so compile verification must be completed in Android Studio or CI.

## Refactor priorities

1. Add a shared design system with semantic colors, spacing, typography, cards, status chips, empty/error/loading states, and accessible button sizing.
2. Replace the single dashboard with a real navigation shell: Home, Tournaments, Live, Profile, and role-based Workspace destinations.
3. Make tournament, match, draft, registration, scorer, admin, and governance actions discoverable with plain-language labels and contextual helper text.
4. Harden polling to use one cancellable request loop, preserve the last good state on transient errors, and show connection status.
5. Normalize API error messages and add confirmation for picks, undo, status changes, logout, and session-sensitive actions.
6. Add UI tests and view-model tests once Android tooling is available; retain Laravel's passing regression suite as the backend safety net.
