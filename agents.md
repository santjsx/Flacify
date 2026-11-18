# AGENTS.md — Strict Engineering Directives & Architecture Charter

This document is the **Mandatory Operating Charter** for Antigravity, Gemini, and any AI engineering agent or developer modifying the FLACify codebase.

> **CRITICAL DIRECTIVE**: You are strictly forbidden from breaking existing functionality, introducing UI layout shifts, degrading frame rates, or bypassing automated test validation. Every modification must adhere to the laws and invariants defined herein.

---

## 1. Prime Invariants (The "Never Break" Laws)

### Law 1: Engine & State Integrity Precedes Presentation
- Never set domain StateFlows to transient `null` or `0` values during media transitions.
- When transitioning tracks (`playSong`, `skipToNext`, `skipToPrevious`), preload known metadata and durations from the domain `Song` entity immediately into `_currentSong` and `_durationMs`. Do not wait for asynchronous ExoPlayer callbacks to prevent UI flicker.
- Transient Media3 callbacks (such as `onMediaItemTransition(null, ...)` during playlist swaps) must **never** clear `_currentSong` if the queue is non-empty.

### Law 2: Zero Layout Shifts (CLS = 0)
- **MiniPlayer Height Invariant**: The MiniPlayer container must maintain a fixed, immutable height (`76.dp`) whenever an active track exists. It must never expand, contract, or wobble when changing songs.
- **Progress Bar Invariant**: The track progress indicator in the MiniPlayer must occupy a permanently reserved vertical slot (6dp top spacer + 2dp bar track + 2dp bottom spacer = 10dp total). It must **never** be conditionally removed based on `durationMs > 0`. If duration is unknown, render the track line with 0 progress.
- **Badge & Pill Invariant**: Audiophile badges (e.g. `AudioQualityBadge`) and metadata chips must reside in reserved fixed-height rows or containers with explicit line-height and `includeFontPadding = false`. Never allow the presence or absence of a lossless badge to alter row height.
- **Dock & Clearance Invariant**: Bottom content clearance across scrollable lists (`rememberFloatingDockBottomClearance`) is strictly keyed to whether a track is actively loaded. It must **never** momentarily collapse from 136dp to 60dp during track changes.

### Law 3: Compose Lifecycle & Side-Effect Purity
- **Zero Side-Effects in Composition**: Never trigger navigation callbacks, state mutations, or dialog dismissals directly inside the composition body. Always wrap side-effects in `LaunchedEffect` or UI event handlers.
- **Flow Collection**: Always collect StateFlows in UI composables using `collectAsStateWithLifecycle()` to prevent background battery drain and state desynchronization.
- **Gesture Integrity**: Overlays, sheets, and full-screen screens (`NowPlayingScreen`) must intercept empty space touch events using dedicated touch shields to prevent touch pass-through to underlying MiniPlayers or lists.

### Law 4: Hierarchical Back Handling
- Independent sheets (SleepTimer, Equalizer, Favorites, Playlists) own highest back priority.
- `NowPlayingScreen` owns its own back handler for lyrics dismissal and collapse.
- Secondary tab navigation stack pops sequentially.
- Root `HomeScreen` enforces a 2-second debounced double-back confirmation to prevent accidental app exits.
- Agents must never register competing or uncoordinated `BackHandler`s.

---

## 2. Component Design Standards

### 2.1 MiniPlayer & Unified Floating Dock
```
+-------------------------------------------------------------+
| UnifiedFloatingDock (16dp horizontal padding, 8dp bottom)   |
|  +-------------------------------------------------------+  |
|  | MiniPlayerSection (Fixed 76.dp height)                |  |
|  |  - Drag Handle: 32dp x 4dp                            |  |
|  |  - Spacer: 8dp                                        |  |
|  |  - Content Row: Fixed 42dp height                     |  |
|  |     * Squircle Album Art: 42dp x 42dp                 |  |
|  |     * Text Column: Fixed 38dp, centered               |  |
|  |         Row (Title + AudioQualityBadge): Fixed 20dp   |  |
|  |         Spacer: 2dp                                   |  |
|  |         Artist Name: Fixed 16dp                       |  |
|  |     * Play/Pause Button: 40dp x 40dp                  |  |
|  |     * Skip Next Button: 40dp x 40dp                   |  |
|  |  - Spacer: 6dp                                        |  |
|  |  - Progress Bar: Fixed 2dp track (always present)     |  |
|  |  - Spacer: 2dp                                        |  |
|  +-------------------------------------------------------+  |
|  | NavigationSection (52dp height, sliding pill)         |  |
|  +-------------------------------------------------------+  |
+-------------------------------------------------------------+
```

### 2.2 Text & Typography Rules
- All single-line title and metadata texts in player bars must specify:
  1. `maxLines = 1`
  2. `overflow = TextOverflow.Ellipsis`
  3. `style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false), lineHeight = ...)`
  4. Explicit or bounded row height (`height(20.dp)`, `height(16.dp)`).

---

## 3. Playback Engine State Protocol

When modifying `PlaybackRepository` or `FlacifyPlaybackService`:
1. **Immediate State Propagation**:
   - `playSong(song, queue)` -> Pre-populate `_currentSong.value = song`, `_durationMs.value = song.durationMs`, `_currentPositionMs.value = 0L`.
   - `skipToNext()` -> Find `nextSong = queue.getOrNull(nextIndex)`, pre-populate `_currentSong.value = nextSong`, `_durationMs.value = nextSong.durationMs`.
   - `skipToPrevious()` -> Pre-populate matching target song and duration before seeking.
2. **Resilient Player Callbacks**:
   - In `onMediaItemTransition(mediaItem, reason)`:
     - If `mediaItem == null`, do NOT set `_currentSong.value = null` unless `_queue` is truly empty.
     - Fallback to `activeSong.durationMs` whenever `controller.duration <= 0L`.
3. **No Main-Thread I/O**:
   - Database operations (Room), file operations (MediaStore, lyrics cache, sidecars), and palette extractions must execute strictly on `Dispatchers.IO`.

---

## 4. Defect Prevention & Ledger Discipline

- Every bug discovered, analyzed, or resolved must be recorded in `DEFECT_LEDGER.md` with:
  - Defect ID (sequential, e.g. `DEF-025`)
  - Component affected
  - Severity level
  - Accurate root cause description
  - Verification & resolution details
- Any architectural change must be reflected in `TECH_SPEC.md`.

---

## 5. Pre-Completion Quality Verification Checklist

Before reporting ANY task complete, every agent MUST execute and pass all items:

- [ ] **1. Compile Check**: `./gradlew compileDebugKotlin` completes with zero errors.
- [ ] **2. Automated Unit Tests**: `./gradlew testDebugUnitTest` runs with 100% passing tests (zero failures).
- [ ] **3. Layout Shift Audit**:
  - MiniPlayer does not alter height, wobble, or jump during song changes.
  - Progress bar track is permanently anchored and never blinks or pops in/out.
  - Album art, title, and artist text do not shift vertically between lossy and lossless tracks.
  - Background list scroll clearance remains static across song transitions.
- [ ] **4. Touch & Haptics Audit**:
  - Buttons maintain minimum 40–48dp touch boxes.
  - Haptic feedback is preserved on interactive controls.
- [ ] **5. Ledger Updated**: `DEFECT_LEDGER.md` reflects all changes and resolutions.
