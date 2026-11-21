package com.flacify.music.feature.search

import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.delay
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flacify.music.core.data.repository.PlaybackRepository
import com.flacify.music.core.data.repository.SongRepository
import com.flacify.music.core.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class SearchResults(val query: String, val songs: List<Song>)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val playbackRepository: PlaybackRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val currentPlayingSong = playbackRepository.currentSong
    val isPlaying = playbackRepository.isPlaying

    val searchResults = _searchQuery
        .mapLatest { input ->
            val query = input.trim()
            if (query.isEmpty()) SearchResults("", emptyList())
            else {
                delay(300)
                SearchResults(query, songRepository.searchSongs(query))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchResults("", emptyList()))

    fun updateQuery(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun clearQuery() {
        _searchQuery.value = ""
    }

    fun playSong(song: Song) {
        playbackRepository.playSong(song, searchResults.value.songs)
    }
}

