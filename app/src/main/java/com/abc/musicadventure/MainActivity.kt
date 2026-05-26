package com.abc.musicadventure

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.abc.musicadventure.databinding.ActivityMainBinding
import com.abc.musicadventure.managers.GameManager
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        // Animate logo
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        binding.ivLogo.startAnimation(fadeIn)

        // Setup EditText with icon
        binding.tilUsername.startIconDrawable = getDrawable(R.drawable.ic_person)
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()

            if (GameManager.login(username)) {
                // Success animation
                binding.btnLogin.startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.button_press)
                )

                Handler(Looper.getMainLooper()).postDelayed({
                    startActivity(Intent(this, MenuActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                }, 500)
            } else {
                binding.tilUsername.error = "Username must be at least 3 characters"
                binding.tilUsername.startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.shake)
                )
            }
        }

        binding.btnGuest.setOnClickListener {
            if (GameManager.login("Guest_${(1000..9999).random()}")) {
                startActivity(Intent(this, MenuActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()
            }
        }
    }
}