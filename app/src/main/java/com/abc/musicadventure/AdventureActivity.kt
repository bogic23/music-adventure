package com.abc.musicadventure

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.abc.musicadventure.databinding.ActivityAdventureBinding
import com.abc.musicadventure.managers.GameManager
import com.abc.musicadventure.models.MusicNotes

class AdventureActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdventureBinding
    private var currentQuest = 1
    private var score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdventureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupListeners()
        startQuest()
    }

    private fun setupUI() {
        binding.tvQuestTitle.text = "Music Quest $currentQuest"
        binding.tvScore.text = "Score: 0"
        updatePlayerStats()
    }

    private fun updatePlayerStats() {
        val player = GameManager.currentPlayer
        binding.tvHP.text = "HP: ${player?.health ?: 100}"
        binding.tvMP.text = "MP: ${player?.mana ?: 50}"

        binding.progressHP.progress = player?.health ?: 100
        binding.progressMP.progress = player?.mana ?: 50
    }

    private fun setupListeners() {
        binding.btnPlayNote.setOnClickListener {
            playRandomNote()
        }

        binding.btnBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun startQuest() {
        binding.tvQuestDescription.text =
            """
            Quest $currentQuest: Musical Forest

            Play the correct notes to proceed through the enchanted forest.
            Listen carefully to each note and identify it correctly to keep moving forward.
            """.trimIndent()

        binding.cardQuest.startAnimation(
            AnimationUtils.loadAnimation(this, R.anim.fade_in)
        )
    }

    private fun playRandomNote() {
        val notes = MusicNotes.getRandomNotes(4)
        val correctNote = notes.random()
        val noteNames = notes.map { it.name }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Identify the Note")
            .setMessage("Which note did you hear?")
            .setItems(noteNames) { _, which ->
                if (notes[which] == correctNote) {
                    onCorrectAnswer()
                } else {
                    onWrongAnswer(correctNote.name)
                }
            }
            .setNegativeButton("Listen Again") { dialog, _ ->
                dialog.dismiss()
            }
            .show()

        Toast.makeText(this, "Listen carefully to the note...", Toast.LENGTH_SHORT).show()
    }

    private fun onCorrectAnswer() {
        score += 10
        binding.tvScore.text = "Score: $score"

        val player = GameManager.currentPlayer
        player?.apply {
            addExperience(20)
            gold += 5
        }

        updatePlayerStats()

        Toast.makeText(this, "Correct! +10 points", Toast.LENGTH_SHORT).show()

        if (score >= 50) {
            completeQuest()
        }
    }

    private fun onWrongAnswer(correctNote: String) {
        score = (score - 5).coerceAtLeast(0)
        binding.tvScore.text = "Score: $score"

        val player = GameManager.currentPlayer
        player?.apply {
            health = (health - 10).coerceAtLeast(0)
        }

        updatePlayerStats()

        Toast.makeText(this, "Wrong! The note was $correctNote", Toast.LENGTH_SHORT).show()
    }

    private fun completeQuest() {
        AlertDialog.Builder(this)
            .setTitle("Quest Complete!")
            .setMessage(
                """
                Congratulations! You completed Quest $currentQuest.

                Rewards:
                - 50 Gold
                - 100 XP
                - New Note Learned
                """.trimIndent()
            )
            .setPositiveButton("Continue") { _, _ ->
                val player = GameManager.currentPlayer
                player?.apply {
                    gold += 50
                    addExperience(100)
                    if (!notesLearned.contains("F")) {
                        notesLearned.add("F")
                    }
                }

                currentQuest++
                score = 0
                binding.tvScore.text = "Score: 0"
                startQuest()
            }
            .setNegativeButton("Back to Menu") { _, _ ->
                finish()
            }
            .show()
    }
}
