package com.flacify.music.core.audio.haptics

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.AudioProcessor.AudioFormat
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import java.nio.ByteBuffer

/**
 * Non-destructive, zero-latency PCM tap for Media3 ExoPlayer.
 * Intercepts decoded 16-bit PCM audio frames and routes duplicates into [PcmRingBuffer]
 * while passing the original audio unmodified down the pipeline to AudioTrack.
 */
@UnstableApi
class HapticAudioTapProcessor(
    private val ringBuffer: PcmRingBuffer
) : BaseAudioProcessor() {

    @Volatile
    var isEnabled: Boolean = true

    override fun onConfigure(inputAudioFormat: AudioFormat): AudioFormat {
        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
            throw AudioProcessor.UnhandledAudioFormatException(inputAudioFormat)
        }
        return inputAudioFormat
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val remaining = inputBuffer.remaining()
        if (remaining == 0) return

        if (isEnabled) {
            val duplicate = inputBuffer.duplicate()
            ringBuffer.write(duplicate, inputAudioFormat.channelCount, inputAudioFormat.sampleRate)
        }

        // Pass-through without alteration
        val outputBuffer = replaceOutputBuffer(remaining)
        outputBuffer.put(inputBuffer)
        outputBuffer.flip()
    }

    override fun onFlush() {
        super.onFlush()
        ringBuffer.clear()
    }

    override fun onReset() {
        super.onReset()
        ringBuffer.clear()
    }
}
