package com.abc.musicadventure.models

data class Player(
    var username: String = "",
    var level: Int = 1,
    var experience: Int = 0,
    var health: Int = MAX_HEALTH,
    var mana: Int = MAX_MP / 2,
    var musicKnowledge: Int = 0,
    var instrument: String = "Piano",
    var notesLearned: MutableList<String> = mutableListOf("C", "D", "E"),
    var gold: Int = 100,
    var adventureQuest: Int = 1,
    var isGuest: Boolean = false
) {
    companion object {
        const val MAX_HEALTH = 100
        const val MAX_MP = 100
    }

    fun addMana(amount: Int) {
        mana = (mana + amount).coerceAtMost(MAX_MP)
    }

    fun spendManaForHeal(mpToSpend: Int, hpPerMp: Int = 5): Int {
        if (mpToSpend <= 0 || mana <= 0) return 0
        val actualSpend = mpToSpend.coerceAtMost(mana)
        val missingHp = MAX_HEALTH - health
        if (missingHp <= 0) return 0
        val maxHealFromMp = actualSpend * hpPerMp
        val healAmount = maxHealFromMp.coerceAtMost(missingHp)
        val mpUsed = (healAmount + hpPerMp - 1) / hpPerMp
        mana -= mpUsed
        health += healAmount
        return healAmount
    }

    fun takeDamage(amount: Int) {
        health = (health - amount).coerceAtLeast(0)
    }

    fun isAlive(): Boolean = health > 0
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