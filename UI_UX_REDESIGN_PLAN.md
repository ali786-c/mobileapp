# Cricket Draft OS Mobile UI/UX Redesign Plan

## Product direction

Cricket Draft OS Mobile will be redesigned as a role-aware SaaS product rather than a collection of disconnected screens. Every signed-in user should immediately understand where they are, what requires attention, what is live, and which action is safest to take next. The application will use a consistent information hierarchy across public audience, player, captain, scorer, admin, and Super Admin experiences while preserving the permissions enforced by Laravel.

The redesign will be implemented in small, testable slices. Each slice will preserve the existing API contracts unless an API gap is identified, and each completed slice will be verified on a compact phone, a large Android phone, landscape mode, a slow connection, and a disconnected state.

## Current baseline audit

The current project already has a useful foundation: Jetpack Compose, Material 3, Hilt, Retrofit, typed API models, secure token storage, reusable buttons/cards/status chips, and role-specific screen modules. The main UX risks are that the app shell still behaves like a simple four-tab prototype, some role destinations are entry placeholders rather than complete task flows, loading and error states are visually inconsistent, public and operational workflows are not clearly separated, and several screens rely on text buttons and navigation patterns that make the next action unclear on a small screen.

The first redesign target is therefore the shared shell and component layer. Once the shell is stable, individual role workflows can be improved without repeatedly rewriting spacing, headers, action bars, states, dialogs, and navigation behavior.

## Experience architecture

| Experience area | Primary user | Main job | Mobile entry point |
|---|---|---|---|
| Home dashboard | All signed-in users | Understand current status and next action | Home |
| Public cricket | Guest, player, supporter | Browse tournaments, fixtures, standings, and live matches | Explore |
| Captain workspace | Captain | Manage draft picks, squad, registration, and playing XI | My team |
| Scorer workspace | Scorer | Record deliveries and maintain an accurate live scorecard | Scoring |
| Tournament operations | Admin | Control tournament lifecycle, draft, fixtures, reports, and teams | Operations |
| Platform governance | Super Admin | Manage API clients, admins, audit, and platform health | Governance |
| Personal account | All signed-in users | View identity, role, permissions, and sign out | Profile |

The bottom navigation will remain limited to four high-value destinations. Role-specific workspaces will be surfaced from the Home dashboard and from contextual cards, not forced into a crowded universal tab bar. Each workspace will include a clear back path, a compact title bar, a role badge, and a visible tournament or match context.

## Phase 1: Foundation and design system

The design system will define semantic colors for canvas, surface, ink, muted text, primary action, success, warning, danger, live state, and disabled state. Colors will be checked for contrast. Typography will define display, screen title, section title, body, metadata, label, and numeric score styles. Spacing will use a small set of tokens rather than arbitrary values. Cards will have consistent radius, border, elevation, internal padding, and press behavior.

Reusable components will include `AppTopBar`, `ScreenHeader`, `WorkspaceHeader`, `RoleBadge`, `TournamentStatusChip`, `LiveIndicator`, `MetricCard`, `ActionCard`, `EmptyState`, `ErrorState`, `OfflineBanner`, `RefreshingIndicator`, `ConfirmActionDialog`, `BottomSheetFilter`, `SearchField`, `SegmentedFilter`, `PlayerRow`, `TeamRow`, `FixtureRow`, `ScoreSummary`, `TimelineRow`, `ReportRow`, and `SectionDivider`. These components will be accessible by default and will not hide important state in color alone.

The shared state components will distinguish initial loading, refreshing existing data, empty data, recoverable API error, permission denial, expired session, offline mode, and destructive-action confirmation. A refresh should never blank the last good content. Buttons that mutate server data will show progress and become temporarily unavailable to prevent duplicate submissions.

## Phase 2: Authentication and shell

The login screen will become a focused sign-in experience with brand mark, concise value proposition, email validation, password visibility toggle, keyboard actions, progress state, server validation text, retry behavior, and a clear explanation when the API cannot be reached. The secure session restoration screen will use branded loading rather than a generic spinner.

The authenticated shell will show the user name, active role, profile avatar fallback, current tournament context when applicable, and a notification or attention area. Navigation will use predictable back-stack behavior, restore the last safe screen, avoid duplicate destinations, and use `popUpTo` for top-level tabs. Deep links such as tournament, match, report, and draft routes will be encoded safely and will not expose an empty screen when an identifier is invalid.

## Phase 3: Public audience experience

The public home will prioritize live events, upcoming fixtures, and the user’s recently opened tournaments. Tournament cards will show logo/banner where available, status, date range, location, team count, and one primary action. The tournament detail page will use tabs or segmented navigation for overview, fixtures, standings, squads, draft, and reports, depending on public availability.

Live match screens will lead with both team names, current result, live/reconnecting state, revision timestamp, innings score, wickets, overs, target, required run rate where available, and a compact recent-events timeline. The screen will retain the latest good state during polling failures and clearly state when data is stale. Fixtures will support date grouping and match-status filters. Standings will use a horizontally scrollable table with a compact mobile summary and accessible column labels.

## Phase 4: Captain and player experience

The captain home will start with the assigned tournament and a clear draft status card. During a live draft, the captain will see the current round, pick number, countdown, whose turn it is, connection status, available-player search, role filters, selected-player confirmation, and the captain’s current squad. The pick action will require an explicit confirmation sheet and will immediately show a server-confirmed result.

Player lists will be searchable and filterable by batsman, bowler, all-rounder, wicketkeeper, and other backend roles. Every player row will display name, role, location or useful metadata, availability, and selected status. The squad screen will show role balance, total players, remaining slots, and a clear difference between pending and server-confirmed selections.

Registration and playing-XI flows will use step-based forms with saved progress, validation near the field, a final review screen, and an explicit submission result. Playing XI selection will show the required number of players, captain and wicketkeeper constraints when supplied by the API, bench players, and a final confirmation state.

## Phase 5: Scorer and live scoring experience

The scorer workspace will be optimized for speed and accuracy. It will expose the selected match context at all times, innings status, striker and non-striker, bowler, over progress, score summary, wicket state, and a large delivery-entry control area. Common scoring actions will be one tap away, while less common actions will be grouped in a bottom sheet.

Every delivery submission will show a short progress state, prevent accidental duplicate taps, and preserve a local pending action indicator until the server confirms the revision. Undo or correction will require explicit permission and confirmation. A connection banner will explain whether the scorer is online, retrying, or viewing the latest confirmed score.

## Phase 6: Admin and Super Admin experience

Admin screens will use an operations dashboard rather than a long list of buttons. The dashboard will show tournament status, active draft or match, pending registrations, upcoming fixtures, team squad completion, recent audit activity, and high-risk actions requiring attention.

Tournament management will be organized into Overview, Configuration, Teams, Players, Draft, Fixtures, Matches, Reports, and Audit. Controls such as state transitions, timer extension, pause/resume, skip, undo, manual selection, reassignment, and deletion will be grouped by context and guarded by confirmation dialogs. Invalid transitions will be communicated using the server’s response rather than optimistic UI.

Super Admin will have a platform overview with API client health, active administrators, recent activity, audit events, and system status. API client creation and editing will use a guided form that explains slug, active status, description, and the effect of disabling a client. Sensitive operations will be visually separated from normal navigation.

## Phase 7: Reports, notifications, and polish

Reports will be presented as role-aware cards with report description, date range, generated state, preview action, and download/share action. Admin users will see complete reports; captains will see their squad and tournament reports; live viewers will see the public summary. The UI will not expose internal filenames or technical API details.

Notifications will begin with an in-app attention center for draft turn, timer expiry, match start, score revision, registration result, and admin action results. Later, Android push notifications can be added without changing the screen architecture.

Accessibility and polish will include minimum touch targets, content descriptions for icons, screen-reader labels for score tables, dynamic text support, landscape layouts for scoring, reduced motion behavior, dark-theme readiness, keyboard-safe forms, and correct system-bar padding. All destructive actions will use clear text and never rely only on color.

## Verification gates

Each UI slice is complete only when its initial, loading, refreshing, empty, error, offline, permission, success, and duplicate-action states have been checked. The slice must also be checked with real Laravel response envelopes, a slow network, rotation or process recreation where applicable, and a user role that lacks permission.

The final verification pass will cover login and session restore, public tournament browsing, draft polling, captain selection, playing XI submission, live scoring, admin controls, Super Admin governance, reports, logout, token expiry, API errors, and accessibility basics. The final GitHub commit will include a concise change log and the exact Android Studio build instructions.

## Implementation order

The recommended order is to first refactor the shared theme and components, then redesign login and the app shell, then improve Home and public tournament browsing. After that, the captain draft and squad flow should be completed because it is the most important role-specific interaction. Scoring follows because it needs landscape and high-speed interaction design. Admin and Super Admin follow with the shared operations patterns. Reports, notifications, accessibility, and final verification close the first product-quality release.
