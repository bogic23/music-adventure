package com.abc.musicadventure.models

data class MusicNote(
    val name: String,
    val frequency: Float,
    val color: Int,
    val symbol: String
)

object MusicNotes {
    val notes = listOf(
        MusicNote("C", 261.63f, android.graphics.Color.parseColor("#FF0000"), "🎵"),
        MusicNote("D", 293.66f, android.graphics.Color.parseColor("#FF7F00"), "🎶"),
        MusicNote("E", 329.63f, android.graphics.Color.parseColor("#FFFF00"), "🎼"),
        MusicNote("F", 349.23f, android.graphics.Color.parseColor("#00FF00"), "🎹"),
        MusicNote("G", 392.00f, android.graphics.Color.parseColor("#0000FF"), "🎸"),
        MusicNote("A", 440.00f, android.graphics.Color.parseColor("#4B0082"), "🎺"),
        MusicNote("B", 493.88f, android.graphics.Color.parseColor("#9400D3"), "🥁")
    )

    fun getRandomNotes(count: Int): List<MusicNote> {
        return notes.shuffled().take(count)
    }
}