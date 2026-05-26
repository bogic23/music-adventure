package com.abc.musicadventure.models

enum class ReadingTopic(
    val id: String,
    val title: String,
    val subtitle: String,
    val overview: String
) {
    NOTATION(
        "notation", "Notation", "Staff & symbols",
        "How pitch and symbols are written on the staff — clefs, lines, spaces, and accidentals."
    ),
    KEY(
        "key", "Key", "Signatures & tonality",
        "Key signatures and major vs minor tonality tell you which notes to expect."
    ),
    TIME(
        "time", "Time", "Time signatures",
        "The time signature defines how many beats fit in each measure and what note gets the beat."
    ),
    BEATS(
        "beats", "Beats", "Pulse & rhythm",
        "The steady pulse of music, accents, and how beats split into smaller rhythms."
    ),
    MEASURE(
        "measure", "Measure", "Bars & counting",
        "Measures divide music into equal time units so you can count and stay together."
    ),
    NOTE_VALUES(
        "note_values", "Note Values", "Duration & rests",
        "How long notes and rests last — whole notes through sixteenths, dots, and ties."
    );

    companion object {
        fun fromId(id: String): ReadingTopic =
            entries.firstOrNull { it.id == id } ?: NOTATION
    }
}

data class ReadingLesson(
    val id: String,
    val topic: ReadingTopic,
    val title: String,
    val summary: String,
    val content: String
) {
    companion object {
        val all: List<ReadingLesson> = listOf(
            ReadingLesson(
                id = "staff",
                topic = ReadingTopic.NOTATION,
                title = "The Staff",
                summary = "Five lines where pitch is written.",
                content = """
                    Music is written on a staff of five horizontal lines and four spaces. Notes sit on lines or in spaces to show pitch.

                    • Treble clef (𝄞) is used for higher instruments and the right hand on piano.
                    • Bass clef (𝄢) is used for lower instruments and the left hand on piano.
                    • Ledger lines extend the staff above or below when notes go higher or lower than the five lines.

                    Read from left to right: each symbol tells you what to play and for how long.
                """.trimIndent()
            ),
            ReadingLesson(
                id = "note_heads",
                topic = ReadingTopic.NOTATION,
                title = "Note Heads & Accidentals",
                summary = "Shape and symbols that modify pitch.",
                content = """
                    A note head on the staff tells you which pitch to play. The stem and flag (or beam) show rhythm.

                    • Sharp (♯) raises a note by one half step.
                    • Flat (♭) lowers a note by one half step.
                    • Natural (♮) cancels a sharp or flat.

                    Accidentals apply to that measure unless carried by a tie or key signature.
                """.trimIndent()
            ),
            ReadingLesson(
                id = "key_sig",
                topic = ReadingTopic.KEY,
                title = "Key Signatures",
                summary = "Sharps or flats at the start of each line.",
                content = """
                    The key signature appears right after the clef. It tells you which notes are sharp or flat throughout the piece.

                    • No sharps or flats often means C major or A minor.
                    • Order of sharps: F–C–G–D–A–E–B (Father Charles Goes Down And Ends Battle).
                    • Order of flats: B–E–A–D–G–C–F (reverse of sharps).

                    Knowing the key helps you expect which pitches will appear and how the music will feel.
                """.trimIndent()
            ),
            ReadingLesson(
                id = "major_minor",
                topic = ReadingTopic.KEY,
                title = "Major & Minor Keys",
                summary = "Tonality shapes mood and harmony.",
                content = """
                    • Major keys usually sound bright, open, or happy.
                    • Minor keys usually sound darker, serious, or melancholic.

                    Each key has a tonic (home note). Melodies and chords gravitate toward that tonic. Relative major and minor share the same key signature (e.g. C major and A minor).
                """.trimIndent()
            ),
            ReadingLesson(
                id = "time_sig",
                topic = ReadingTopic.TIME,
                title = "Time Signatures",
                summary = "How many beats per measure and what note gets the beat.",
                content = """
                    The time signature is two numbers stacked at the start of the staff:

                    • Top number = how many beats are in each measure.
                    • Bottom number = which note value counts as one beat (4 = quarter note, 8 = eighth note).

                    Examples:
                    • 4/4 — four quarter-note beats per measure (common time).
                    • 3/4 — three quarter-note beats (waltz feel).
                    • 6/8 — six eighth-note beats grouped in two groups of three.
                """.trimIndent()
            ),
            ReadingLesson(
                id = "simple_compound",
                topic = ReadingTopic.TIME,
                title = "Simple & Compound Meter",
                summary = "How beats group together.",
                content = """
                    • Simple meter: the beat divides into two (4/4, 3/4, 2/4).
                    • Compound meter: the beat divides into three (6/8, 9/8, 12/8).

                    In 6/8, you often feel two big beats per measure, each split into three eighth notes. Tap the big pulse first, then fill in subdivisions.
                """.trimIndent()
            ),
            ReadingLesson(
                id = "beat_pulse",
                topic = ReadingTopic.BEATS,
                title = "Beat & Pulse",
                summary = "The steady heartbeat of the music.",
                content = """
                    The beat is the steady pulse you can tap your foot to. Tempo tells how fast those beats go (BPM — beats per minute).

                    • Strong beats feel accented (downbeats).
                    • Weak beats fill the space between (upbeats).

                    In 4/4, beat 1 is usually strongest; beats 2 and 4 are often emphasized in pop and rock (backbeat).
                """.trimIndent()
            ),
            ReadingLesson(
                id = "subdivision",
                topic = ReadingTopic.BEATS,
                title = "Subdivisions",
                summary = "Splitting beats into smaller parts.",
                content = """
                    One beat can divide into two eighth notes, four sixteenth notes, or three triplets.

                    Counting helps:
                    • Quarter: 1, 2, 3, 4
                    • Eighths: 1 & 2 & 3 & 4 &
                    • Sixteenths: 1 e & a 2 e & a …

                    Clap the beat first, then add faster notes inside each beat.
                """.trimIndent()
            ),
            ReadingLesson(
                id = "measure_bar",
                topic = ReadingTopic.MEASURE,
                title = "Measures (Bars)",
                summary = "Music divided into equal units of time.",
                content = """
                    A measure (bar) is the space between two bar lines. Each measure holds the number of beats written in the time signature — no more, no less.

                    • Single bar line separates measures.
                    • Double bar line marks a section end.
                    • Final bar line ends the piece.

                    Counting in measures keeps you aligned with other musicians and with the structure of the song.
                """.trimIndent()
            ),
            ReadingLesson(
                id = "counting",
                topic = ReadingTopic.MEASURE,
                title = "Counting Through a Piece",
                summary = "Stay oriented from measure to measure.",
                content = """
                    When sight-reading, track the measure number mentally or aloud at first.

                    1. Find the time signature.
                    2. Count one full measure out loud before playing.
                    3. Keep your eyes moving ahead so you enter each new measure on beat 1.

                    Pick-up measures (anacrusis) start before beat 1 of the first full bar — common in melodies that begin on “and” or beat 4.
                """.trimIndent()
            ),
            ReadingLesson(
                id = "note_lengths",
                topic = ReadingTopic.NOTE_VALUES,
                title = "Note Lengths",
                summary = "How long each note lasts.",
                content = """
                    • Whole note — 4 beats (in 4/4)
                    • Half note — 2 beats
                    • Quarter note — 1 beat
                    • Eighth note — ½ beat
                    • Sixteenth note — ¼ beat

                    A dot adds half the note’s value. A tie connects two notes — hold for their combined length. A slur curves over notes to play them smoothly (legato).
                """.trimIndent()
            ),
            ReadingLesson(
                id = "rests",
                topic = ReadingTopic.NOTE_VALUES,
                title = "Rests",
                summary = "Silence written with the same rules as notes.",
                content = """
                    Every note value has a matching rest:

                    • Whole rest — silence for 4 beats
                    • Half rest — 2 beats
                    • Quarter rest — 1 beat
                    • Eighth & sixteenth rests — shorter silences

                    Rests are not “nothing” — they are timed silence. Count through rests the same way you count through notes.
                """.trimIndent()
            )
        )

        fun forTopic(topic: ReadingTopic): List<ReadingLesson> =
            all.filter { it.topic == topic }
    }
}
