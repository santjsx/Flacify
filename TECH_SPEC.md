# AUREL — Technical Specification
**Version**: 1.0  
**Target Platform**: Android 10+ (API Level 29 to 35+)  
**Architecture**: Clean Architecture, Unidirectional Data Flow (UDF), Decoupled Playback  

---

## 1. System Architecture & Module Boundaries

AUREL enforces strict architectural separation between the UI and the underlying Audio Engine. Composable functions must **never** instantiate or own ExoPlayer instances.

```
                    ┌─────────────────────────┐
                    │  Presentation (Compose) │
                    └────────────┬────────────┘
                                 │ Observes StateFlows / Sends Intents
                    ┌────────────▼────────────┐
                    │    PlaybackRepository   │
                    └────────────┬────────────┘
                                 │ IPC / Binder
                    ┌────────────▼────────────┐
                    │   AurelPlaybackService  │ (MediaSessionService)
                    │   ┌──────────────────┐  │
                    │   │    ExoPlayer     │  │
                    │   └──────────────────┘  │
                    └─────────────────────────┘
                                 ▲
                                 │ MediaItem Sources
                    ┌────────────┴────────────┐
                    │      Room Database      │ (Single Source of Truth)
                    └────────────▲────────────┘
                                 │ Incremental Batch Upsert
                    ┌────────────┴────────────┐
                    │   MediaStore Scanner    │ (Coroutines + Dispatchers.IO)
                    └─────────────────────────┘
```

### Module Structure
- `:core:model` — Domain entities (`Song`, `Album`, `Artist`, `Playlist`, `QueueItem`, `PlaybackState`, `EqualizerBand`).
- `:core:database` — Room database, DAOs, entities (`SongEntity`, `AlbumEntity`, etc.), migrations.
- `:core:audio` — `AurelPlaybackService`, ExoPlayer manager, AudioFocus handler, AudioEffect/Equalizer controller, Notification manager.
- `:core:data` — Repositories (`SongRepository`, `PlaybackRepository`, `PlaylistRepository`, `SettingsRepository`), MediaStore scanner worker.
- `:core:designsystem` — Theme tokens, typography, semantic color roles, continuous squircle shapes, spring animation specs.
- `:core:ui` — Reusable components (MiniPlayer, SongRow, AlbumCard, HighPrecisionScrubber, MarqueeText).
- `:feature:home` — Home dashboard (Library stats, Most Played, Recently Added).
- `:feature:library` — Songs list, Albums grid, Artists list with Paging 3.
- `:feature:nowplaying` — Immersive player, dynamic background palette, Up Next queue.
- `:feature:lyrics` — LRC synchronized and unsynchronized lyrics renderer.
- `:feature:equalizer` — Band frequency visualizer and preset controls.
- `:feature:search` — Debounced multi-entity search with filter chips.
- `:feature:settings` — Theme, library scan controls, playback preferences, GitHub update dialog.
- `:app` — Application entry point, Hilt dependency injection root, Navigation Compose host.

---

## 2. Database Schema (Room)

All queries are optimized for libraries containing 50,000+ tracks using composite indices.

### Entity: `songs`
```sql
CREATE TABLE IF NOT EXISTS songs (
    id INTEGER PRIMARY KEY,           -- MediaStore._ID
    media_uri TEXT NOT NULL,
    title TEXT NOT NULL,
    artist_id INTEGER NOT NULL,
    artist_name TEXT NOT NULL,
    album_id INTEGER NOT NULL,
    album_title TEXT NOT NULL,
    duration_ms INTEGER NOT NULL,
    track_number INTEGER DEFAULT 0,
    disc_number INTEGER DEFAULT 1,
    year INTEGER DEFAULT 0,
    date_added INTEGER NOT NULL,      -- MediaStore.DATE_ADDED
    date_modified INTEGER NOT NULL,   -- MediaStore.DATE_MODIFIED
    file_size INTEGER NOT NULL,
    mime_type TEXT NOT NULL,
    bitrate INTEGER DEFAULT 0,
    sample_rate INTEGER DEFAULT 0,
    play_count INTEGER DEFAULT 0,
    last_played_timestamp INTEGER DEFAULT 0,
    is_favorite INTEGER DEFAULT 0
);

CREATE INDEX idx_songs_title ON songs(title COLLATE NOCASE);
CREATE INDEX idx_songs_artist ON songs(artist_name COLLATE NOCASE);
CREATE INDEX idx_songs_album ON songs(album_title COLLATE NOCASE);
CREATE INDEX idx_songs_date_added ON songs(date_added DESC);
CREATE INDEX idx_songs_play_count ON songs(play_count DESC);
CREATE INDEX idx_songs_is_favorite ON songs(is_favorite);
```

### Entity: `playlists` & `playlist_tracks`
```sql
CREATE TABLE IF NOT EXISTS playlists (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS playlist_tracks (
    playlist_id INTEGER NOT NULL,
    song_id INTEGER NOT NULL,
    sort_order INTEGER NOT NULL,
    PRIMARY KEY(playlist_id, song_id),
    FOREIGN KEY(playlist_id) REFERENCES playlists(id) ON DELETE CASCADE,
    FOREIGN KEY(song_id) REFERENCES songs(id) ON DELETE CASCADE
);
CREATE INDEX idx_playlist_tracks_order ON playlist_tracks(playlist_id, sort_order ASC);
```

---

## 3. Media3 Audio Engine & Playback Service

### Lifecycle Isolation
- `AurelPlaybackService` extends `androidx.media3.session.MediaSessionService`.
- A single `ExoPlayer` instance is created inside `onCreate()` and destroyed in `onDestroy()`.
- The service acquires `WAKE_LOCK` and runs as a Foreground Service with notification when playing.
- Foreground notification is managed via `MediaNotification.Provider` ensuring instant response to lockscreen actions.

### Audio Focus & Becoming Noisy
- `AudioAttributes` set to:
  - `USAGE_MEDIA`
  - `CONTENT_TYPE_MUSIC`
- ExoPlayer handles audio focus loss automatically:
  - `AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK`: Volume reduced to 20%
  - `AUDIOFOCUS_LOSS_TRANSIENT`: Pauses playback, automatically resumes on regain
  - `AUDIOFOCUS_LOSS`: Pauses playback, drops foreground notification priority
- BroadcastReceiver intercepts `AudioManager.ACTION_AUDIO_BECOMING_NOISY` and triggers immediate pause.

---

## 4. MediaStore Scanning & Paging Protocol

1. **Permission Strategy**:
   - Android 13+ (API 33+): Request `android.permission.READ_MEDIA_AUDIO`.
   - Android 10-12 (API 29-32): Request `android.permission.READ_EXTERNAL_STORAGE`.
2. **Incremental Indexing**:
   - Query MediaStore for records where `DATE_MODIFIED > last_scan_timestamp`.
   - Query all existing MediaStore IDs and perform set difference to remove files deleted from disk.
   - Batch insert/update Room records in chunks of 500 to prevent SQLite transaction limits.
   - Run on `Dispatchers.IO` via WorkManager or background Coroutine with `Notification` progress updates.
3. **Paging 3 Pipeline**:
   - Room DAO returns `PagingSource<Int, SongEntity>`.
   - Repositories expose `Flow<PagingData<Song>>`.
   - Composables collect via `collectAsLazyPagingItems()` ensuring memory footprint remains bounded even with 50,000 items.

---

## 5. Motion, Springs & Insets

### Motion Curves
- Interruptible spring animations using damped harmonic motion:
  $$m \cdot x'' + c \cdot x' + k \cdot x = 0$$
- Mini-player expansion and modal sheets use:
  - Stiffness: `Spring.StiffnessMediumLow` (400f)
  - Damping Ratio: `Spring.DampingRatioLowBouncy` (0.75f)
- Interactive buttons: Press scale down to `0.97f` on touch down; immediate spring recovery.

### Insets Architecture
- Root `Scaffold` sets `contentWindowInsets = WindowInsets(0, 0, 0, 0)`.
- Mini-player padding calculation:
  $$\text{BottomOffset} = \text{NavigationBarHeight} + \text{BottomBarHeight}$$
- Now Playing is rendered full bleed with `statusBarsPadding()` and `navigationBarsPadding()` applied only to internal control containers.

