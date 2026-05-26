package com.abc.musicadventure.adventure

enum class QuestCategory(val displayName: String) {
    CHORDS("Chords"),
    READING("Reading"),
    INTERVALS("Intervals"),
    SCALES("Scales"),
    HARMONY("Harmony");

    companion object {
        fun forQuestNumber(questNumber: Int): QuestCategory {
            val index = (questNumber - 1) % entries.size
            return entries[index]
        }
    }
}

data class AdventureQuest(
    val questNumber: Int,
    val category: QuestCategory,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)
