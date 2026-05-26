package com.abc.musicadventure

import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.abc.musicadventure.databinding.ActivityProfileBinding
import com.abc.musicadventure.managers.GameManager

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        val player = GameManager.currentPlayer
        binding.apply {
            tvPlayerName.text = player?.username ?: "Adventurer"
            tvLevel.text = "Level ${player?.level ?: 1}"
            tvExperience.text = "${player?.experience ?: 0} / ${player?.getExperienceToNextLevel() ?: 100} XP"
            tvHealth.text = "${player?.health ?: 100}/100"
            tvMana.text = "${player?.mana ?: 50}/50"
            tvMusicKnowledge.text = "${player?.musicKnowledge ?: 0}"
            tvGold.text = "${player?.gold ?: 0} Gold"
            tvInstrument.text = player?.instrument ?: "Piano"
            tvNotesLearned.text = player?.notesLearned?.joinToString(", ") ?: "C, D, E"

            progressXP.progress = player?.experience ?: 0
            progressXP.max = player?.getExperienceToNextLevel() ?: 100

            progressHP.progress = player?.health ?: 100
            progressMP.progress = player?.mana ?: 50
        }

        // Animate stats
        binding.cardStats.startAnimation(
            AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom)
        )
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }
}