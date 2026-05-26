package com.abc.musicadventure

import android.os.Bundle
import android.os.CountDownTimer
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.abc.musicadventure.adventure.AdventureQuest
import com.abc.musicadventure.adventure.QuestGenerator
import com.abc.musicadventure.databinding.ActivityAdventureBinding
import com.abc.musicadventure.managers.GameManager
import com.abc.musicadventure.models.Player

class AdventureActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdventureBinding
    private var currentQuest: AdventureQuest? = null
    private var questTimer: CountDownTimer? = null
    private var answersEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (GameManager.isGuest() || GameManager.currentPlayer == null) {
            Toast.makeText(this, R.string.adventure_guest_blocked, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        binding = ActivityAdventureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupListeners()
        startNextQuest()
    }

    override fun onPause() {
        super.onPause()
        if (!GameManager.isGuest() && GameManager.currentPlayer != null) {
            saveProgress()
        }
    }

    override fun onDestroy() {
        questTimer?.cancel()
        super.onDestroy()
    }

    private fun player(): Player = GameManager.currentPlayer!!

    private fun questNumber(): Int = player().adventureQuest

    private fun setupUI() {
        updatePlayerStats()
        updateTimerAppearance(urgent = false)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            saveProgress()
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        val answerButtons = listOf(binding.btnAnswer1, binding.btnAnswer2, binding.btnAnswer3, binding.btnAnswer4)
        answerButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (answersEnabled) {
                    onAnswerSelected(index)
                }
            }
        }
    }

    private fun retryCurrentQuest() {
        val quest = currentQuest
        if (quest == null) {
            startNextQuest()
            return
        }
        answersEnabled = true
        resetAnswerAppearance()
        binding.tvQuestDescription.text = quest.question
        startTimer()
    }

    private fun startNextQuest() {
        if (!player().isAlive()) {
            showGameOver()
            return
        }

        val quest = QuestGenerator.generate(questNumber())
        currentQuest = quest
        answersEnabled = true

        binding.tvQuestTitle.text = getString(R.string.adventure_quest_number, quest.questNumber)
        binding.tvQuestCategory.text = getString(R.string.adventure_category, quest.category.displayName)
        binding.tvQuestDescription.text = quest.question

        val buttons = answerButtons()
        quest.options.forEachIndexed { index, option ->
            buttons[index].text = option
            buttons[index].isEnabled = true
        }

        binding.cardQuest.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))
        binding.scrollAdventure.post { binding.scrollAdventure.smoothScrollTo(0, 0) }
        resetAnswerAppearance()
        startTimer()
    }

    private fun answerButtons(): List<MaterialButton> =
        listOf(binding.btnAnswer1, binding.btnAnswer2, binding.btnAnswer3, binding.btnAnswer4)

    private fun startTimer() {
        questTimer?.cancel()
        binding.tvTimer.text = getString(R.string.adventure_timer_seconds, QUEST_TIME_SECONDS)
        updateTimerAppearance(urgent = false)

        questTimer = object : CountDownTimer(QUEST_TIME_SECONDS * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000L).toInt().coerceAtLeast(0)
                binding.tvTimer.text = getString(R.string.adventure_timer_seconds, seconds)
                updateTimerAppearance(urgent = seconds <= 5)
            }

            override fun onFinish() {
                binding.tvTimer.text = getString(R.string.adventure_timer_expired)
                updateTimerAppearance(urgent = true)
                onWrongAnswer(getString(R.string.adventure_timeout_reason))
            }
        }.start()
    }

    private fun updateTimerAppearance(urgent: Boolean) {
        if (urgent) {
            binding.timerBadge.setBackgroundResource(R.drawable.bg_adventure_timer_urgent)
            binding.tvTimer.setTextColor(getColor(R.color.white))
        } else {
            binding.timerBadge.setBackgroundResource(R.drawable.bg_adventure_timer)
            binding.tvTimer.setTextColor(getColor(R.color.text_dark))
        }
    }

    private fun cancelTimer() {
        questTimer?.cancel()
        questTimer = null
    }

    private fun setAnswersEnabled(enabled: Boolean) {
        answersEnabled = enabled
        answerButtons().forEach {
            it.isEnabled = enabled
            it.alpha = if (enabled) 1f else 0.5f
        }
    }

    private fun resetAnswerAppearance() {
        answerButtons().forEach {
            it.isEnabled = true
            it.alpha = 1f
        }
    }

    private fun onAnswerSelected(selectedIndex: Int) {
        val quest = currentQuest ?: return
        cancelTimer()
        setAnswersEnabled(false)

        if (selectedIndex == quest.correctIndex) {
            onCorrectAnswer(quest)
        } else {
            val correctText = quest.options[quest.correctIndex]
            onWrongAnswer(getString(R.string.adventure_wrong_reason, correctText))
        }
    }

    private fun onCorrectAnswer(quest: AdventureQuest) {
        player().addMana(MP_REWARD)
        updatePlayerStats()

        Toast.makeText(this, R.string.adventure_correct_toast, Toast.LENGTH_SHORT).show()

        val completedQuest = quest.questNumber
        player().adventureQuest = completedQuest + 1
        saveProgress()

        if (completedQuest % CHECKPOINT_INTERVAL == 0) {
            showCheckpoint(completedQuest)
        } else {
            startNextQuest()
        }
    }

    private fun onWrongAnswer(reason: String) {
        setAnswersEnabled(false)
        cancelTimer()

        player().takeDamage(DAMAGE_ON_WRONG)
        updatePlayerStats()

        binding.cardQuest.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake))

        if (!player().isAlive()) {
            showGameOver()
            return
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.adventure_hit_title)
            .setMessage(getString(R.string.adventure_hit_message, DAMAGE_ON_WRONG, reason))
            .setPositiveButton(R.string.adventure_retry_quest) { _, _ ->
                retryCurrentQuest()
            }
            .setCancelable(false)
            .show()
    }

    private fun showCheckpoint(completedQuest: Int) {
        updatePlayerStats()

        AlertDialog.Builder(this)
            .setTitle(R.string.adventure_checkpoint_title)
            .setMessage(
                getString(
                    R.string.adventure_checkpoint_message,
                    completedQuest,
                    player().health,
                    player().mana
                )
            )
            .setPositiveButton(R.string.adventure_heal_mp) { _, _ ->
                healAtCheckpoint()
            }
            .setNeutralButton(R.string.adventure_save_game) { _, _ ->
                saveProgress()
                Toast.makeText(this, R.string.adventure_saved_toast, Toast.LENGTH_SHORT).show()
                startNextQuest()
            }
            .setNegativeButton(R.string.adventure_continue) { _, _ ->
                startNextQuest()
            }
            .setCancelable(false)
            .show()
    }

    private fun healAtCheckpoint() {
        if (player().mana <= 0) {
            Toast.makeText(this, R.string.adventure_no_mp, Toast.LENGTH_SHORT).show()
            showCheckpoint(player().adventureQuest - 1)
            return
        }
        if (player().health >= Player.MAX_HEALTH) {
            Toast.makeText(this, R.string.adventure_hp_full, Toast.LENGTH_SHORT).show()
            showCheckpoint(player().adventureQuest - 1)
            return
        }

        val healed = player().spendManaForHeal(1, HEAL_PER_MP)
        updatePlayerStats()
        saveProgress()

        if (healed > 0) {
            Toast.makeText(
                this,
                getString(R.string.adventure_healed_toast, healed, HEAL_PER_MP),
                Toast.LENGTH_SHORT
            ).show()
        }
        showCheckpoint(player().adventureQuest - 1)
    }

    private fun showGameOver() {
        player().health = Player.MAX_HEALTH / 2
        saveProgress()
        AlertDialog.Builder(this)
            .setTitle(R.string.adventure_game_over_title)
            .setMessage(R.string.adventure_game_over_message)
            .setPositiveButton(R.string.adventure_back_menu) { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun updatePlayerStats() {
        val p = player()
        binding.tvHP.text = getString(R.string.adventure_hp_value, p.health, Player.MAX_HEALTH)
        binding.tvMP.text = getString(R.string.adventure_mp_value, p.mana, Player.MAX_MP)
        binding.progressHP.progress = p.health
        binding.progressMP.progress = p.mana
    }

    private fun saveProgress() {
        GameManager.savePlayer(this)
    }

    companion object {
        private const val QUEST_TIME_SECONDS = 30
        private const val DAMAGE_ON_WRONG = 15
        private const val MP_REWARD = 1
        private const val HEAL_PER_MP = 5
        private const val CHECKPOINT_INTERVAL = 5
    }
}
