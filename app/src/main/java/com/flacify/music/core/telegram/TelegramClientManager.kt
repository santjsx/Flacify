package com.flacify.music.core.telegram

import android.content.Context
import android.os.Build
import com.flacify.music.BuildConfig
import com.flacify.music.core.data.repository.PlaybackRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi
import java.io.File
import java.io.IOException
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

sealed interface TelegramAuthState {
    data object Uninitialized : TelegramAuthState
    data object WaitAppCredentials : TelegramAuthState
    data object Connecting : TelegramAuthState
    data object WaitPhoneNumber : TelegramAuthState
    data class WaitCode(val codeInfo: TdApi.AuthenticationCodeInfo) : TelegramAuthState
    data class WaitPassword(val hint: String) : TelegramAuthState
    data object WaitEmail : TelegramAuthState
    data object WaitEmailCode : TelegramAuthState
    data class Ready(val accountId: Long, val displayName: String) : TelegramAuthState
    data object LoggingOut : TelegramAuthState
    data object Closed : TelegramAuthState
    data class Notice(val message: String) : TelegramAuthState
}

class TelegramError(val code: Int, detail: String) : IOException(
    when {
        code == 429 || detail.contains("FLOOD_WAIT") -> "Too many attempts. " +
            (Regex("\\d+").find(detail)?.value?.let { "Try again in $it seconds." } ?: "Please try again later.")
        detail.contains("PHONE_CODE_INVALID") -> "That code is incorrect. Check the latest Telegram code."
        detail.contains("PASSWORD_HASH_INVALID") -> "Incorrect two-step verification password."
        detail.contains("PHONE_NUMBER_INVALID") -> "Enter a phone number with its country code."
        else -> detail
    }
)

@Singleton
class TelegramClientManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: TelegramDao,
    private val playback: PlaybackRepository,
    private val credentials: TelegramCredentialStore
) {
    private val dispatcher = Dispatchers.IO.limitedParallelism(1)
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    @Volatile private var client: Client? = null
    private var generation = 0L
    private var lastAccount: Long? = null
    private val _auth = MutableStateFlow<TelegramAuthState>(TelegramAuthState.Uninitialized)
    val auth = _auth.asStateFlow()
    private val _updates = MutableSharedFlow<TdApi.Update>(extraBufferCapacity = 128, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val updates = _updates.asSharedFlow()

    // Watch only files with a live reader. A StateFlow retains updates received between reads.
    internal class Watch(val fileId: Int) { val latest = MutableStateFlow<TdApi.File?>(null) }
    private val watchers = CopyOnWriteArrayList<Watch>()
    private var clearingCache = false

    @Synchronized internal fun watch(id: Int): Watch {
        if (clearingCache) throw IOException("Telegram media cache is being cleared.")
        return Watch(id).also(watchers::add)
    }

    @Synchronized internal fun release(watch: Watch) {
        watchers.remove(watch)
        if (watchers.none { it.fileId == watch.fileId } && auth.value is TelegramAuthState.Ready) {
            client?.send(TdApi.CancelDownloadFile(watch.fileId, false), null)
        }
    }

    internal suspend fun <T> withIdleCache(block: suspend () -> T): T {
        synchronized(this) {
            if (clearingCache || watchers.isNotEmpty()) throw IOException("Finish Telegram playback before clearing the cache.")
            clearingCache = true
        }
        return try { block() } finally { synchronized(this) { clearingCache = false } }
    }

    suspend fun start() = withContext(dispatcher) {
        if (client != null) return@withContext
        val saved = try {
            credentials.load()
        } catch (e: Exception) {
            _auth.value = TelegramAuthState.WaitAppCredentials
            throw IOException("Re-enter your Telegram app credentials to reconnect.", e)
        }
        val app = saved ?: if (BuildConfig.TELEGRAM_API_ID > 0 && BuildConfig.TELEGRAM_API_HASH.isNotEmpty())
            TelegramCredentials(BuildConfig.TELEGRAM_API_ID, BuildConfig.TELEGRAM_API_HASH) else null
        if (app == null) {
            _auth.value = TelegramAuthState.WaitAppCredentials
            return@withContext
        }
        _auth.value = TelegramAuthState.Connecting
        val version = ++generation
        try {
            Client.execute(TdApi.SetLogVerbosityLevel(0))
            client = Client.create({ result ->
                if (result is TdApi.Update) scope.launch {
                    if (generation != version) return@launch
                    _updates.tryEmit(result)
                    when (result) {
                        is TdApi.UpdateFile -> watchers.forEach {
                            if (it.fileId == result.file.id) it.latest.value = result.file
                        }
                        is TdApi.UpdateAuthorizationState -> handleAuth(result.authorizationState, version, app)
                    }
                }
            }, null, null)
        } catch (e: LinkageError) {
            _auth.value = TelegramAuthState.Notice("Telegram’s native library could not start on this device.")
        }
    }

    suspend fun configure(id: Int, hash: String) = withContext(dispatcher) {
        check(client == null && auth.value == TelegramAuthState.WaitAppCredentials)
        credentials.save(TelegramCredentials(id, hash))
        start()
    }

    private suspend fun handleAuth(state: TdApi.AuthorizationState, version: Long, app: TelegramCredentials) {
        try {
            when (state) {
                is TdApi.AuthorizationStateWaitTdlibParameters -> send(TdApi.SetTdlibParameters().apply {
                    databaseDirectory = File(context.noBackupFilesDir, "tdlib/database").absolutePath
                    filesDirectory = File(context.noBackupFilesDir, "tdlib/files").absolutePath
                    databaseEncryptionKey = byteArrayOf()
                    useFileDatabase = true
                    useChatInfoDatabase = true
                    useMessageDatabase = true
                    apiId = app.apiId
                    apiHash = app.apiHash
                    systemLanguageCode = Locale.getDefault().language.ifBlank { "en" }
                    deviceModel = Build.MODEL
                    systemVersion = Build.VERSION.RELEASE
                    applicationVersion = BuildConfig.VERSION_NAME
                })
                is TdApi.AuthorizationStateWaitPhoneNumber -> _auth.value = TelegramAuthState.WaitPhoneNumber
                is TdApi.AuthorizationStateWaitCode -> _auth.value = TelegramAuthState.WaitCode(state.codeInfo)
                is TdApi.AuthorizationStateWaitPassword -> _auth.value = TelegramAuthState.WaitPassword(state.passwordHint)
                is TdApi.AuthorizationStateWaitEmailAddress -> _auth.value = TelegramAuthState.WaitEmail
                is TdApi.AuthorizationStateWaitEmailCode -> _auth.value = TelegramAuthState.WaitEmailCode
                is TdApi.AuthorizationStateReady -> {
                    _auth.value = TelegramAuthState.Connecting
                    val me = send(TdApi.GetMe())
                    if (version == generation && _auth.value == TelegramAuthState.Connecting) {
                        lastAccount = me.id
                        val name = me.usernames?.activeUsernames?.firstOrNull()?.let { "@$it" } ?: "+${me.phoneNumber}"
                        _auth.value = TelegramAuthState.Ready(me.id, name)
                    }
                }
                is TdApi.AuthorizationStateLoggingOut -> {
                    _auth.value = TelegramAuthState.LoggingOut
                    credentials.clear()
                    lastAccount?.let { dao.forget(it) }
                    withContext(Dispatchers.Main.immediate) {
                        playback.queue.value.indices.reversed().forEach { i ->
                            if (playback.queue.value.getOrNull(i)?.mediaUri?.startsWith("telegram://") == true) playback.removeFromQueue(i)
                        }
                    }
                }
                is TdApi.AuthorizationStateClosing -> _auth.value = TelegramAuthState.LoggingOut
                is TdApi.AuthorizationStateClosed -> {
                    generation++
                    client = null
                    lastAccount = null
                    _auth.value = TelegramAuthState.Closed
                }
                else -> _auth.value = TelegramAuthState.Notice("This account requires another authorization step. Complete it in the official Telegram app, then reconnect.")
            }
        } catch (e: IOException) {
            if (version == generation) _auth.value = TelegramAuthState.Notice(e.message ?: "Telegram connection failed.")
        }
    }

    // Client.send assigns its own query ID and dispatches exactly the corresponding handler.
    suspend fun <T : TdApi.Object> send(function: TdApi.Function<T>): T = suspendCancellableCoroutine { continuation ->
        val active = client
        if (active == null) {
            continuation.resumeWithException(IOException("Telegram is disconnected."))
        } else active.send(function) { result ->
            if (continuation.isActive) {
                if (result is TdApi.Error) continuation.resumeWithException(TelegramError(result.code, result.message))
                else {
                    @Suppress("UNCHECKED_CAST")
                    continuation.resume(result as T)
                }
            }
        }
    }

    fun account() = (auth.value as? TelegramAuthState.Ready)?.accountId ?: throw IOException("Connect to Telegram first.")
    suspend fun sendPhoneNumber(phone: String) = send(TdApi.SetAuthenticationPhoneNumber(phone, null))
    suspend fun checkAuthCode(code: String) = send(TdApi.CheckAuthenticationCode(code))
    suspend fun checkAuthPassword(password: String) = send(TdApi.CheckAuthenticationPassword(password))
    suspend fun logout() = withContext(dispatcher) {
        if (client == null) {
            credentials.clear()
            _auth.value = TelegramAuthState.WaitAppCredentials
        } else {
            send(TdApi.LogOut())
            Unit
        }
    }
}
