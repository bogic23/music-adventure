package com.abc.musicadventure.managers

import android.content.Context
import com.abc.musicadventure.models.Player
import com.abc.musicadventure.storage.PlayerStorage

object GameManager {
    var currentPlayer: Player? = null
    var isLoggedIn: Boolean = false

    fun login(context: Context, username: String, isGuest: Boolean = false): Boolean {
        if (username.isBlank() || username.length < 3) return false

        currentPlayer = if (isGuest) {
            Player(username = username, isGuest = true)
        } else {
            PlayerStorage.load(context, username) ?: Player(username = username, isGuest = false)
        }
        isLoggedIn = true
        return true
    }

    fun logout() {
        currentPlayer = null
        isLoggedIn = false
    }

    fun isGuest(): Boolean = currentPlayer?.isGuest == true

    fun savePlayer(context: Context) {
        val player = currentPlayer ?: return
        PlayerStorage.save(context, player)
    }

    fun getPlayerInfo(): String {
        val player = currentPlayer ?: return "No player"
        return """
            Player: ${player.username}
            Level: ${player.level}
            HP: ${player.health}/${Player.MAX_HEALTH}
            MP: ${player.mana}/${Player.MAX_MP}
            Quest: ${player.adventureQuest}
            Music Knowledge: ${player.musicKnowledge}
            Gold: ${player.gold}
            Notes Learned: ${player.notesLearned.joinToString()}
        """.trimIndent()
    }
}
