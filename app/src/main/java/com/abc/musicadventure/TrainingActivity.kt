package com.abc.musicadventure

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.abc.musicadventure.audio.ChordTonePlayer
import com.abc.musicadventure.databinding.ActivityTrainingBinding
import com.abc.musicadventure.databinding.ItemChordLessonBinding
import com.abc.musicadventure.databinding.ItemReadingLessonBinding
import com.abc.musicadventure.managers.GameManager
import com.abc.musicadventure.models.ChordCategory
import com.abc.musicadventure.models.ChordType
import com.abc.musicadventure.models.ReadingLesson
import com.abc.musicadventure.models.ReadingTopic
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip

class TrainingActivity : AppCompatActivity() {

    private enum class TrainingMode { CHORD, READING }

    private lateinit var binding: ActivityTrainingBinding
    private val tonePlayer = ChordTonePlayer()

    private var selectedCategory: ChordCategory = ChordCategory.TRIADS
    private var selectedChord: ChordType = ChordType.forCategory(ChordCategory.TRIADS).first()
    private var exampleRoot: String = "C"

    private var earQuizChord: ChordType? = null
    private var earQuizRoot: String = "C"
    private var earQuizUseBlock: Boolean = true
    private var updatingCategoryChips = false
    private var updatingReadingTopicChips = false

    private var trainingMode: TrainingMode = TrainingMode.CHORD
    private var selectedReadingTopic: ReadingTopic = ReadingTopic.NOTATION
    private var selectedReadingLesson: ReadingLesson =
        ReadingLesson.forTopic(ReadingTopic.NOTATION).first()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrainingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupModeToggle()
        setupCategoryChips()
        setupReadingTopicChips()
        setupListeners()
        showTrainingMode(TrainingMode.CHORD)
        selectCategory(ChordCategory.TRIADS)
        selectReadingTopic(ReadingTopic.NOTATION)
    }

    private fun setupModeToggle() {
        binding.toggleTrainingMode.check(R.id.btnModeChord)
        binding.toggleTrainingMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            when (checkedId) {
                R.id.btnModeChord -> showTrainingMode(TrainingMode.CHORD)
                R.id.btnModeReading -> showTrainingMode(TrainingMode.READING)
            }
        }
    }

    private fun showTrainingMode(mode: TrainingMode) {
        trainingMode = mode
        binding.panelChordTraining.isVisible = mode == TrainingMode.CHORD
        binding.panelReading.isVisible = mode == TrainingMode.READING

        when (mode) {
            TrainingMode.CHORD -> {
                binding.tvTitle.text = getString(R.string.training_mode_chord_header)
                binding.tvSubtitle.text =
                    "${selectedCategory.title} · ${selectedCategory.subtitle}"
            }
            TrainingMode.READING -> {
                binding.tvTitle.text = getString(R.string.training_mode_reading_header)
                binding.tvSubtitle.text = getString(R.string.training_reading_subtitle)
                tonePlayer.stop()
                hideKeyboard()
                resetEarQuizUi()
            }
        }
    }

    override fun onDestroy() {
        tonePlayer.release()
        super.onDestroy()
    }

    private fun setupCategoryChips() {
        val chipGroup = binding.chipGroupCategories
        chipGroup.removeAllViews()

        ChordCategory.entries.forEach { category ->
            val chip = layoutInflater.inflate(
                R.layout.item_training_category,
                chipGroup,
                false
            ) as Chip
            chip.id = View.generateViewId()
            chip.text = category.title
            chip.tag = category.id
            chip.isCheckable = true
            chipGroup.addView(chip)
        }

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (updatingCategoryChips || checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val chip = findCheckedCategoryChip(group) ?: return@setOnCheckedStateChangeListener
            selectCategory(ChordCategory.fromId(chip.tag as String))
        }
    }

    private fun findCheckedCategoryChip(group: ViewGroup): Chip? {
        for (i in 0 until group.childCount) {
            val chip = group.getChildAt(i) as? Chip ?: continue
            if (chip.isChecked) return chip
        }
        return null
    }

    private fun selectCategory(category: ChordCategory) {
        selectedCategory = category
        val chords = ChordType.forCategory(category)
        selectedChord = chords.first()
        exampleRoot = selectedChord.exampleRoot

        binding.tvCategoryOverview.text = category.overview
        if (trainingMode == TrainingMode.CHORD) {
            binding.tvSubtitle.text = "${category.title} · ${category.subtitle}"
        }

        val chipGroup = binding.chipGroupCategories
        updatingCategoryChips = true
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            chip.isChecked = chip.tag == category.id
        }
        updatingCategoryChips = false

        populateChordList(chords)
        updateLessonPanel()
        resetEarQuizUi()
    }

    private fun populateChordList(chords: List<ChordType>) {
        val container = binding.chordsContainer
        container.removeAllViews()

        chords.forEach { chord ->
            val itemBinding = ItemChordLessonBinding.inflate(layoutInflater, container, false)
            bindChordItem(itemBinding, chord)
            container.addView(itemBinding.root)
        }
    }

    private fun bindChordItem(itemBinding: ItemChordLessonBinding, chord: ChordType) {
        val root = chord.exampleRoot
        itemBinding.tvChordSymbol.text = chord.displayLabel(root)
        itemBinding.tvChordName.text = chord.name
        itemBinding.tvChordFormula.text = chord.formula

        val card = itemBinding.root as MaterialCardView
        val isSelected = chord.id == selectedChord.id
        applyChordSelection(card, isSelected)

        card.setOnClickListener {
            selectedChord = chord
            exampleRoot = chord.exampleRoot
            refreshChordSelections()
            updateLessonPanel()
            binding.scrollTraining.smoothScrollTo(0, binding.cardLesson.top)
        }
    }

    private fun refreshChordSelections() {
        val container = binding.chordsContainer
        val chords = ChordType.forCategory(selectedCategory)
        for (i in 0 until container.childCount) {
            val card = container.getChildAt(i) as MaterialCardView
            applyChordSelection(card, chords.getOrNull(i)?.id == selectedChord.id)
        }
    }

    private fun applyChordSelection(card: MaterialCardView, selected: Boolean) {
        val strokeColor = if (selected) R.color.cyan_primary else R.color.blue_light
        val strokeWidth = if (selected) 2 else 1
        card.strokeColor = ContextCompat.getColor(this, strokeColor)
        card.strokeWidth = strokeWidth
        card.cardElevation = if (selected) 8f else 2f
    }

    private fun updateLessonPanel() {
        val chord = selectedChord
        val root = exampleRoot

        binding.tvLessonChordName.text = "${chord.name} (${chord.displayLabel(root)})"
        binding.tvLessonFormula.text = "Formula: ${chord.formula}"
        binding.tvLessonNotes.text = "Notes in $root: ${chord.noteNames(root)}"
        binding.tvLessonStructure.text = chord.structure
    }

    private fun setupReadingTopicChips() {
        val chipGroup = binding.chipGroupReadingTopics
        chipGroup.removeAllViews()

        ReadingTopic.entries.forEach { topic ->
            val chip = layoutInflater.inflate(
                R.layout.item_training_category,
                chipGroup,
                false
            ) as Chip
            chip.id = View.generateViewId()
            chip.text = topic.title
            chip.tag = topic.id
            chip.isCheckable = true
            chipGroup.addView(chip)
        }

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (updatingReadingTopicChips || checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val chip = findCheckedCategoryChip(group) ?: return@setOnCheckedStateChangeListener
            selectReadingTopic(ReadingTopic.fromId(chip.tag as String))
        }
    }

    private fun selectReadingTopic(topic: ReadingTopic) {
        selectedReadingTopic = topic
        val lessons = ReadingLesson.forTopic(topic)
        selectedReadingLesson = lessons.first()

        binding.tvReadingTopicOverview.text = topic.overview

        val chipGroup = binding.chipGroupReadingTopics
        updatingReadingTopicChips = true
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            chip.isChecked = chip.tag == topic.id
        }
        updatingReadingTopicChips = false

        populateReadingLessonList(lessons)
        updateReadingLessonPanel()
    }

    private fun populateReadingLessonList(lessons: List<ReadingLesson>) {
        val container = binding.readingLessonsContainer
        container.removeAllViews()

        lessons.forEach { lesson ->
            val itemBinding = ItemReadingLessonBinding.inflate(layoutInflater, container, false)
            bindReadingLessonItem(itemBinding, lesson)
            container.addView(itemBinding.root)
        }
    }

    private fun bindReadingLessonItem(itemBinding: ItemReadingLessonBinding, lesson: ReadingLesson) {
        itemBinding.tvReadingIcon.text = readingLessonIcon(lesson)
        itemBinding.tvReadingTitle.text = lesson.title
        itemBinding.tvReadingSummary.text = lesson.summary

        val card = itemBinding.root as MaterialCardView
        applyReadingSelection(card, lesson.id == selectedReadingLesson.id)

        card.setOnClickListener {
            selectedReadingLesson = lesson
            refreshReadingSelections()
            updateReadingLessonPanel()
            binding.scrollTraining.smoothScrollTo(0, binding.cardReadingLesson.top)
        }
    }

    private fun refreshReadingSelections() {
        val container = binding.readingLessonsContainer
        val lessons = ReadingLesson.forTopic(selectedReadingTopic)
        for (i in 0 until container.childCount) {
            val card = container.getChildAt(i) as MaterialCardView
            applyReadingSelection(card, lessons.getOrNull(i)?.id == selectedReadingLesson.id)
        }
    }

    private fun applyReadingSelection(card: MaterialCardView, selected: Boolean) {
        val strokeColor = if (selected) R.color.gold_primary else R.color.blue_light
        card.strokeColor = ContextCompat.getColor(this, strokeColor)
        card.strokeWidth = if (selected) 2 else 1
        card.cardElevation = if (selected) 8f else 2f
    }

    private fun updateReadingLessonPanel() {
        val lesson = selectedReadingLesson
        binding.tvReadingLessonTitle.text = lesson.title
        binding.tvReadingLessonSummary.text = lesson.summary
        binding.tvReadingLessonContent.text = lesson.content
    }

    private fun readingLessonIcon(lesson: ReadingLesson): String = when (lesson.topic) {
        ReadingTopic.NOTATION -> "𝄞"
        ReadingTopic.KEY -> "♯"
        ReadingTopic.TIME -> "𝄴"
        ReadingTopic.BEATS -> "♩"
        ReadingTopic.MEASURE -> "│"
        ReadingTopic.NOTE_VALUES -> "♪"
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        binding.btnPlayExample.setOnClickListener {
            playExample(block = true)
        }

        binding.btnPlayArpeggio.setOnClickListener {
            playExample(block = false)
        }

        binding.btnEarQuizChord.setOnClickListener {
            startEarTrainingRound(block = true)
        }

        binding.btnEarQuizArpeggio.setOnClickListener {
            startEarTrainingRound(block = false)
        }

        binding.btnReplayEar.setOnClickListener {
            replayEarTrainingChord()
        }

        binding.btnSubmitEar.setOnClickListener {
            validateEarAnswer()
        }

        binding.etEarAnswer.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                validateEarAnswer()
                true
            } else {
                false
            }
        }

        binding.etEarAnswer.doAfterTextChanged {
            binding.tilEarAnswer.error = null
            binding.tvEarFeedback.isVisible = false
        }
    }

    private fun playExample(block: Boolean) {
        binding.btnPlayExample.isEnabled = false
        binding.btnPlayArpeggio.isEnabled = false

        val onDone = { runOnUiThread { enablePlayButtons() } }
        if (block) {
            tonePlayer.playBlockChord(selectedChord, exampleRoot, onDone)
        } else {
            tonePlayer.playChord(selectedChord, exampleRoot, onDone)
        }

        Toast.makeText(
            this,
            getString(R.string.training_playing, selectedChord.displayLabel(exampleRoot)),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun enablePlayButtons() {
        binding.btnPlayExample.isEnabled = true
        binding.btnPlayArpeggio.isEnabled = true
    }

    private fun startEarTrainingRound(block: Boolean) {
        val pool = ChordType.forCategory(selectedCategory)
        if (pool.isEmpty()) return

        earQuizUseBlock = block
        earQuizChord = pool.random()
        earQuizRoot = ChordType.randomRoot()

        binding.earQuizPanel.isVisible = true
        binding.tvEarFeedback.isVisible = false
        binding.tilEarAnswer.error = null
        binding.etEarAnswer.text = null
        binding.etEarAnswer.isEnabled = false
        binding.btnSubmitEar.isEnabled = false
        binding.btnReplayEar.isEnabled = false
        setEarQuizButtonsEnabled(false)
        binding.tvEarPrompt.text = getString(R.string.training_ear_playing)

        playEarQuizChord {
            runOnUiThread { onEarQuizReady() }
        }
    }

    private fun playEarQuizChord(onComplete: () -> Unit) {
        val chord = earQuizChord ?: return
        if (earQuizUseBlock) {
            tonePlayer.playBlockChord(chord, earQuizRoot, onComplete)
        } else {
            tonePlayer.playChord(chord, earQuizRoot, onComplete)
        }
    }

    private fun setEarQuizButtonsEnabled(enabled: Boolean) {
        binding.btnEarQuizChord.isEnabled = enabled
        binding.btnEarQuizArpeggio.isEnabled = enabled
    }

    private fun onEarQuizReady() {
        binding.tvEarPrompt.text = getString(R.string.training_ear_prompt)
        binding.etEarAnswer.isEnabled = true
        binding.btnSubmitEar.isEnabled = true
        binding.btnReplayEar.isEnabled = true
        setEarQuizButtonsEnabled(true)
        binding.etEarAnswer.requestFocus()
        showKeyboard(binding.etEarAnswer)
    }

    private fun replayEarTrainingChord() {
        if (earQuizChord == null) return
        binding.btnReplayEar.isEnabled = false
        binding.btnSubmitEar.isEnabled = false
        setEarQuizButtonsEnabled(false)
        binding.tvEarPrompt.text = getString(R.string.training_ear_playing)

        playEarQuizChord {
            runOnUiThread { onEarQuizReady() }
        }
    }

    private fun validateEarAnswer() {
        val chord = earQuizChord ?: return
        val answer = binding.etEarAnswer.text?.toString()?.trim().orEmpty()

        if (answer.isEmpty()) {
            binding.tilEarAnswer.error = getString(R.string.training_ear_empty)
            return
        }

        val correctLabel = chord.displayLabel(earQuizRoot)
        if (chord.matchesAnswer(answer, earQuizRoot)) {
            showEarFeedback(
                getString(R.string.training_ear_correct, correctLabel),
                isCorrect = true
            )
            GameManager.currentPlayer?.apply {
                addExperience(20)
                musicKnowledge++
            }
        } else {
            showEarFeedback(
                getString(R.string.training_ear_wrong, correctLabel),
                isCorrect = false
            )
        }

        hideKeyboard()
    }

    private fun showEarFeedback(message: String, isCorrect: Boolean) {
        binding.tvEarFeedback.isVisible = true
        binding.tvEarFeedback.text = message
        binding.tvEarFeedback.setTextColor(
            ContextCompat.getColor(
                this,
                if (isCorrect) R.color.success_green else R.color.danger_red
            )
        )
    }

    private fun resetEarQuizUi() {
        earQuizChord = null
        binding.earQuizPanel.isVisible = false
        binding.tvEarFeedback.isVisible = false
        binding.tilEarAnswer.error = null
        binding.etEarAnswer.text = null
        setEarQuizButtonsEnabled(true)
        hideKeyboard()
    }

    private fun showKeyboard(view: View) {
        view.post {
            val imm = ContextCompat.getSystemService(this, InputMethodManager::class.java)
            imm?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun hideKeyboard() {
        val imm = ContextCompat.getSystemService(this, InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(binding.etEarAnswer.windowToken, 0)
    }
}
