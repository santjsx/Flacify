package com.aurel.music.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.aurel.music.core.data.repository.PlaybackRepository
import com.aurel.music.core.data.repository.SongRepository
import com.aurel.music.core.model.Song
import com.aurel.music.core.model.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LibraryTab(val title: String) {
    SONGS("Songs"),
    ALBUMS("Albums"),
    ARTISTS("Artists")
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val playbackRepository: PlaybackRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(LibraryTab.SONGS)
    val selectedTab: StateFlow<LibraryTab> = _selectedTab.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.TITLE_ASC)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    val songCount = songRepository.getSongCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val albumCount = songRepository.getAlbumCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val artistCount = songRepository.getArtistCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val currentPlayingSong = playbackRepository.currentSong
    val isPlaying = playbackRepository.isPlaying

    val pagedSongs: Flow<PagingData<Song>> = _sortOption
        .flatMapLatest { sort ->
            songRepository.getSongsPaged(sort)
        }
        .cachedIn(viewModelScope)

    fun selectTab(tab: LibraryTab) {
        _selectedTab.value = tab
    }

    fun selectSortOption(sort: SortOption) {
        _sortOption.value = sort
    }

    fun playSong(song: Song) {
        playbackRepository.playSong(song)
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            songRepository.setFavorite(song.id, !song.isFavorite)
        }
    }
}

