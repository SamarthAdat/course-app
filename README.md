# Learning Dashboard — Android

Kotlin · Jetpack Compose · MVVM + Clean layering · Room · Hilt

Login → Course dashboard → Course details with lesson completion, fully usable offline.

**Demo account:** `learner@example.com` / `password123` (shown on the login screen)
**APK:** [`artifacts/CourseApp-release.apk`](artifacts/CourseApp-release.apk) · or `./gradlew :app:assembleRelease`
**Tests:** `./gradlew :app:testDebugUnitTest` — 21 tests

---

## 1. Architecture

`UI (Compose) → ViewModel → Repository → API + Room`, with a `domain` layer of plain Kotlin in the
middle. MVVM with a single immutable state object per screen, because that is what Compose is built
for and what the rest of the Android ecosystem expects a team to maintain.

Three decisions carry most of the weight:

- **Each screen has one sealed `UiState`.** Loading / Empty / Error / Content cannot be combined
  into an invalid pair, so the screen has no "spinner over an error over stale data" case to debug.
- **Progress is derived, never stored.** `ProgressCalculator` turns completed/total lessons into a
  percentage, computed in SQL for the dashboard and from the same counts on the details screen. The
  two screens cannot disagree after a lesson is ticked, and there is no cross-screen event to wire.
- **Interfaces at every boundary.** `CourseApi`, `AuthApi`, `NetworkMonitor` and the repositories
  are interfaces; the mock implementations are named in one Hilt module. Swapping the bundled JSON
  for Retrofit is an edit to `DataModule` and nothing above it.

The mock API is deliberately not a plain file read: it costs ~900 ms, checks connectivity first and
throws `NoConnectionException` when the device is offline, so the loading and offline paths are real
rather than simulated.

## 2. Offline support

Room is the **single source of truth**. The UI only ever observes `Flow`s over the database; the
network is only a way to update it. A failed refresh therefore cannot blank the screen — it
downgrades to a banner over real cached data, and only takes over the screen when nothing has been
cached yet. Tested in `DashboardViewModelTest`.

`CourseDao.replaceCatalog` merges rather than overwrites: lesson completions that exist only locally
survive a refresh, so progress made offline is not silently undone when the radio comes back. The
cache also survives process death — cold-starting in airplane mode still shows the catalog.

## 3. Security

In production the session token would go in **`EncryptedSharedPreferences` / DataStore encrypted
with an Android Keystore key** (`setUserAuthenticationRequired` where the risk justifies it), never
in plain `SharedPreferences`, never in the database, never in logs. The refresh token stays in that
store; the short-lived access token stays in memory and is attached by an OkHttp interceptor.

This app keeps the token **in memory only** and returns to login after a restart. That is a
deliberate choice over a half-done version: plain preferences would look like persistence while
being strictly worse than none.

## 4. Scale — 1M users, hundreds of courses

1. **Paginate the catalog** (`Paging 3` + Room `PagingSource`, cursor-based API). Fetching hundreds
   of courses and all their lessons in one response is the first thing that breaks.
2. **Sync lesson completion properly.** Today the local flag wins. At scale it needs a queue of
   pending completions, `WorkManager` with backoff, idempotent writes, and a documented conflict
   rule (last-write-wins on `completedAt`, server authoritative).
3. **Conditional fetches** — ETag / `If-Modified-Since` plus a `lastSyncedAt` column, so a refresh
   on an unchanged catalog costs a 304 instead of a full payload.
4. **Observability.** Crash reporting, a trace around the refresh call, and counters for
   cache-hit-rate and refresh failure rate. Offline bugs are invisible without them.
5. **Release engineering.** R8 is already on; add baseline profiles, a startup benchmark, and a
   staged rollout so a bad catalog migration reaches 1% of users rather than all of them.

## 5. The same app on iOS/macOS

The layering maps almost one to one: SwiftUI for the UI, `@Observable` (or `ObservableObject`)
view models exposing the same sealed state — a Swift `enum` with associated values, which is the
exact analogue of the sealed interfaces here. Repositories become protocols, injected through the
initialiser instead of Hilt (a small container, or swift-dependencies); `Flow` becomes
`AsyncStream`/Combine publishers; Room becomes **SwiftData** (or Core Data) with an
`@Query`-observed store so the UI still reads from one source of truth; `ConnectivityManager`
becomes `NWPathMonitor`; the token moves to the **Keychain** with `kSecAttrAccessibleAfterFirstUnlock`.
The domain layer — models, `ProgressCalculator`, validation — is pure logic and would port almost
verbatim.

---

## Project layout

```
core/      result types (AppResult/AppError), NetworkMonitor
domain/    models, ProgressCalculator, validation, repository interfaces
data/      remote (DTOs + mock API), local (Room), mappers, repository impls
di/        Hilt modules — the only place that knows the APIs are mocked
feature/   login / dashboard / detail — UiState + ViewModel + Compose screen
navigation/ type-safe routes
```

## Testing

21 unit tests, the meaningful ones being:

- `DashboardViewModelTest` — the offline requirement itself: a failed refresh keeps cached courses
  on screen with a notice, but shows a full-screen error when there is nothing cached; an empty
  success is an empty state, not an error.
- `ProgressCalculatorTest` — rounding, clamping, and the divide-by-zero case.
- `CourseCatalogFixtureTest` — the payload's declared `progress`/`lessons` must equal what the app
  derives from the lesson list, which catches a bad fixture or a lossy mapper.
- `LoginViewModelTest` — invalid input never reaches the API; a rejected login re-enables the button.

## Notes for the reviewer

- The debug build has a **"Simulate API failure"** item in the dashboard overflow menu, for
  exercising the error state without unplugging anything.
- To see the offline behaviour: open the dashboard once, enable airplane mode, pull to refresh.
  The banner appears and the courses stay. Marking lessons complete keeps working.
- `app/src/main/assets/courses.json` keeps the field names from the brief (`progress`, `lessons`)
  and adds `lessonList`; the app ignores the first two and derives them, as described above.
