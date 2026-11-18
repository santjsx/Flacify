package com.flacify.music.core.audio.haptics

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

    init {
        require((capacitySamples and mask) == 0) { "Capacity must be a power of two" }
    }

    /**
     * Writes 16-bit interleaved PCM samples, downmixing to mono float on the fly.
     * Drops older samples if the buffer overflows to prevent stalling the audio thread.
     */
    fun write(byteBuffer: ByteBuffer, channels: Int, rate: Int) {
        if (channels <= 0) return
        sampleRate = rate
        val shortBuffer = byteBuffer.asShortBuffer()
        val numFrames = shortBuffer.remaining() / channels
        if (numFrames <= 0) return

        var currentWrite = writeIndex.get()
        val currentRead = readIndex.get()

        for (i in 0 until numFrames) {
            var sum = 0f
            for (c in 0 until channels) {
                sum += shortBuffer.get() / 32768.0f
            }
            val monoSample = sum / channels

            // Drop oldest sample if buffer would overflow
            if ((currentWrite + 1 - currentRead) > buffer.size) {
                readIndex.lazySet(currentRead + 1)
            }

            buffer[currentWrite and mask] = monoSample
            currentWrite++
        }
        writeIndex.lazySet(currentWrite)
    }

    /**
     * Reads up to [size] samples into [destination].
     * Returns the actual number of samples copied.
     */
    fun read(destination: FloatArray, size: Int): Int {
        val currentRead = readIndex.get()
        val currentWrite = writeIndex.get()
        val available = currentWrite - currentRead
        val toRead = minOf(available, size)

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
