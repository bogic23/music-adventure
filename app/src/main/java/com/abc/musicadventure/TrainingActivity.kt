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
import com.abc.musicadventure.models.IntervalGroup
import com.abc.musicadventure.models.IntervalLesson
import com.abc.musicadventure.models.ReadingLesson
import com.abc.musicadventure.models.ReadingTopic
import com.abc.musicadventure.models.ScaleCategory
import com.abc.musicadventure.models.ScaleLesson
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip

class TrainingActivity : AppCompatActivity() {

    private enum class TrainingMode(val id: String) {
        CHORD("chord"),
        READING("reading"),
        INTERVALS("intervals"),
        SCALES("scales"),
        HARMONIC("harmonic");

        companion object {
            fun fromId(id: String): TrainingMode =
                entries.firstOrNull { it.id == id } ?: CHORD
        }
    }

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
    private var updatingTrainingModeChips = false
    private var updatingIntervalGroupChips = false
    private var updatingScaleCategoryChips = false

    private var trainingMode: TrainingMode = TrainingMode.CHORD
    private var selectedReadingTopic: ReadingTopic = ReadingTopic.NOTATION
    private var selectedReadingLesson: ReadingLesson =
        ReadingLesson.forTopic(ReadingTopic.NOTATION).first()
    private var selectedIntervalGroup: IntervalGroup = IntervalGroup.SECOND
    private var selectedIntervalLesson: IntervalLesson =
        IntervalLesson.forGroup(IntervalGroup.SECOND).first()
    private var selectedScaleCategory: ScaleCategory = ScaleCategory.DIATONIC
    private var selectedScaleLesson: ScaleLesson =
        ScaleLesson.forCategory(ScaleCategory.DIATONIC).first()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrainingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTrainingModeChips()
        setupCategoryChips()
        setupReadingTopicChips()
        setupIntervalGroupChips()
        setupScaleCategoryChips()
        setupListeners()
        showTrainingMode(TrainingMode.CHORD)
        selectCategory(ChordCategory.TRIADS)
        selectReadingTopic(ReadingTopic.NOTATION)
        selectIntervalGroup(IntervalGroup.SECOND)
        selectScaleCategory(ScaleCategory.DIATONIC)
    }

    private fun setupTrainingModeChips() {
        val chipGroup = binding.chipGroupTrainingModes
        chipGroup.removeAllViews()

        val modes = listOf(
            TrainingMode.CHORD to R.string.training_mode_chord,
            TrainingMode.READING to R.string.training_mode_reading,
            TrainingMode.INTERVALS to R.string.training_mode_intervals,
            TrainingMode.SCALES to R.string.training_mode_scales,
            TrainingMode.HARMONIC to R.string.training_mode_harmonic
        )

        modes.forEach { (mode, labelRes) ->
            val chip = layoutInflater.inflate(
                R.layout.item_training_category,
                chipGroup,
                false
            ) as Chip
            chip.id = View.generateViewId()
            chip.text = getString(labelRes)
            chip.tag = mode.id
            chip.isCheckable = true
            chipGroup.addView(chip)
        }

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (updatingTrainingModeChips || checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val chip = findCheckedChip(group) ?: return@setOnCheckedStateChangeListener
            showTrainingMode(TrainingMode.fromId(chip.tag as String))
        }
    }

    private fun showTrainingMode(mode: TrainingMode) {
        trainingMode = mode
        binding.panelChordTraining.isVisible = mode == TrainingMode.CHORD
        binding.panelReading.isVisible = mode == TrainingMode.READING
        binding.panelIntervals.isVisible = mode == TrainingMode.INTERVALS
        binding.panelScales.isVisible = mode == TrainingMode.SCALES
        binding.panelHarmonic.isVisible = mode == TrainingMode.HARMONIC

        if (mode != TrainingMode.CHORD) {
            tonePlayer.stop()
            hideKeyboard()
            resetEarQuizUi()
        }

        val chipGroup = binding.chipGroupTrainingModes
        updatingTrainingModeChips = true
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            chip.isChecked = chip.tag == mode.id
        }
        updatingTrainingModeChips = false

        when (mode) {
            TrainingMode.CHORD -> {
                binding.tvTitle.text = getString(R.string.training_mode_chord_header)
                binding.tvSubtitle.text =
                    "${selectedCategory.title} · ${selectedCategory.subtitle}"
            }
            TrainingMode.READING -> {
                binding.tvTitle.text = getString(R.string.training_mode_reading_header)
                binding.tvSubtitle.text = getString(R.string.training_reading_subtitle)
            }
            TrainingMode.INTERVALS -> {
                binding.tvTitle.text = getString(R.string.training_mode_intervals_header)
                binding.tvSubtitle.text = getString(R.string.training_intervals_subtitle)
            }
            TrainingMode.SCALES -> {
                binding.tvTitle.text = getString(R.string.training_mode_scales_header)
                binding.tvSubtitle.text = getString(R.string.training_scales_subtitle)
            }
            TrainingMode.HARMONIC -> {
                binding.tvTitle.text = getString(R.string.training_mode_harmonic_header)
                binding.tvSubtitle.text = getString(R.string.training_harmonic_subtitle)
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
            val chip = findCheckedChip(group) ?: return@setOnCheckedStateChangeListener
            selectCategory(ChordCategory.fromId(chip.tag as String))
        }
    }

    private fun findCheckedChip(group: ViewGroup): Chip? {
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
            val chip = findCheckedChip(group) ?: return@setOnCheckedStateChangeListener
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
        applyLessonSelection(card, lesson.id == selectedReadingLesson.id, R.color.gold_primary)

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
            applyLessonSelection(
                card,
                lessons.getOrNull(i)?.id == selectedReadingLesson.id,
                R.color.gold_primary
            )
        }
    }

    private fun applyLessonSelection(
        card: MaterialCardView,
        selected: Boolean,
        accentColorRes: Int
    ) {
        val strokeColor = if (selected) accentColorRes else R.color.blue_light
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
        ReadingTopic.TIME -> "4/4"
        ReadingTopic.BEATS -> "♩"
        ReadingTopic.MEASURE -> "│"
        ReadingTopic.NOTE_VALUES -> "♪"
    }

    private fun setupIntervalGroupChips() {
        val chipGroup = binding.chipGroupIntervalGroups
        chipGroup.removeAllViews()

        IntervalGroup.entries.forEach { group ->
            val chip = layoutInflater.inflate(
                R.layout.item_training_category,
                chipGroup,
                false
            ) as Chip
            chip.id = View.generateViewId()
            chip.text = group.title
            chip.tag = group.id
            chip.isCheckable = true
            chipGroup.addView(chip)
        }

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (updatingIntervalGroupChips || checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val chip = findCheckedChip(group) ?: return@setOnCheckedStateChangeListener
            selectIntervalGroup(IntervalGroup.fromId(chip.tag as String))
        }
    }

    private fun selectIntervalGroup(group: IntervalGroup) {
        selectedIntervalGroup = group
        val lessons = IntervalLesson.forGroup(group)
        selectedIntervalLesson = lessons.first()

        binding.tvIntervalGroupOverview.text = group.overview

        val chipGroup = binding.chipGroupIntervalGroups
        updatingIntervalGroupChips = true
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            chip.isChecked = chip.tag == group.id
        }
        updatingIntervalGroupChips = false

        populateIntervalLessonList(lessons)
        updateIntervalLessonPanel()
    }

    private fun populateIntervalLessonList(lessons: List<IntervalLesson>) {
        val container = binding.intervalLessonsContainer
        container.removeAllViews()

        lessons.forEach { lesson ->
            val itemBinding = ItemReadingLessonBinding.inflate(layoutInflater, container, false)
            bindIntervalLessonItem(itemBinding, lesson)
            container.addView(itemBinding.root)
        }
    }

    private fun bindIntervalLessonItem(itemBinding: ItemReadingLessonBinding, lesson: IntervalLesson) {
        itemBinding.tvReadingIcon.text = lesson.shortLabel
        itemBinding.tvReadingTitle.text = lesson.title
        itemBinding.tvReadingSummary.text = lesson.summary

        val card = itemBinding.root as MaterialCardView
        applyLessonSelection(card, lesson.id == selectedIntervalLesson.id, R.color.coral_primary)

        card.setOnClickListener {
            selectedIntervalLesson = lesson
            refreshIntervalSelections()
            updateIntervalLessonPanel()
            binding.scrollTraining.smoothScrollTo(0, binding.cardIntervalLesson.top)
        }
    }

    private fun refreshIntervalSelections() {
        val container = binding.intervalLessonsContainer
        val lessons = IntervalLesson.forGroup(selectedIntervalGroup)
        for (i in 0 until container.childCount) {
            val card = container.getChildAt(i) as MaterialCardView
            applyLessonSelection(
                card,
                lessons.getOrNull(i)?.id == selectedIntervalLesson.id,
                R.color.coral_primary
            )
        }
    }

    private fun updateIntervalLessonPanel() {
        val lesson = selectedIntervalLesson
        binding.tvIntervalLessonTitle.text = lesson.title
        binding.tvIntervalLessonSummary.text = lesson.summary
        binding.tvIntervalLessonMeta.text = getString(
            R.string.training_interval_semitones,
            lesson.semitones,
            lesson.shortLabel
        )
        binding.tvIntervalLessonContent.text = lesson.content
    }

    private fun setupScaleCategoryChips() {
        val chipGroup = binding.chipGroupScaleCategories
        chipGroup.removeAllViews()

        ScaleCategory.entries.forEach { category ->
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
            if (updatingScaleCategoryChips || checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val chip = findCheckedChip(group) ?: return@setOnCheckedStateChangeListener
            selectScaleCategory(ScaleCategory.fromId(chip.tag as String))
        }
    }

    private fun selectScaleCategory(category: ScaleCategory) {
        selectedScaleCategory = category
        val lessons = ScaleLesson.forCategory(category)
        selectedScaleLesson = lessons.first()

        binding.tvScaleCategoryOverview.text = category.overview

        val chipGroup = binding.chipGroupScaleCategories
        updatingScaleCategoryChips = true
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            chip.isChecked = chip.tag == category.id
        }
        updatingScaleCategoryChips = false

        populateScaleLessonList(lessons)
        updateScaleLessonPanel()
    }

    private fun populateScaleLessonList(lessons: List<ScaleLesson>) {
        val container = binding.scaleLessonsContainer
        container.removeAllViews()

        lessons.forEach { lesson ->
            val itemBinding = ItemReadingLessonBinding.inflate(layoutInflater, container, false)
            bindScaleLessonItem(itemBinding, lesson)
            container.addView(itemBinding.root)
        }
    }

    private fun bindScaleLessonItem(itemBinding: ItemReadingLessonBinding, lesson: ScaleLesson) {
        itemBinding.tvReadingIcon.text = "♯"
        itemBinding.tvReadingTitle.text = lesson.title
        itemBinding.tvReadingSummary.text = lesson.summary

        val card = itemBinding.root as MaterialCardView
        applyLessonSelection(card, lesson.id == selectedScaleLesson.id, R.color.cyan_primary)

        card.setOnClickListener {
            selectedScaleLesson = lesson
            refreshScaleSelections()
            updateScaleLessonPanel()
            binding.scrollTraining.smoothScrollTo(0, binding.cardScaleLesson.top)
        }
    }

    private fun refreshScaleSelections() {
        val container = binding.scaleLessonsContainer
        val lessons = ScaleLesson.forCategory(selectedScaleCategory)
        for (i in 0 until container.childCount) {
            val card = container.getChildAt(i) as MaterialCardView
            applyLessonSelection(
                card,
                lessons.getOrNull(i)?.id == selectedScaleLesson.id,
                R.color.cyan_primary
            )
        }
    }

    private fun updateScaleLessonPanel() {
        val lesson = selectedScaleLesson
        binding.tvScaleLessonTitle.text = lesson.title
        binding.tvScaleLessonSummary.text = lesson.summary
        binding.tvScaleLessonPattern.text = getString(R.string.training_scale_pattern, lesson.pattern)
        binding.tvScaleLessonContent.text = lesson.content
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
