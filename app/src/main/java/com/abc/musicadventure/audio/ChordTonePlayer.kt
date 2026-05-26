package com.abc.musicadventure.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.abc.musicadventure.models.ChordType
import com.abc.musicadventure.models.MusicNotes
import kotlin.math.PI
import kotlin.math.sin

class ChordTonePlayer {

    private var audioTrack: AudioTrack? = null
    @Volatile
    private var isPlaying = false

    fun playChord(chord: ChordType, root: String, onComplete: (() -> Unit)? = null) {
        val rootFreq = MusicNotes.notes.firstOrNull { it.name == root }?.frequency
            ?: MusicNotes.notes.first().frequency
        val frequencies = chord.intervals.map { semi ->
            rootFreq * Math.pow(2.0, semi / 12.0).toFloat()
        }
        playArpeggio(frequencies, onComplete)
    }

    fun playBlockChord(chord: ChordType, root: String, onComplete: (() -> Unit)? = null) {
        val rootFreq = MusicNotes.notes.firstOrNull { it.name == root }?.frequency
            ?: MusicNotes.notes.first().frequency
        val frequencies = chord.intervals.map { semi ->
            rootFreq * Math.pow(2.0, semi / 12.0).toFloat()
        }
        playBlock(frequencies, onComplete)
    }

    fun playBlock(frequencies: List<Float>, onComplete: (() -> Unit)? = null) {
        stop()
        if (frequencies.isEmpty()) return

        val sampleRate = SAMPLE_RATE
        val durationMs = BLOCK_MS
        val totalSamples = (sampleRate * durationMs / 1000)
        val buffer = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = envelopeAt(i, totalSamples)
            var mixed = 0.0
            frequencies.forEach { freq ->
                mixed += sin(2.0 * PI * freq * t)
            }
            val normalized = mixed / frequencies.size
            val sample = normalized * envelope * MAX_AMPLITUDE
            buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        playBuffer(buffer, onComplete)
    }

    fun playArpeggio(frequencies: List<Float>, onComplete: (() -> Unit)? = null) {
        stop()
        if (frequencies.isEmpty()) return

        isPlaying = true
        val sampleRate = SAMPLE_RATE
        val noteDurationMs = NOTE_MS
        val gapMs = GAP_MS
        val samplesPerNote = (sampleRate * noteDurationMs / 1000)
        val gapSamples = (sampleRate * gapMs / 1000)
        val totalSamples = frequencies.size * samplesPerNote + (frequencies.size - 1) * gapSamples

        val buffer = ShortArray(totalSamples)
        var offset = 0

        frequencies.forEachIndexed { index, freq ->
            fillSine(buffer, offset, samplesPerNote, freq, sampleRate)
            offset += samplesPerNote
            if (index < frequencies.lastIndex) {
                offset += gapSamples
            }
        }

        playBuffer(buffer, onComplete)
    }

    private fun playBuffer(buffer: ShortArray, onComplete: (() -> Unit)?) {
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack = track
        track.write(buffer, 0, buffer.size)
        track.setNotificationMarkerPosition(buffer.size)
        track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onMarkerReached(track: AudioTrack?) {
                isPlaying = false
                onComplete?.invoke()
            }

            override fun onPeriodicNotification(track: AudioTrack?) = Unit
        })
        track.play()
    }

    fun stop() {
        audioTrack?.let { track ->
            if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                track.stop()
            }
            track.release()
        }
        audioTrack = null
        isPlaying = false
    }

    fun release() = stop()

    private fun fillSine(
        buffer: ShortArray,
        start: Int,
        length: Int,
        frequency: Float,
        sampleRate: Int
    ) {
        for (i in 0 until length) {
            val t = i.toDouble() / sampleRate
            val envelope = envelopeAt(i, length)
            val sample = sin(2.0 * PI * frequency * t) * envelope * MAX_AMPLITUDE
            buffer[start + i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    private fun envelopeAt(sampleIndex: Int, totalSamples: Int): Double {
        val attack = (totalSamples * 0.08).toInt().coerceAtLeast(1)
        val release = (totalSamples * 0.25).toInt().coerceAtLeast(1)
        return when {
            sampleIndex < attack -> sampleIndex.toDouble() / attack
            sampleIndex > totalSamples - release ->
                (totalSamples - sampleIndex).toDouble() / release
            else -> 1.0
        }
    }

    companion object {
        private const val SAMPLE_RATE = 44100
        private const val NOTE_MS = 520
        private const val GAP_MS = 60
        private const val BLOCK_MS = 1600
        private const val MAX_AMPLITUDE = 28000.0
    }
}
