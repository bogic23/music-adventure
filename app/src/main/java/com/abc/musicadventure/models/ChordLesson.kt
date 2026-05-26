package com.abc.musicadventure.models

enum class ChordCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val overview: String
) {
    TRIADS(
        id = "triads",
        title = "Triads",
        subtitle = "Basic harmony",
        overview = "Three-note chords built from stacked thirds — the foundation of Western harmony."
    ),
    SEVENTHS(
        id = "sevenths",
        title = "Sevenths",
        subtitle = "Four-note chords",
        overview = "Add a seventh above the triad for richer color — essential in jazz, pop, and classical voice-leading."
    ),
    EXTENSIONS(
        id = "extensions",
        title = "Extensions",
        subtitle = "9ths, 11ths, 13ths",
        overview = "Stack tones beyond the seventh for lush, modern harmony and color chords."
    ),
    SUSPENDED(
        id = "suspended",
        title = "Suspended",
        subtitle = "Sus2 & Sus4",
        overview = "Replace the third with the 2nd or 4th for open, unresolved tension before resolving."
    ),
    SPECIAL(
        id = "special",
        title = "Special / Alt.",
        subtitle = "Power, slash & more",
        overview = "Alternative voicings and harmonic colors used in rock, film scoring, and contemporary writing."
    );

    companion object {
        fun fromId(id: String): ChordCategory =
            entries.firstOrNull { it.id == id } ?: TRIADS
    }
}

data class ChordType(
    val id: String,
    val name: String,
    val symbol: String,
    val category: ChordCategory,
    val intervals: List<Int>,
    val formula: String,
    val structure: String,
    val exampleRoot: String = "C"
) {
    fun noteNames(root: String): String {
        val chromatic = CHROMATIC
        val rootIndex = chromatic.indexOf(root).coerceAtLeast(0)
        return intervals.joinToString(" – ") { semi ->
            chromatic[(rootIndex + semi) % chromatic.size]
        }
    }

    fun displayLabel(root: String = exampleRoot): String = "$root$symbol"

    fun matchesAnswer(input: String, root: String): Boolean {
        val normalized = normalizeAnswer(input)
        if (normalized.isEmpty()) return false
        return acceptableAnswers(root).contains(normalized)
    }

    private fun acceptableAnswers(root: String): Set<String> {
        val answers = mutableSetOf<String>()
        val r = normalizeAnswer(root)

        answers.add(normalizeAnswer(name))
        answers.add(normalizeAnswer(id.replace("_", " ")))
        if (symbol.isNotEmpty()) {
            answers.add(normalizeAnswer(symbol))
            answers.add(normalizeAnswer("$root$symbol"))
        } else {
            answers.add(r)
        }

        when (id) {
            "major" -> answers.addAll(listOf("maj", "major"))
            "minor" -> answers.addAll(listOf("min", "m", "${r}m"))
            "diminished" -> answers.addAll(listOf("dim", "diminished"))
            "augmented" -> answers.addAll(listOf("aug", "augmented"))
            "maj7" -> answers.addAll(listOf("maj7", "major7", "ma7", "${r}maj7"))
            "dom7" -> answers.addAll(listOf("7", "dom7", "dominant7", "${r}7"))
            "min7" -> answers.addAll(listOf("m7", "min7", "minor7", "${r}m7"))
            "m7b5" -> answers.addAll(listOf("m7b5", "min7b5", "halfdiminished", "halfdim"))
            "dim7" -> answers.addAll(listOf("dim7", "diminished7"))
            "maj9" -> answers.addAll(listOf("maj9", "major9", "${r}maj9"))
            "dom9" -> answers.addAll(listOf("9", "dom9", "${r}9"))
            "min9" -> answers.addAll(listOf("m9", "min9", "minor9", "${r}m9"))
            "add9" -> answers.addAll(listOf("add9", "${r}add9"))
            "maj11" -> answers.addAll(listOf("maj11", "major11"))
            "dom13" -> answers.addAll(listOf("13", "dom13", "${r}13"))
            "sus2" -> answers.addAll(listOf("sus2", "${r}sus2"))
            "sus4" -> answers.addAll(listOf("sus4", "${r}sus4"))
            "7sus4" -> answers.addAll(listOf("7sus4", "${r}7sus4"))
            "power" -> answers.addAll(listOf("5", "power", "powerchord", "${r}5"))
            "slash_c_g" -> answers.addAll(listOf("slash", "c/g", "cg"))
            "quartal" -> answers.addAll(listOf("quartal", "quart"))
            "alt" -> answers.addAll(listOf("7alt", "alt", "altered", "altereddominant"))
        }

        return answers.map { normalizeAnswer(it) }.toSet()
    }

    companion object {
        fun normalizeAnswer(input: String): String =
            input.trim().lowercase()
                .replace("♭", "b")
                .replace("♯", "#")
                .replace(" ", "")
                .replace("-", "")
        private val CHROMATIC = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

        val all: List<ChordType> = listOf(
            // Triads
            ChordType(
                id = "major",
                name = "Major",
                symbol = "",
                category = ChordCategory.TRIADS,
                intervals = listOf(0, 4, 7),
                formula = "1 – 3 – 5",
                structure = "Bright and stable. The major third (4 semitones above the root) defines its happy, resolved sound."
            ),
            ChordType(
                id = "minor",
                name = "Minor",
                symbol = "m",
                category = ChordCategory.TRIADS,
                intervals = listOf(0, 3, 7),
                formula = "1 – ♭3 – 5",
                structure = "Darker and melancholic. The minor third (3 semitones) lowers the middle tone of the triad."
            ),
            ChordType(
                id = "diminished",
                name = "Diminished",
                symbol = "dim",
                category = ChordCategory.TRIADS,
                intervals = listOf(0, 3, 6),
                formula = "1 – ♭3 – ♭5",
                structure = "Tense and unstable. Both the third and fifth are lowered, creating strong pull to resolve."
            ),
            ChordType(
                id = "augmented",
                name = "Augmented",
                symbol = "aug",
                category = ChordCategory.TRIADS,
                intervals = listOf(0, 4, 8),
                formula = "1 – 3 – ♯5",
                structure = "Dreamy and ambiguous. The raised fifth stretches the triad outward with a whole-tone color."
            ),
            // Sevenths
            ChordType(
                id = "maj7",
                name = "Major 7",
                symbol = "maj7",
                category = ChordCategory.SEVENTHS,
                intervals = listOf(0, 4, 7, 11),
                formula = "1 – 3 – 5 – 7",
                structure = "Smooth and sophisticated. The major seventh sits a half step below the octave for a lush, jazzy feel."
            ),
            ChordType(
                id = "dom7",
                name = "Dominant 7",
                symbol = "7",
                category = ChordCategory.SEVENTHS,
                intervals = listOf(0, 4, 7, 10),
                formula = "1 – 3 – 5 – ♭7",
                structure = "The backbone of blues and jazz. The flat seventh creates tension that wants to resolve to the tonic."
            ),
            ChordType(
                id = "min7",
                name = "Minor 7",
                symbol = "m7",
                category = ChordCategory.SEVENTHS,
                intervals = listOf(0, 3, 7, 10),
                formula = "1 – ♭3 – 5 – ♭7",
                structure = "Mellow and versatile. Common in ii–V progressions and modal jazz as a softer minor color."
            ),
            ChordType(
                id = "m7b5",
                name = "Half-Diminished",
                symbol = "m7♭5",
                category = ChordCategory.SEVENTHS,
                intervals = listOf(0, 3, 6, 10),
                formula = "1 – ♭3 – ♭5 – ♭7",
                structure = "Also called ø7. Found on the viiø in minor keys — tense but less harsh than a full diminished seventh."
            ),
            ChordType(
                id = "dim7",
                name = "Diminished 7",
                symbol = "dim7",
                category = ChordCategory.SEVENTHS,
                intervals = listOf(0, 3, 6, 9),
                formula = "1 – ♭3 – ♭5 – 𝄫7",
                structure = "Symmetric and dramatic. Every note is a minor third apart, so it can resolve in multiple directions."
            ),
            // Extensions
            ChordType(
                id = "maj9",
                name = "Major 9",
                symbol = "maj9",
                category = ChordCategory.EXTENSIONS,
                intervals = listOf(0, 4, 7, 11, 14),
                formula = "1 – 3 – 5 – 7 – 9",
                structure = "Adds the 9th (same pitch class as the 2nd) above a maj7 for a floating, contemporary color."
            ),
            ChordType(
                id = "dom9",
                name = "Dominant 9",
                symbol = "9",
                category = ChordCategory.EXTENSIONS,
                intervals = listOf(0, 4, 7, 10, 14),
                formula = "1 – 3 – 5 – ♭7 – 9",
                structure = "Funk and soul staple. The 9th adds sparkle without losing the dominant pull of the flat seventh."
            ),
            ChordType(
                id = "min9",
                name = "Minor 9",
                symbol = "m9",
                category = ChordCategory.EXTENSIONS,
                intervals = listOf(0, 3, 7, 10, 14),
                formula = "1 – ♭3 – 5 – ♭7 – 9",
                structure = "Warm minor palette with an added ninth — common in R&B ballads and neo-soul voicings."
            ),
            ChordType(
                id = "add9",
                name = "Add 9",
                symbol = "add9",
                category = ChordCategory.EXTENSIONS,
                intervals = listOf(0, 4, 7, 14),
                formula = "1 – 3 – 5 – 9",
                structure = "Major triad plus the 9th, without the seventh. Open and pop-friendly without jazz dissonance."
            ),
            ChordType(
                id = "maj11",
                name = "Major 11",
                symbol = "maj11",
                category = ChordCategory.EXTENSIONS,
                intervals = listOf(0, 4, 7, 11, 14, 17),
                formula = "1 – 3 – 5 – 7 – 9 – 11",
                structure = "Very wide and atmospheric. The 11th (same as the 4th) adds modal, open-air harmony."
            ),
            ChordType(
                id = "dom13",
                name = "Dominant 13",
                symbol = "13",
                category = ChordCategory.EXTENSIONS,
                intervals = listOf(0, 4, 7, 10, 14, 21),
                formula = "1 – 3 – 5 – ♭7 – 9 – 13",
                structure = "Full dominant palette. The 13th (6th up an octave) is the classic jazz dominant color."
            ),
            // Suspended
            ChordType(
                id = "sus2",
                name = "Suspended 2",
                symbol = "sus2",
                category = ChordCategory.SUSPENDED,
                intervals = listOf(0, 2, 7),
                formula = "1 – 2 – 5",
                structure = "No third — the 2nd replaces it. Light and open, often used in ambient and pop intros."
            ),
            ChordType(
                id = "sus4",
                name = "Suspended 4",
                symbol = "sus4",
                category = ChordCategory.SUSPENDED,
                intervals = listOf(0, 5, 7),
                formula = "1 – 4 – 5",
                structure = "The classic sus sound. The 4th hangs above the root, begging to fall to the major third."
            ),
            ChordType(
                id = "7sus4",
                name = "7 Sus4",
                symbol = "7sus4",
                category = ChordCategory.SUSPENDED,
                intervals = listOf(0, 5, 7, 10),
                formula = "1 – 4 – 5 – ♭7",
                structure = "Suspended tension with dominant function. A go-to in gospel, rock, and modal interchange."
            ),
            // Special / Alternative
            ChordType(
                id = "power",
                name = "Power Chord",
                symbol = "5",
                category = ChordCategory.SPECIAL,
                intervals = listOf(0, 7),
                formula = "1 – 5",
                structure = "Root and fifth only — neither major nor minor. The sound of rock, metal, and distorted guitar."
            ),
            ChordType(
                id = "slash_c_g",
                name = "Slash Chord",
                symbol = "/G",
                category = ChordCategory.SPECIAL,
                intervals = listOf(0, 4, 7),
                formula = "1 – 3 – 5 / bass",
                structure = "A triad with a non-root bass note (e.g. C/G). Creates inversion-like motion and bass independence.",
                exampleRoot = "C"
            ),
            ChordType(
                id = "quartal",
                name = "Quartal",
                symbol = "quart",
                category = ChordCategory.SPECIAL,
                intervals = listOf(0, 5, 10),
                formula = "1 – 4 – ♭7",
                structure = "Built from stacked fourths instead of thirds. Associated with McCoy Tyner and modern jazz piano."
            ),
            ChordType(
                id = "alt",
                name = "Altered Dominant",
                symbol = "7alt",
                category = ChordCategory.SPECIAL,
                intervals = listOf(0, 4, 8, 10, 13),
                formula = "1 – 3 – ♯5 – ♭7 – ♭9",
                structure = "Maximum tension before resolution. Uses raised or lowered 5ths and 9ths for a chromatic, bebop edge."
            )
        )

        fun forCategory(category: ChordCategory): List<ChordType> =
            all.filter { it.category == category }

        fun randomRoot(): String = listOf("C", "D", "E", "G", "A").random()
    }
}
