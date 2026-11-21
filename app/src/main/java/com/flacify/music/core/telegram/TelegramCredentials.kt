package com.flacify.music.core.telegram

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileNotFoundException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

internal class TelegramCredentials(val apiId: Int, val apiHash: String) {
    init {
        require(apiId > 0 && hashPattern.matches(apiHash)) { "Check the Telegram API ID and hash." }
    }

    companion object {
        val hashPattern = Regex("[0-9a-fA-F]{32}")
    }
}

/** Called on the client's serialized I/O dispatcher. The ciphertext is excluded from backups. */
class TelegramCredentialStore @Inject constructor(@ApplicationContext context: Context) {
    private val file = AtomicFile(File(context.noBackupFilesDir, "telegram-app.enc"))
    private val alias = "flacify.telegram.app"

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(alias, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        }.generateKey()
    }

    internal fun save(value: TelegramCredentials) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val bytes = cipher.doFinal("${value.apiId}\n${value.apiHash}".toByteArray(Charsets.UTF_8))
        val stream = file.startWrite()
        try {
            stream.write(cipher.iv.size)
            stream.write(cipher.iv)
            stream.write(bytes)
            file.finishWrite(stream)
        } catch (e: Exception) {
            file.failWrite(stream)
            throw e
        }
    }

    internal fun load(): TelegramCredentials? {
        val bytes = try {
            file.readFully()
        } catch (e: FileNotFoundException) {
            if (file.baseFile.exists()) throw e else return null
        }
        require(bytes.size > 13 && bytes[0].toInt() == 12) { "Re-enter your Telegram app credentials." }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes, 1, 12))
        val plain = cipher.doFinal(bytes, 13, bytes.size - 13).toString(Charsets.UTF_8)
        return TelegramCredentials(plain.substringBefore('\n').toInt(), plain.substringAfter('\n'))
    }

    internal fun clear() = file.delete()
}
