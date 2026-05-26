package com.abc.musicadventure.models

enum class IntervalGroup(
    val id: String,
    val title: String,
    val overview: String
) {
    SECOND("second", "2nds", "The smallest steps — major and minor seconds."),
    THIRD("third", "3rds", "Build triads — major and minor thirds."),
    FOURTH("fourth", "4ths", "Perfect fourth and the tritone."),
    FIFTH("fifth", "5ths", "Perfect fifth — the backbone of harmony."),
    SIXTH("sixth", "6ths", "Major and minor sixth colors."),
    SEVENTH("seventh", "7ths", "Leading tone tension before the octave."),
    OCTAVE("octave", "Octave", "Same note name — eight scale steps apart.");

    companion object {
        fun fromId(id: String): IntervalGroup =
            entries.firstOrNull { it.id == id } ?: SECOND
    }
}

data class IntervalLesson(
    val id: String,
    val group: IntervalGroup,
    val title: String,
    val shortLabel: String,
    val semitones: Int,
    val summary: String,
    val content: String
) {
    companion object {
        val all: List<IntervalLesson> = listOf(
            interval("m2", IntervalGroup.SECOND, "Minor 2nd", "m2", 1,
                "One half step — very dissonant, tight tension.",
                "A minor second is one semitone (half step). C to D♭ is a minor second. It sounds extremely close and tense — think the Jaws theme or a chromatic passing tone."),
            interval("M2", IntervalGroup.SECOND, "Major 2nd", "M2", 2,
                "Two half steps — whole step, scale step feel.",
                "A major second spans two semitones (a whole step). C to D is a major second. It is the distance between adjacent notes in a major scale."),
            interval("m3", IntervalGroup.THIRD, "Minor 3rd", "m3", 3,
                "Three half steps — minor chord color.",
                "A minor third is three semitones. C to E♭ is a minor third — the bottom of a minor triad. It feels darker than a major third."),
            interval("M3", IntervalGroup.THIRD, "Major 3rd", "M3", 4,
                "Four half steps — major chord color.",
                "A major third is four semitones. C to E is a major third — the bright third of a major triad."),
            interval("P4", IntervalGroup.FOURTH, "Perfect 4th", "P4", 5,
                "Five half steps — open, stable fourth.",
                "A perfect fourth is five semitones. C to F is a perfect fourth. It sounds open and is common in sus4 chords and folk harmony."),
            interval("tri", IntervalGroup.FOURTH, "Tritone", "♯4 / ♭5", 6,
                "Six half steps — the devil in music, unstable.",
                "The tritone (augmented 4th or diminished 5th) is six semitones — exactly half an octave. C to F♯ or C to G♭. Maximum tension; wants to resolve."),
            interval("P5", IntervalGroup.FIFTH, "Perfect 5th", "P5", 7,
                "Seven half steps — power chord, strong stability.",
                "A perfect fifth is seven semitones. C to G is a perfect fifth — the sound of a power chord and the strongest consonance after the octave."),
            interval("m6", IntervalGroup.SIXTH, "Minor 6th", "m6", 8,
                "Eight half steps — melancholic leap.",
                "A minor sixth is eight semitones. C to A♭. Heard in minor-key melodies and some chord extensions."),
            interval("M6", IntervalGroup.SIXTH, "Major 6th", "M6", 9,
                "Nine half steps — sweet, lyrical leap.",
                "A major sixth is nine semitones. C to A. Common in romantic melodies and added sixth chords."),
            interval("m7", IntervalGroup.SEVENTH, "Minor 7th", "m7", 10,
                "Ten half steps — dominant seventh below octave.",
                "A minor seventh is ten semitones. C to B♭ — the seventh above the root in a dominant 7th chord (C7)."),
            interval("M7", IntervalGroup.SEVENTH, "Major 7th", "M7", 11,
                "Eleven half steps — one half step below octave.",
                "A major seventh is eleven semitones. C to B — the lush leading tone of a maj7 chord, one half step from the octave."),
            interval("P8", IntervalGroup.OCTAVE, "Octave", "P8", 12,
                "Twelve half steps — same pitch class, higher register.",
                "An octave is twelve semitones. C to C — the same note name, double frequency. Ultimate consonance; melodies often outline octaves.")
        )

        fun forGroup(group: IntervalGroup): List<IntervalLesson> =
            all.filter { it.group == group }

        private fun interval(
            id: String,
            group: IntervalGroup,
            title: String,
            shortLabel: String,
            semitones: Int,
            summary: String,
            content: String
        ) = IntervalLesson(id, group, title, shortLabel, semitones, summary, content)
    }
}
