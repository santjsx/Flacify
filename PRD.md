# Product Requirements Document — AUREL
## Premium Android Offline Music Player

**Product specification**  
**Platform**: Android native (Kotlin + Jetpack Compose)  
**UX Philosophy**: iOS-inspired visual restraint, Android native power  
**Target Collection**: 50,000+ local audio tracks  
**Version**: 1.0 · Date: November 2, 2026  

---

## 1. Product Overview & Vision
AUREL is an offline-first music player that pairs the visual discipline of Apple's design language (hierarchy, typography, subtle continuous curves, restrained motion) with Android's native audio capabilities and the robustness of a production-grade audio application.

### Five Core Principles
1. **Clarity**: Every screen has a clear purpose. No clutter, unnecessary cards, or distracting banners.
2. **Fluidity**: Scrolling, navigation, transitions, and playback controls respond immediately. 120Hz/60Hz frame budgets strictly enforced.
3. **Consistency**: Typography, spacing, iconography, colors, and interaction patterns remain coherent across every surface.
4. **Reliability**: Playback, library indexing, and background service operation remain stable under real-world conditions (process death, call interruptions, memory pressure).
5. **Craftsmanship**: Loading, empty states, dialogs, error states, and micro-interactions receive deliberate design attention.

### Scope
- **Included**: Local audio discovery, background playback (`MediaSessionService`), queue management, Now Playing, lyrics (LRC/embedded), equalizer, sleep timer, playlists, favorites, library statistics, light/dark/system themes, GitHub Releases update engine.
- **Excluded**: Streaming subscriptions, social feeds, cloud sync, advertising, AI recommendations.

---

## 2. Technology Stack & Architectural Constraints
- **Language**: Kotlin 2.0+
- **UI**: Jetpack Compose with Material 3 foundation (heavily customized)
- **Architecture**: Clean Architecture + MVVM (Presentation, Domain, Data, Playback, Platform, Update)
- **Audio Engine**: AndroidX Media3 / ExoPlayer 1.5+
- **Background Service**: `MediaSessionService` with foreground notification & lock-screen controls
- **Database**: Room 2.6+ (Single source of truth for indexed library, playlists, favorites, history)
- **Dependency Injection**: Hilt
- **Async Operations**: Kotlin Coroutines + Flow
- **Preferences**: DataStore Preferences
- **Artwork Loading**: Coil 3.x
- **Paging**: AndroidX Paging 3 (mandatory for 50,000+ tracks)
- **Background Jobs**: WorkManager
- **Navigation**: Jetpack Navigation Compose
- **Updates**: GitHub Releases API via Retrofit + OkHttp
- **Testing**: JUnit5, MockK, Turbine, Compose UI Test, Macrobenchmark

---

## 3. Application Architecture & Navigation
- **Primary Destinations** (Maximum 4):
  1. **Home**: Overview statistics, Most Played (threshold-based history), Recently Added (15 tracks).
  2. **Library**: Songs, Albums, Artists switcher with persistent sort & batch actions.
  3. **Search**: Instant debounced search with filter chips (Songs, Albums, Artists, Playlists).
  4. **More**: Compact secondary surface (Playlists, Favorites, Equalizer, Settings, About).
- **Persistent Mini-Player**: Floats above navigation bar when a track is active; tapping expands into Now Playing; play/pause responds on touch-down.
- **Now Playing Experience**:
  - Full-screen edge-to-edge layout with safe system insets.
  - Large square album artwork with continuous rounded corners.
  - Optional subtle ambient color wash extracted via Palette with strict contrast safety clamps.
  - Scrubber with resting 4dp line, expanded touch pill, and -remaining time display.
  - Primary controls: Previous, Play/Pause (72dp primary), Next.
  - Secondary controls: Shuffle, Repeat, Up Next / Queue, Sleep Timer, More menu.
- **Lyrics Surface**: Large centered typography, LRC synchronized highlights with manual scroll override.
- **Sleep Timer**: Monotonic clock timer with custom duration, end-of-track trigger, and gentle 2-second audio ducking.
- **Equalizer**: Native Android `AudioEffect` integration with preset tuning, fallback detection, and no-crash guarantee on unsupported hardware.
- **Library Scanner**: Incremental MediaStore scanner (`READ_MEDIA_AUDIO`), batch Room upserts, deleted file purge, zero main-thread work.
- **GitHub Releases Updater**: Non-silent update flow with SHA-256 validation and standard Android Package Installer invocation.

