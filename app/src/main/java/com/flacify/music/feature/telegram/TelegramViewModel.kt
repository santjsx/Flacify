package com.flacify.music.feature.telegram

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flacify.music.core.data.repository.PlaybackRepository
import com.flacify.music.core.telegram.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.drinkless.tdlib.TdApi
import javax.inject.Inject

@HiltViewModel
class TelegramViewModel @Inject constructor(
    private val client: TelegramClientManager,
    private val repository: TelegramChannelRepository,
    private val cache: TelegramCacheManager,
    private val playback: PlaybackRepository
) : ViewModel() {
    val auth = client.auth
    private val selected = MutableStateFlow<TelegramChannel?>(null)
    val channel = selected.asStateFlow()
    val channels = auth.flatMapLatest { state ->
        if (state is TelegramAuthState.Ready) repository.dao.channels(state.accountId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val tracks = selected.flatMapLatest { channel ->
        if (channel == null) flowOf(emptyList()) else repository.dao.tracks(channel.accountId, channel.chatId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val playingSong = playback.currentSong
    val playbackState = playback.playbackState
    val playing = playback.isPlaying
    private val _status = MutableStateFlow("")
    val status = _status.asStateFlow()
    private val _working = MutableStateFlow(false)
    val working = _working.asStateFlow()
    private val _size = MutableStateFlow<Long?>(null)
    val size = _size.asStateFlow()
    private val _hasMore = MutableStateFlow(false)
    val hasMore = _hasMore.asStateFlow()
    private var cursor = 0L
    private var job: Job? = null

    init {
        viewModelScope.launch {
            auth.collectLatest { state ->
                if (state is TelegramAuthState.Ready) {
                    try {
                        _size.value = cache.sizeBytes()
                        repository.getUserChannels()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        if (selected.value == null) _status.value = e.message ?: "Unable to refresh channels."
                    }
                } else {
                    if (state == TelegramAuthState.LoggingOut || state == TelegramAuthState.Closed) job?.cancel()
                    selected.value = null
                    _size.value = null
                    _hasMore.value = false
                }
            }
        }
    }

    // Native callbacks resume on the caller's dispatcher. Room performs its own I/O.
    private fun perform(action: suspend () -> Unit) {
        if (job?.isActive == true) return
        _working.value = true
        _status.value = ""
        job = viewModelScope.launch {
            try {
                action()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _status.value = e.message ?: "Telegram request failed. Try again."
            } finally {
                _working.value = false
            }
        }
    }

    fun connect() = perform { client.start() }
    fun configure(id: Int, hash: String) = perform { client.configure(id, hash) }
    fun submit(input: String) = perform {
        when (auth.value) {
            TelegramAuthState.WaitPhoneNumber -> client.sendPhoneNumber(input.trim())
            is TelegramAuthState.WaitCode -> client.checkAuthCode(input.trim())
            is TelegramAuthState.WaitPassword -> client.checkAuthPassword(input)
            TelegramAuthState.WaitEmail -> client.send(TdApi.SetAuthenticationEmailAddress(input.trim()))
            TelegramAuthState.WaitEmailCode -> client.send(TdApi.CheckAuthenticationEmailCode(TdApi.EmailAddressAuthenticationCode(input.trim())))
            else -> Unit
        }
    }
    fun refreshChannels() = perform {
        repository.getUserChannels()
        _size.value = cache.sizeBytes()
    }
    fun resolve(input: String) = perform { show(repository.resolvePublicChannel(input)) }
    fun select(channel: TelegramChannel) = perform { show(channel) }
    private suspend fun show(channel: TelegramChannel) {
        if (channel.accountId != client.account()) throw java.io.IOException("Select a channel from the connected account.")
        selected.value = channel
        cursor = 0
        _hasMore.value = false
        load(channel)
    }
    fun backToChannels() {
        job?.cancel()
        selected.value = null
        _status.value = ""
        _hasMore.value = false
    }
    private suspend fun load(channel: TelegramChannel) {
        val next = repository.page(channel, cursor)
        currentCoroutineContext().ensureActive()
        cursor = next
        _hasMore.value = next != 0L
    }
    fun loadNext() {
        val channel = selected.value ?: return
        if (_hasMore.value) perform { load(channel) }
    }
    fun refresh(all: Boolean = false) {
        val channel = selected.value ?: return
        perform {
            cursor = 0
            do {
                load(channel)
            } while (all && _hasMore.value)
            _status.value = if (all) "Metadata sync complete" else "Tracks refreshed"
        }
    }
    fun play(track: TelegramTrack) {
        val channel = selected.value ?: return
        val queue = tracks.value.filter { it.metadata.chatId == channel.chatId && it.metadata.accountId == channel.accountId }
        val index = queue.indexOfFirst { it.playbackId == track.playbackId }
        if (index >= 0) playback.playQueue(queue.map { it.toSong(channel.title) }, index)
    }
    fun clearCache() = perform {
        cache.clear()
        _size.value = cache.sizeBytes()
        _status.value = "Telegram media cache cleared"
    }
    fun logout() = perform { client.logout() }
}
