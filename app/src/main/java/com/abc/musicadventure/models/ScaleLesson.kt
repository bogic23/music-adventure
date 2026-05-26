package com.abc.musicadventure.models

enum class ScaleCategory(
    val id: String,
    val title: String,
    val overview: String
) {
    DIATONIC(
        "diatonic", "Major / Minor",
        "The core Western scales — major and the three minor forms."
    ),
    MODES(
        "modes", "Modes",
        "Seven rotations of the major scale — each with its own color."
    );

    companion object {
        fun fromId(id: String): ScaleCategory =
            entries.firstOrNull { it.id == id } ?: DIATONIC
    }
}

data class ScaleLesson(
    val id: String,
    val category: ScaleCategory,
    val title: String,
    val pattern: String,
    val summary: String,
    val content: String
) {
    companion object {
        val all: List<ScaleLesson> = listOf(
            scale("major", ScaleCategory.DIATONIC, "Major (Ionian)",
                "W – W – H – W – W – W – H",
                "Bright, stable — the reference scale.",
                """
                    Intervals from the root: 1 – 2 – 3 – 4 – 5 – 6 – 7 (all natural unless key signature says otherwise).

                    C major: C D E F G A B C

                    Major is the foundation for chords, melodies, and the other modes. Its third is major and its seventh is a whole step below the octave.
                """.trimIndent()),
            scale("natural_minor", ScaleCategory.DIATONIC, "Natural Minor (Aeolian)",
                "W – H – W – W – H – W – W",
                "Pure minor — same key signature as relative major.",
                """
                    Also called the Aeolian mode. Lowered 3rd, 6th, and 7th compared to major.

                    A natural minor: A B C D E F G A (relative to C major).

                    Darker, folk-like minor sound without the raised 7th of harmonic minor.
                """.trimIndent()),
            scale("harmonic_minor", ScaleCategory.DIATONIC, "Harmonic Minor",
                "W – H – W – W – H – A2 – H",
                "Raised 7th — classical minor tension.",
                """
                    Same as natural minor but with a raised 7th (leading tone).

                    A harmonic minor: A B C D E F G♯ A

                    Creates a strong pull to the tonic and the characteristic minor-key V chord (E major in A minor).
                """.trimIndent()),
            scale("melodic_minor", ScaleCategory.DIATONIC, "Melodic Minor",
                "W – H – W – W – W – W – H (ascending)",
                "Raised 6th and 7th ascending — jazz minor.",
                """
                    Ascending: raised 6th and 7th. Descending often matches natural minor in classical practice; jazz often uses the ascending form both ways.

                    A melodic minor (ascending): A B C D E F♯ G♯ A

                    Smoother melodic lines and colorful jazz harmony.
                """.trimIndent()),
            scale("dorian", ScaleCategory.MODES, "Dorian",
                "W – H – W – W – W – H – W",
                "Minor with raised 6th — soulful, modal jazz.",
                """
                    Minor mode with a major 6th. D Dorian (from C major): D E F G A B C D.

                    Less sad than natural minor; common in funk, jazz, and rock (So What, Oye Como Va).
                """.trimIndent()),
            scale("phrygian", ScaleCategory.MODES, "Phrygian",
                "H – W – W – W – H – W – W",
                "Minor with lowered 2nd — Spanish, dark.",
                """
                    Minor mode with a flat 2nd. E Phrygian: E F G A B C D E.

                    The half step above the root gives a Spanish / flamenco flavor.
                """.trimIndent()),
            scale("lydian", ScaleCategory.MODES, "Lydian",
                "W – W – W – H – W – W – H",
                "Major with raised 4th — dreamy, floating.",
                """
                    Major mode with a sharp 4th. F Lydian: F G A B C D E F.

                    Open, cinematic sound — avoids the “leading” feel of the natural 4th.
                """.trimIndent()),
            scale("mixolydian", ScaleCategory.MODES, "Mixolydian",
                "W – W – H – W – W – H – W",
                "Major with flat 7th — rock and blues dominant.",
                """
                    Major mode with a flat 7th. G Mixolydian: G A B C D E F G.

                    The sound of dominant 7th tonality — rock, blues, and folk grooves often sit here.
                """.trimIndent()),
            scale("locrian", ScaleCategory.MODES, "Locrian",
                "H – W – W – H – W – W – W",
                "Diminished 2nd and 5th — unstable, rare.",
                """
                    The darkest mode — flat 2nd and flat 5th. B Locrian: B C D E F G A B.

                    Highly unstable; used sparingly in metal and jazz over half-diminished harmony.
                """.trimIndent())
        )

        fun forCategory(category: ScaleCategory): List<ScaleLesson> =
            all.filter { it.category == category }

        private fun scale(
            id: String,
            category: ScaleCategory,
            title: String,
            pattern: String,
            summary: String,
            content: String
        ) = ScaleLesson(id, category, title, pattern, summary, content)
    }
}
