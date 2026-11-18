package com.flacify.music.core.audio.haptics

import androidx.media3.common.C
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicInteger

/**
 * Lock-free, wait-free Single Producer Single Consumer (SPSC) circular buffer
 * designed for real-time streaming of mono-downmixed audio samples from the
 * ExoPlayer AudioProcessor render thread to the DSP analysis thread.
 */
class PcmRingBuffer(capacitySamples: Int = 16384) {
    private val buffer = FloatArray(capacitySamples)
    private val mask = capacitySamples - 1
    private val writeIndex = AtomicInteger(0)
    private val readIndex = AtomicInteger(0)

    @Volatile
    var sampleRate: Int = 44100
        private set

    val availableRead: Int
        get() = (writeIndex.get() - readIndex.get()).coerceAtLeast(0)

    init {
        require((capacitySamples and mask) == 0) { "Capacity must be a power of two" }
    }

    /**
     * Writes interleaved PCM samples (16-bit, float, or 24-bit), downmixing to mono float on the fly.
     * Wrapped in defensive try-catch so it NEVER interrupts or throws on the audio thread.
     */
    fun write(
        byteBuffer: ByteBuffer,
        channels: Int,
        rate: Int,
        encoding: Int = C.ENCODING_PCM_16BIT
    ) {
        if (channels <= 0 || byteBuffer.remaining() <= 0) return
        sampleRate = rate

        try {
            val dup = byteBuffer.duplicate().order(byteBuffer.order())
            var currentWrite = writeIndex.get()
            val currentRead = readIndex.get()

            when (encoding) {
                C.ENCODING_PCM_FLOAT -> {
                    val floatBuf = dup.asFloatBuffer()
                    val numFrames = floatBuf.remaining() / channels
                    for (i in 0 until numFrames) {
                        var sum = 0f
                        for (c in 0 until channels) {
                            sum += floatBuf.get()
                        }
                        buffer[currentWrite and mask] = sum / channels
                        currentWrite++
                    }
                }
                C.ENCODING_PCM_24BIT -> {
                    val bytesPerFrame = channels * 3
                    val numFrames = dup.remaining() / bytesPerFrame
                    for (i in 0 until numFrames) {
                        var sum = 0f
                        for (c in 0 until channels) {
                            val b0 = dup.get().toInt() and 0xFF
                            val b1 = dup.get().toInt() and 0xFF
                            val b2 = dup.get().toInt() // sign-extended
                            val sample = (b2 shl 16) or (b1 shl 8) or b0
                            sum += sample / 8388608.0f
                        }
                        buffer[currentWrite and mask] = sum / channels
                        currentWrite++
                    }
                }
                else -> { // Default 16-bit PCM (C.ENCODING_PCM_16BIT)
                    val shortBuf = dup.asShortBuffer()
                    val numFrames = shortBuf.remaining() / channels
                    for (i in 0 until numFrames) {
                        var sum = 0f
                        for (c in 0 until channels) {
                            sum += shortBuf.get() / 32768.0f
                        }
                        buffer[currentWrite and mask] = sum / channels
                        currentWrite++
                    }
                }
            }

            // Drop oldest samples if writeIndex advanced beyond buffer capacity relative to readIndex
            if (currentWrite - currentRead > buffer.size) {
                readIndex.lazySet(currentWrite - buffer.size)
            }
            writeIndex.lazySet(currentWrite)
        } catch (_: Throwable) {
            // Never let buffer errors disrupt the audio playback thread
        }
    }

    /**
     * Reads up to [size] samples into [destination].
     * Returns the actual number of samples copied.
     */
    fun read(destination: FloatArray, size: Int): Int {
        val currentRead = readIndex.get()
        val currentWrite = writeIndex.get()
        val available = currentWrite - currentRead
        if (available <= 0) return 0
        val toRead = minOf(available, size, destination.size)

        for (i in 0 until toRead) {
            destination[i] = buffer[(currentRead + i) and mask]
        }

        readIndex.lazySet(currentRead + toRead)
        return toRead
    }

    fun clear() {
        writeIndex.set(0)
        readIndex.set(0)
    }
}
