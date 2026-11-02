package com.aurel.music.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.aurel.music.core.data.repository.PlaybackRepository
import com.aurel.music.core.data.repository.PlaylistRepository
import com.aurel.music.core.data.repository.SongRepository
import com.aurel.music.core.model.Album
import com.aurel.music.core.model.Artist
import com.aurel.music.core.model.Playlist
import com.aurel.music.core.model.Song
import com.aurel.music.core.model.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LibraryTab(val title: String) {
    PLAYLISTS("Playlists"),
    SONGS("Songs"),
    ALBUMS("Albums"),
    ARTISTS("Artists")
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val playbackRepository: PlaybackRepository,
    private val playlistRepository: PlaylistRepository
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

    val albums: StateFlow<List<Album>> = songRepository.getAllAlbums()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val artists: StateFlow<List<Artist>> = songRepository.getAllArtists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = playlistRepository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlistCount = playlists.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _selectedAlbum = MutableStateFlow<Album?>(null)
    val selectedAlbum: StateFlow<Album?> = _selectedAlbum.asStateFlow()

    val selectedAlbumSongs: StateFlow<List<Song>> = _selectedAlbum
        .flatMapLatest { album ->
            if (album != null) songRepository.getSongsByAlbum(album.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedArtist = MutableStateFlow<Artist?>(null)
    val selectedArtist: StateFlow<Artist?> = _selectedArtist.asStateFlow()

    val selectedArtistSongs: StateFlow<List<Song>> = _selectedArtist
        .flatMapLatest { artist ->
            if (artist != null) songRepository.getSongsByArtist(artist.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

    val selectedPlaylistSongs: StateFlow<List<Song>> = _selectedPlaylist
        .flatMapLatest { playlist ->
            if (playlist != null) playlistRepository.getSongsForPlaylist(playlist.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun selectAlbum(album: Album?) {
        _selectedAlbum.value = album
    }

    fun selectArtist(artist: Artist?) {
        _selectedArtist.value = artist
    }

    fun selectPlaylist(playlist: Playlist?) {
        _selectedPlaylist.value = playlist
    }

    fun createPlaylist(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            playlistRepository.createPlaylist(trimmed)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(playlistId)
            if (_selectedPlaylist.value?.id == playlistId) {
                _selectedPlaylist.value = null
            }
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            playlistRepository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            playlistRepository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun playSong(song: Song) {
        playbackRepository.playSong(song)
    }

    fun playQueue(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isNotEmpty()) {
            val songToPlay = songs.getOrElse(startIndex) { songs.first() }
            playbackRepository.playSong(songToPlay, songs)
        }
    }

    fun toggleFavorite(song: Song) {
        val newFav = !song.isFavorite
        playbackRepository.updateFavoriteStatus(song.id, newFav)
        viewModelScope.launch {
            songRepository.setFavorite(song.id, newFav)
        }
    }

    fun playNext(song: Song) {
        val currentQueue = playbackRepository.queue.value.toMutableList()
        val current = playbackRepository.currentSong.value
        val currentIndex = if (current != null) currentQueue.indexOfFirst { it.id == current.id } else -1
        if (currentIndex >= 0 && currentIndex < currentQueue.size) {
            currentQueue.add(currentIndex + 1, song)
        } else {
            currentQueue.add(song)
        }
        if (current == null) {
            playbackRepository.playSong(song, currentQueue)
        }
    }
}

