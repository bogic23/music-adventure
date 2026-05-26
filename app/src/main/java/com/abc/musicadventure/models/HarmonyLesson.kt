package com.abc.musicadventure.models

enum class HarmonyTopic(
    val id: String,
    val title: String,
    val overview: String
) {
    TONIC("tonic", "Tonic (I)", "Home chord — stable resolution point."),
    SUBDOMINANT("subdominant", "Subdominant (IV)", "Pre-dominant color — moves away from tonic."),
    DOMINANT("dominant", "Dominant (V)", "Tension chord — pulls back to tonic."),
    PROGRESSION("progression", "Progressions", "Common chord movement in a key.");

    companion object {
        fun fromId(id: String): HarmonyTopic =
            entries.firstOrNull { it.id == id } ?: TONIC
    }
}

data class HarmonyLesson(
    val id: String,
    val topic: HarmonyTopic,
    val title: String,
    val summary: String,
    val content: String
) {
    companion object {
        val all: List<HarmonyLesson> = listOf(
            HarmonyLesson(
                id = "tonic_i",
                topic = HarmonyTopic.TONIC,
                title = "Tonic (I)",
                summary = "The home chord in a key.",
                content = "In C major, the tonic chord is C major (I). It sounds resolved and is where progressions often end."
            ),
            HarmonyLesson(
                id = "sub_iv",
                topic = HarmonyTopic.SUBDOMINANT,
                title = "Subdominant (IV)",
                summary = "Moves away from home before dominant tension.",
                content = "In C major, F major is the IV chord. It often precedes V and adds a sense of departure from tonic."
            ),
            HarmonyLesson(
                id = "dom_v",
                topic = HarmonyTopic.DOMINANT,
                title = "Dominant (V)",
                summary = "Creates tension that wants to resolve to I.",
                content = "In C major, G major (or G7) is V. The leading tone pulls strongly back to the tonic."
            ),
            HarmonyLesson(
                id = "ii_v_i",
                topic = HarmonyTopic.PROGRESSION,
                title = "ii – V – I",
                summary = "The most common jazz and classical cadence.",
                content = "In C major: Dm7 → G7 → Cmaj7. ii sets up V, and V resolves to I."
            ),
            HarmonyLesson(
                id = "vi_iv",
                topic = HarmonyTopic.PROGRESSION,
                title = "vi – IV – I – V",
                summary = "Popular pop progression.",
                content = "In C major: Am → F → C → G. Also called the 'sensitive female chord progression' in pop analysis."
            ),
            HarmonyLesson(
                id = "cadence",
                topic = HarmonyTopic.DOMINANT,
                title = "Authentic Cadence",
                summary = "V (or V7) resolving to I.",
                content = "A perfect authentic cadence is V–I with both chords in root position. It is the strongest sense of closure in tonal music."
            )
        )
    }
}
