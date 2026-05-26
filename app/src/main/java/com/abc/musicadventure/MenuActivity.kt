package com.abc.musicadventure

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.abc.musicadventure.databinding.ActivityMenuBinding
import com.abc.musicadventure.managers.GameManager

class MenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        val player = GameManager.currentPlayer
        binding.tvPlayerName.text = player?.username ?: "Adventurer"
        binding.tvPlayerLevel.text = "Level ${player?.level ?: 1}"

        // Animate menu buttons
        val buttons = listOf(
            binding.btnAdventure,
            binding.btnTraining,
            binding.btnProfile
        )

        buttons.forEachIndexed { index, button ->
            button.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom).apply {
                    startOffset = (index * 100).toLong()
                }
            )
        }
    }

    private fun setupListeners() {
        binding.btnAdventure.setOnClickListener {
            if (GameManager.isGuest()) {
                AlertDialog.Builder(this)
                    .setTitle(R.string.adventure)
                    .setMessage(R.string.adventure_guest_menu)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
                return@setOnClickListener
            }
            startActivity(Intent(this, AdventureActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.btnTraining.setOnClickListener {
            startActivity(Intent(this, TrainingActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.btnLogout.setOnClickListener {
            GameManager.logout()
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }
    }
}