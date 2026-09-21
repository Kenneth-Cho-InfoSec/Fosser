# Fosser — Open-Source App Discovery for F-Droid

Fosser is a privacy-friendly Android app for discovering open-source apps from F-Droid
through a fast swipe-card interface. No account, no tracking, local-first.

Forked from [alejandro-piguave/TinderCloneCompose](https://github.com/alejandro-piguave/TinderCloneCompose)
and substantially rebuilt as a purpose-built discovery app.

## Interaction

- 👈 Swipe left → **Pass** (dismiss, recorded locally)
- 👉 Swipe right → **Like / Save** (saved locally, visible in Saved screen)
- 👆 Swipe up → **Open F-Droid** (browser `ACTION_VIEW`, never WebView)
- 👆 Tap card → in-app detail screen

Buttons supplement gestures (Pass / Save / Open) with content descriptions and 56dp targets.

## What was reused from the base repo

- Swipe-card interaction pattern: `detectDragGestures` + `Animatable<Offset>` drag state,
  coerce logic, dominant-axis resolution, rotation derived from X offset, tween reset/swipe.
  (`ui/components/swipe/SwipableCardState.kt`, `SwipableCard.kt`, adapted from `SwipableCard*.kt`)
- MVVM + `StateFlow` patterns (`HomeViewModel`, state-driven deck).
- Navigation Compose patterns (single `NavHost` with routes).
- Coil image loading, project/build configuration shape.

## What was removed

- Firebase (Auth, Firestore, Storage), Google Sign-In, `google-services` plugin.
- Dating domain: profiles, matches, chats, messages, onboarding, edit-profile.
- Dating branding, strings, assets, mock/chat backend logic.
- Koin (replaced with a tiny manual `AppContainer`).

## What was added

- F-Droid data layer: streaming `index-v2.json` parser (`FdroidIndexParser`, Gson streaming),
  `FdroidIndexFetcher` (OkHttp, no backend), absolute icon/screenshot URL building.
- Room database: `apps`, `swipe_history`, `saved_apps`.
- `RecommendationEngine`: deterministic scoring (LIKE +3, OPEN +1, PASS −2) over
  category/developer/license/keywords + diversity pass + unseen-first ranking.
- Three-way swipe: `SwipeAction { PASS, LIKE, OPEN }`, LEFT→PASS, RIGHT→LIKE, UP→OPEN,
  DOWN blocked; thresholds 30% width / 25% height; overlays fade with drag distance.
- Screens: Home (card stack), Details, Saved, History, Settings. Offline banner,
  error handling, accessibility semantics, Material3 Fosser branding + original icon.

## F-Droid data source

- `https://f-droid.org/repo/index-v2.json` (official signed index, ~60MB), streamed and cached in Room.
- Per-app page: `https://f-droid.org/packages/<packageName>`.
- Images: `https://f-droid.org/repo/<path>` built from index `name` fields.
- Bundled `SeedApps` (real popular apps) is only an offline bootstrap until the first
  successful index download; production data always comes from the live index.

## Database schema

- `apps(packageName PK, name, summary, description, iconUrl, screenshotsJson, developer,
  license, categoriesCsv, versionName, versionCode, fDroidUrl, sourceUrl, issueTrackerUrl,
  websiteUrl, changelogUrl, donateUrl, lastUpdated, added, catalogUpdatedAt,
  permissionsCsv, apkSize)` — v2 adds the last two via `MIGRATION_1_2` (history preserved)
- `swipe_history(id auto PK, packageName, action LIKE/PASS/OPEN, timestamp)`
- `saved_apps(packageName PK, savedAt)`

## Recommendation logic

```
unseen apps → score(category, developer, license, keywords) → diversify → sort → top N
```

Measured on a 4500-app catalog (JVM benchmark): ~40ms with history,
~5ms cold start (scoring skipped when there are no signals yet).
`diversify` is O(n) via per-category queues (was O(n²) rescanning);
keyword extraction uses a lazy token scan capped at 24 tokens with truncated
descriptions. Ranking runs on `Dispatchers.Default`, never the main thread.

## Card layout (approved HTML prototype in `card-prototype.html`)

Icon top-left | name + summary + "Updated <date> · <size>" top-right |
`Open Source` + license + category pills | permissions bottom-left (first 5 + "+N more") |
first screenshot right in a 9:20 frame (or first-50-words about excerpt if none).
One of three bundled ≤100KB textures behind each card at low opacity, darkened for contrast.
Bottom bar order: Save (left), Open (centre), Pass (right).

## Open fallback chain

Swipe-up / Open tries: (1) system browser via `ACTION_VIEW`
(manifest `<queries>` for http/https so `resolveActivity` works on Android 11+),
(2) in-app `WebViewScreen`, (3) error message with retry. SSL errors are never bypassed.

## Build / test

```sh
./gradlew assembleDebug
./gradlew :app:testDebugUnitTest
```

- `assembleDebug`: succeeds (AGP 9.4.1, Gradle 9.7.1, Kotlin 2.3.21 + Compose plugin,
  KSP 2.3.12, Room 2.8.5, Navigation 2.10.1, Coil 3.2.0, OkHttp 5.5.0, Coroutines 1.11.0,
  Gson 2.14.0, compileSdk/targetSdk 37, Java 17).
- Latest stable everything (checked against Maven Central/Google Maven, Sept 2026):
  JUnit4 stays at 4.13.2 (final release); unused mockk removed; Coil 2 → 3 migration
  (new `coil3` coordinates + OkHttp network factory + disk cache in `FosserApp`).
- Unit tests (28): `RecommendationEngineTest`, `SwipeActionTest`, `FdroidIndexParserTest`
  (now covers permissions + APK size), `MappersTest`, `CardFormatTest` (size/date/excerpt).
- Photo credit: card textures by Veitch/Stander/Radojcic (Unsplash License).

## Release signing

- `keystore/fosser-release.keystore` (RSA-4096, self-signed, valid 2026–2056).
  **Back it up — losing it means the app can never be updated under the same identity.**
- Passwords live only in `local.properties`
  (`release.store.file/password`, `release.key.alias/password`); without them the
  release falls back to the debug key so builds never break.
- Release cert SHA-256: `84:FA:08:DC:97:45:31:94:8A:30:6C:C8:6E:3E:39:2D:0A:F4:86:E8:2A:5A:3C:23:5A:B4:A1:B1:8C:91:FB:A5`
- `./gradlew :app:assembleRelease` → `app/build/outputs/apk/release/app-release.apk`
