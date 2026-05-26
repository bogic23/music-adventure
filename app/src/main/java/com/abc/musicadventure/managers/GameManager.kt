package com.abc.musicadventure.managers

import com.abc.musicadventure.models.Player

object GameManager {
    var currentPlayer: Player? = null
    var isLoggedIn: Boolean = false

    fun login(username: String): Boolean {
        if (username.isNotBlank() && username.length >= 3) {
            currentPlayer = Player(username = username)
            isLoggedIn = true
            return true
        }
        return false
    }

    fun logout() {
        currentPlayer = null
        isLoggedIn = false
    }

    fun getPlayerInfo(): String {
        val player = currentPlayer ?: return "No player"
        return """
            Player: ${player.username}
            Level: ${player.level}
            HP: ${player.health}/100
            MP: ${player.mana}/50
            Music Knowledge: ${player.musicKnowledge}
            Gold: ${player.gold}
            Notes Learned: ${player.notesLearned.joinToString()}
        """.trimIndent()
    }
}