package com.abc.musicadventure.models

data class Player(
    var username: String = "",
    var level: Int = 1,
    var experience: Int = 0,
    var health: Int = 100,
    var mana: Int = 50,
    var musicKnowledge: Int = 0,
    var instrument: String = "Piano",
    var notesLearned: MutableList<String> = mutableListOf("C", "D", "E"),
    var gold: Int = 100
) {
    fun getExperienceToNextLevel(): Int = level * 100

    fun addExperience(exp: Int) {
        experience += exp
        while (experience >= getExperienceToNextLevel()) {
            levelUp()
        }
    }

    private fun levelUp() {
        experience -= getExperienceToNextLevel()
        level++
        health += 20
        mana += 10
        musicKnowledge += 5
    }
}