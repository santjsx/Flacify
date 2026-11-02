package com.flacify.music.core.data.repository

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.flacify.music.core.data.lyrics.LrcParser
import com.flacify.music.core.data.lyrics.LrclibService
import com.flacify.music.core.data.lyrics.Lyrics
import com.flacify.music.core.database.dao.LyricsDao
import com.flacify.music.core.database.entity.SongLyricsEntity
import com.flacify.music.core.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lyricsDao: LyricsDao,
    private val lrclibService: LrclibService
) : LyricsRepository {

    override fun getLyrics(song: Song): Flow<LyricsResult> = flow {
        emit(LyricsResult.Loading)

        // Tier 1: Check Room Cache for instantaneous 0ms display
        val cached = lyricsDao.getLyrics(song.id)
        if (cached != null) {
            if (cached.isInstrumental) {
                emit(LyricsResult.Success(Lyrics(emptyList(), isSynchronized = false), isInstrumental = true, source = cached.source))
                return@flow
            }
            val raw = cached.syncedLyrics ?: cached.plainLyrics
            if (!raw.isNullOrBlank()) {
                val parsed = LrcParser.parse(raw)
                if (parsed.lines.isNotEmpty()) {
                    emit(LyricsResult.Success(parsed, isInstrumental = false, source = cached.source))
                    return@flow
                }
            }
        }

        // Tier 2: Check Local Sidecar (.lrc / .txt) Files
        val localFileLyrics = findLocalSidecarLyrics(song)
        if (localFileLyrics != null) {
            val parsed = LrcParser.parse(localFileLyrics)
            if (parsed.lines.isNotEmpty()) {
                lyricsDao.upsertLyrics(
                    SongLyricsEntity(
                        songId = song.id,
                        syncedLyrics = if (parsed.isSynchronized) localFileLyrics else null,
                        plainLyrics = if (!parsed.isSynchronized) localFileLyrics else null,
                        isInstrumental = false,
                        source = "local_sidecar"
                    )
                )
                emit(LyricsResult.Success(parsed, isInstrumental = false, source = "local_sidecar"))
                return@flow
            }
        }

        // Tier 3: Remote Fetch from LRCLIB
        val remoteLyrics = fetchFromLrclib(song)
        if (remoteLyrics != null) {
            emit(remoteLyrics)
            return@flow
        }

        // Tier 4: Empty Fallback
        emit(LyricsResult.Empty("No lyrics found for \"${song.title}\""))
    }.flowOn(Dispatchers.IO)

    override suspend fun importLyrics(songId: Long, lrcContent: String): LyricsResult = withContext(Dispatchers.IO) {
        try {
            val parsed = LrcParser.parse(lrcContent)
            if (parsed.lines.isEmpty()) {
                return@withContext LyricsResult.Empty("Imported file contains no valid lyric lines")
            }
            lyricsDao.upsertLyrics(
                SongLyricsEntity(
                    songId = songId,
                    syncedLyrics = if (parsed.isSynchronized) lrcContent else null,
                    plainLyrics = if (!parsed.isSynchronized) lrcContent else null,
                    isInstrumental = false,
                    source = "user_import"
                )
            )
            LyricsResult.Success(parsed, isInstrumental = false, source = "user_import")
        } catch (e: Exception) {
            LyricsResult.Empty(e.message ?: "Failed to parse imported lyrics")
        }
    }

    private fun findLocalSidecarLyrics(song: Song): String? {
        try {
            val audioPath = resolveFilePath(song.mediaUri) ?: return null
            val audioFile = File(audioPath)
            if (!audioFile.exists()) return null

            val parentDir = audioFile.parentFile ?: return null

            // 1. Exact match with same basename: Song.lrc
            val sidecarLrc = File(parentDir, "${audioFile.nameWithoutExtension}.lrc")
            if (sidecarLrc.exists() && sidecarLrc.canRead()) {
                val text = sidecarLrc.readText()
                if (text.isNotBlank()) return text
            }

            // 2. Exact match with same basename: Song.txt
            val sidecarTxt = File(parentDir, "${audioFile.nameWithoutExtension}.txt")
            if (sidecarTxt.exists() && sidecarTxt.canRead()) {
                val text = sidecarTxt.readText()
                if (text.isNotBlank()) return text
            }

            // 3. Sibling file matching track title: <title>.lrc
            val titleLrc = File(parentDir, "${song.title}.lrc")
            if (titleLrc.exists() && titleLrc.canRead()) {
                val text = titleLrc.readText()
                if (text.isNotBlank()) return text
            }

            // 4. Case-insensitive search in directory
            val siblingFiles = parentDir.listFiles() ?: emptyArray()
            val targetBaseNames = setOf(
                audioFile.nameWithoutExtension.lowercase(),
                song.title.lowercase().trim()
            )
            for (file in siblingFiles) {
                if (file.isFile && (file.extension.equals("lrc", ignoreCase = true) || file.extension.equals("txt", ignoreCase = true))) {
                    if (targetBaseNames.contains(file.nameWithoutExtension.lowercase())) {
                        val text = file.readText()
                        if (text.isNotBlank()) return text
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore filesystem access exceptions
        }
        return null
    }

    private fun resolveFilePath(mediaUriString: String): String? {
        try {
            val uri = Uri.parse(mediaUriString)
            if (uri.scheme == "file") {
                return uri.path
            }
            if (uri.scheme == "content") {
                val projection = arrayOf(MediaStore.Audio.Media.DATA)
                context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                    if (dataCol != -1 && cursor.moveToFirst()) {
                        return cursor.getString(dataCol)
                    }
                }
            }
        } catch (_: Exception) {
            // ContentResolver query failed
        }
        return null
    }

    private suspend fun fetchFromLrclib(song: Song): LyricsResult? {
        val sanitizedTitle = sanitizeTitle(song.title)
        val sanitizedArtist = sanitizeArtist(song.artistName)
        val durationSeconds = if (song.durationMs > 0) song.durationMs / 1000 else null

        // Attempt 1: Exact GET with track_name, artist_name, album_name, duration
        val resp1 = runCatching {
            lrclibService.getLyrics(
                trackName = sanitizedTitle,
                artistName = sanitizedArtist,
                albumName = song.albumTitle.takeIf { it.isNotBlank() && !it.contains("unknown", ignoreCase = true) },
                durationSeconds = durationSeconds
            )
        }.getOrNull()

        if (resp1 != null && resp1.isSuccessful && resp1.body() != null) {
            val body = resp1.body()!!
            return processLrclibResponse(song.id, body)
        }

        // Attempt 2: Relaxed GET with raw title and artist without duration / album
        if (sanitizedTitle != song.title || sanitizedArtist != song.artistName) {
            val resp2 = runCatching {
                lrclibService.getLyrics(
                    trackName = song.title,
                    artistName = song.artistName
                )
            }.getOrNull()

            if (resp2 != null && resp2.isSuccessful && resp2.body() != null) {
                return processLrclibResponse(song.id, resp2.body()!!)
            }
        }

        // Attempt 3: Fuzzy search endpoint
        val searchResp = runCatching {
            lrclibService.searchLyrics(query = "$sanitizedTitle $sanitizedArtist")
        }.getOrNull()

        if (searchResp != null && searchResp.isSuccessful && !searchResp.body().isNullOrEmpty()) {
            val results = searchResp.body()!!
            // Prefer results with synchronized lyrics
            val bestMatch = results.firstOrNull { !it.syncedLyrics.isNullOrBlank() }
                ?: results.firstOrNull { !it.plainLyrics.isNullOrBlank() || it.instrumental }
                ?: results.first()

            return processLrclibResponse(song.id, bestMatch)
        }

        return null
    }

    private suspend fun processLrclibResponse(
        songId: Long,
        body: com.flacify.music.core.data.lyrics.LrclibResponse
    ): LyricsResult {
        if (body.instrumental) {
            lyricsDao.upsertLyrics(
                SongLyricsEntity(
                    songId = songId,
                    syncedLyrics = null,
                    plainLyrics = null,
                    isInstrumental = true,
                    source = "lrclib"
                )
            )
            return LyricsResult.Success(
                Lyrics(emptyList(), isSynchronized = false),
                isInstrumental = true,
                source = "lrclib"
            )
        }

        val synced = body.syncedLyrics
        val plain = body.plainLyrics
        val raw = synced ?: plain

        if (!raw.isNullOrBlank()) {
            val parsed = LrcParser.parse(raw)
            if (parsed.lines.isNotEmpty()) {
                lyricsDao.upsertLyrics(
                    SongLyricsEntity(
                        songId = songId,
                        syncedLyrics = synced,
                        plainLyrics = plain,
                        isInstrumental = false,
                        source = "lrclib"
                    )
                )
                return LyricsResult.Success(
                    parsed,
                    isInstrumental = false,
                    source = "lrclib"
                )
            }
        }

        return LyricsResult.Empty("No lyrics content found")
    }

    private fun sanitizeTitle(title: String): String {
        return title
            .replace(Regex("""\s*[\(\[](feat\.|ft\.|with|remastered|bonus track|official|deluxe|version).*?[\)\]]""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*-\s*(feat\.|ft\.|remastered|bonus track|official|single|version).*$""", RegexOption.IGNORE_CASE), "")
            .trim()
    }

    private fun sanitizeArtist(artist: String): String {
        return artist
            .split(Regex("[,&/]|\\bfeat\\.\\b|\\bft\\.\\b", RegexOption.IGNORE_CASE))
            .firstOrNull()?.trim() ?: artist
    }
}

