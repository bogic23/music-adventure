package com.abc.musicadventure.storage

import android.content.Context
import com.abc.musicadventure.models.Player
import org.json.JSONArray
import org.json.JSONObject

object PlayerStorage {
    private const val PREFS_NAME = "music_adventure_players"

    fun save(context: Context, player: Player) {
        if (player.isGuest) return
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val allPlayers = loadAllPlayers(prefs)
        allPlayers[player.username] = playerToJson(player)
        val root = JSONObject()
        allPlayers.forEach { (name, data) -> root.put(name, data) }
        prefs.edit().putString("players", root.toString()).apply()
    }

    fun load(context: Context, username: String): Player? {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = loadAllPlayers(prefs)[username] ?: return null
        return jsonToPlayer(json)
    }

    private fun loadAllPlayers(prefs: android.content.SharedPreferences): MutableMap<String, JSONObject> {
        val raw = prefs.getString("players", null) ?: return mutableMapOf()
        return try {
            val root = JSONObject(raw)
            val map = mutableMapOf<String, JSONObject>()
            root.keys().forEach { key ->
                map[key] = root.getJSONObject(key)
            }
            map
        } catch (_: Exception) {
            mutableMapOf()
        }
    }

    private fun playerToJson(player: Player): JSONObject =
        JSONObject().apply {
            put("username", player.username)
            put("level", player.level)
            put("experience", player.experience)
            put("health", player.health)
            put("mana", player.mana)
            put("musicKnowledge", player.musicKnowledge)
            put("instrument", player.instrument)
            put("gold", player.gold)
            put("adventureQuest", player.adventureQuest)
            put("isGuest", player.isGuest)
            put("notesLearned", JSONArray(player.notesLearned))
        }

    private fun jsonToPlayer(json: JSONObject): Player {
        val notes = mutableListOf<String>()
        val notesArray = json.optJSONArray("notesLearned")
        if (notesArray != null) {
            for (i in 0 until notesArray.length()) {
                notes.add(notesArray.getString(i))
            }
        }
        return Player(
            username = json.getString("username"),
            level = json.optInt("level", 1),
            experience = json.optInt("experience", 0),
            health = json.optInt("health", Player.MAX_HEALTH),
            mana = json.optInt("mana", Player.MAX_MP / 2),
            musicKnowledge = json.optInt("musicKnowledge", 0),
            instrument = json.optString("instrument", "Piano"),
            notesLearned = if (notes.isEmpty()) mutableListOf("C", "D", "E") else notes,
            gold = json.optInt("gold", 100),
            adventureQuest = json.optInt("adventureQuest", 1),
            isGuest = json.optBoolean("isGuest", false)
        )
    }
}
