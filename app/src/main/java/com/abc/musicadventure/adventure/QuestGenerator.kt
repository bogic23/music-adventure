package com.abc.musicadventure.adventure

import com.abc.musicadventure.models.ChordType
import com.abc.musicadventure.models.HarmonyLesson
import com.abc.musicadventure.models.IntervalLesson
import com.abc.musicadventure.models.ReadingLesson
import com.abc.musicadventure.models.ScaleLesson

object QuestGenerator {
    fun generate(questNumber: Int): AdventureQuest {
        val category = QuestCategory.forQuestNumber(questNumber)
        return when (category) {
            QuestCategory.CHORDS -> generateChordQuest(questNumber, category)
            QuestCategory.READING -> generateReadingQuest(questNumber, category)
            QuestCategory.INTERVALS -> generateIntervalQuest(questNumber, category)
            QuestCategory.SCALES -> generateScaleQuest(questNumber, category)
            QuestCategory.HARMONY -> generateHarmonyQuest(questNumber, category)
        }
    }

    private fun generateChordQuest(questNumber: Int, category: QuestCategory): AdventureQuest {
        val chord = ChordType.all.random()
        val wrong = ChordType.all.filter { it.id != chord.id }.shuffled().take(3)
        val options = (listOf(chord.name) + wrong.map { it.name }).shuffled()
        return AdventureQuest(
            questNumber = questNumber,
            category = category,
            question = "Which chord uses the formula: ${chord.formula}?",
            options = options,
            correctIndex = options.indexOf(chord.name),
            explanation = "${chord.name}: ${chord.structure}"
        )
    }

    private fun generateReadingQuest(questNumber: Int, category: QuestCategory): AdventureQuest {
        val lesson = ReadingLesson.all.random()
        val wrong = ReadingLesson.all.filter { it.id != lesson.id }.shuffled().take(3)
        val options = (listOf(lesson.title) + wrong.map { it.title }).shuffled()
        return AdventureQuest(
            questNumber = questNumber,
            category = category,
            question = "Which lesson matches this summary?\n\n\"${lesson.summary}\"",
            options = options,
            correctIndex = options.indexOf(lesson.title),
            explanation = lesson.content.lines().firstOrNull()?.trim() ?: lesson.summary
        )
    }

    private fun generateIntervalQuest(questNumber: Int, category: QuestCategory): AdventureQuest {
        val interval = IntervalLesson.all.random()
        val wrongSemitones = IntervalLesson.all
            .map { it.semitones }
            .filter { it != interval.semitones }
            .distinct()
            .shuffled()
            .take(3)
        val correctAnswer = "${interval.semitones} semitones"
        val options = (listOf(correctAnswer) + wrongSemitones.map { "$it semitones" }).shuffled()
        return AdventureQuest(
            questNumber = questNumber,
            category = category,
            question = "How many semitones is a ${interval.title} (${interval.shortLabel})?",
            options = options,
            correctIndex = options.indexOf(correctAnswer),
            explanation = interval.summary
        )
    }

    private fun generateScaleQuest(questNumber: Int, category: QuestCategory): AdventureQuest {
        val scale = ScaleLesson.all.random()
        val wrong = ScaleLesson.all.filter { it.id != scale.id }.shuffled().take(3)
        val options = (listOf(scale.title) + wrong.map { it.title }).shuffled()
        return AdventureQuest(
            questNumber = questNumber,
            category = category,
            question = "Which scale has this pattern?\n\n${scale.pattern}",
            options = options,
            correctIndex = options.indexOf(scale.title),
            explanation = scale.summary
        )
    }

    private fun generateHarmonyQuest(questNumber: Int, category: QuestCategory): AdventureQuest {
        val lesson = HarmonyLesson.all.random()
        val wrong = HarmonyLesson.all.filter { it.id != lesson.id }.shuffled().take(3)
        val options = (listOf(lesson.title) + wrong.map { it.title }).shuffled()
        return AdventureQuest(
            questNumber = questNumber,
            category = category,
            question = "Harmony: ${lesson.summary}\n\nWhich concept is this?",
            options = options,
            correctIndex = options.indexOf(lesson.title),
            explanation = lesson.content
        )
    }
}
