# AUREL Agent Directive: Master Implementation Playbook

This document is the **Autonomous Operating Charter** for Antigravity or any AI engineering agent working on the AUREL codebase.

---

## 1. Operating Rules of Engagement
- **Never implement UI before the underlying engine exists**: You are forbidden from writing mock UI screens populated with dummy static lists. The database schema, repositories, and playback engine must precede presentation composables.
- **Strictly enforce phase boundaries**: Do not start Phase 2 until Phase 1 builds cleanly. Do not start Phase 4 until Room, MediaStore scanning, and Media3 background playback are verified with automated tests.
- **Maintain documentation integrity**: Update `DEFECT_LEDGER.md` with every issue discovered and verified. Keep `TECH_SPEC.md` synchronized with any architectural adjustments.
- **Test continuously**: Run unit tests after each milestone. Never declare a feature done without automated test validation.

---

## 2. Milestone Execution Sequences

### Phase 1: Foundation & Build System
1. Initialize Gradle build system with `gradle wrapper`.
2. Configure Gradle Version Catalog (`gradle/libs.versions.toml`) with Kotlin, Compose BOM, AndroidX Media3, Room, Hilt, Coil, Coroutines, Navigation, Paging 3, DataStore.
3. Configure `settings.gradle.kts` and root `build.gradle.kts`.
4. Configure `:app` module `build.gradle.kts` with proper compileSdk (35), minSdk (29), targetSdk (35), Java 21 toolchains, Compose compiler plugin.
5. Create base package structure:
   - `com.aurel.music.core.model`
   - `com.aurel.music.core.database`
   - `com.aurel.music.core.audio`
   - `com.aurel.music.core.data`
   - `com.aurel.music.core.designsystem`
   - `com.aurel.music.core.ui`
   - `com.aurel.music.feature.*`
6. Verify clean build: `./gradlew assembleDebug testDebugUnitTest`.

### Phase 2: Audio Engine & Playback Service
1. Define domain contracts in `core.model`: `Song`, `PlaybackState`, `RepeatMode`, `QueueItem`.
2. Implement `AurelPlaybackService` extending `MediaSessionService`.
3. Configure `ExoPlayer` instance with `AudioAttributes` for media playback.
4. Implement `AudioFocusHandler` and `BecomingNoisyReceiver`.
5. Implement `AurelNotificationProvider` for lockscreen and notification tray controls.
6. Implement `PlaybackRepository` with `MediaController` connection exposing state flows to the UI.
7. Write unit tests for playback state machine and queue transition logic.

### Phase 3: Room Database & Incremental Scanner
1. Implement Room entities: `SongEntity`, `AlbumEntity`, `ArtistEntity`, `PlaylistEntity`, `FavoriteEntity`.
2. Implement Room DAOs with Paging 3 `PagingSource` return types.
3. Implement `MediaStoreScanner`: incremental scanning with change detection (`DATE_MODIFIED`), deleted track pruning, and batch upserting.
4. Implement `SongRepository` exposing `Flow<PagingData<Song>>`.
5. Write database migration tests and scanner batching tests.

### Phase 4: Core UI & Design System
1. Implement `AurelTheme` (Apple-inspired dark/light/system, semantic color tokens, typography scale).
2. Implement physics-based spring animations (`AurelSprings`).
3. Build `MiniPlayer` with single-pass marquee text and instant play/pause feedback.
4. Build `NavigationScaffold` managing 4 primary destinations (Home, Library, Search, More).
5. Build `NowPlayingScreen` with dynamic palette extraction, contrast clamp, and high-precision scrubber.
6. Build `LibraryScreen` with segmented control (Songs, Albums, Artists) and LazyPagingColumn.

### Phase 5: Advanced Features
1. Implement `SearchScreen` with debounced Room FTS/indexed search and filter chips.
2. Implement `SleepTimer` with monotonic clock, audio ducking, and completion notification.
3. Implement `LyricsScreen` with LRC timestamp parser and auto-scroll highlighting.
4. Implement `EqualizerScreen` with Android `AudioEffect` wrapper and graceful fallback.
5. Implement `PlaylistManager` with transactional song ordering and favorites toggle.

### Phase 6: Polish, Motion & Edge-to-Edge
1. Audit system insets across status bars, navigation bars, and IME cutouts.
2. Add talkback accessibility semantics and enforce 48dp minimum click targets.
3. Verify 60Hz and 120Hz frame rates with Macrobenchmark and Baseline Profiles.

### Phase 7: GitHub Releases OTA & Hardening
1. Implement `UpdateManager` querying GitHub Releases API.
2. Add SHA-256 APK checksum validation and package installer intent.
3. Configure R8 optimization rules and generate signed release bundle.

