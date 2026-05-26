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
import com.abc.musicadventure.managers.GameManager
import com.abc.musicadventure.ui.NavCardItem
import com.abc.musicadventure.ui.TrainingGridHelper
import com.abc.musicadventure.models.ChordCategory
import com.abc.musicadventure.models.ChordType
import com.abc.musicadventure.models.IntervalGroup
import com.abc.musicadventure.models.IntervalLesson
import com.abc.musicadventure.models.ReadingLesson
import com.abc.musicadventure.models.ReadingTopic
import com.abc.musicadventure.models.ScaleCategory
import com.abc.musicadventure.models.ScaleLesson
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

        setupTrainingModeGrid()
        buildChordSectionGrid()
        buildReadingSectionGrid()
        buildIntervalSectionGrid()
        buildScaleSectionGrid()
        setupListeners()
        showTrainingMode(TrainingMode.CHORD)
        selectCategory(ChordCategory.TRIADS)
        selectReadingTopic(ReadingTopic.NOTATION)
        selectIntervalGroup(IntervalGroup.SECOND)
        selectScaleCategory(ScaleCategory.DIATONIC)
    }

    private fun setupTrainingModeGrid() {
        val items = listOf(
            modeNavItem(TrainingMode.CHORD, R.string.training_mode_chord, R.string.training_mode_chord_desc, "🎹"),
            modeNavItem(TrainingMode.READING, R.string.training_mode_reading, R.string.training_mode_reading_desc, "📖"),
            modeNavItem(TrainingMode.INTERVALS, R.string.training_mode_intervals, R.string.training_mode_intervals_desc, "↔️"),
            modeNavItem(TrainingMode.SCALES, R.string.training_mode_scales, R.string.training_mode_scales_desc, "🎼"),
            modeNavItem(TrainingMode.HARMONIC, R.string.training_mode_harmonic, R.string.training_mode_harmonic_desc, "🎛️")
        )
        TrainingGridHelper.populateTwoColumnGrid(
            container = binding.gridTrainingModes,
            inflater = layoutInflater,
            layoutRes = R.layout.item_training_mode_card,
            items = items,
            selectedId = trainingMode.id,
            accentColorRes = R.color.cyan_primary,
            bindViews = TrainingGridHelper::bindModeCard,
            onItemClick = { item ->
                showTrainingMode(TrainingMode.fromId(item.id))
                binding.scrollTraining.smoothScrollTo(0, 0)
            }
        )
    }

    private fun modeNavItem(
        mode: TrainingMode,
        titleRes: Int,
        descRes: Int,
        icon: String
    ) = NavCardItem(mode.id, getString(titleRes), getString(descRes), icon)

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

        TrainingGridHelper.refreshGridSelection(
            binding.gridTrainingModes,
            mode.id,
            R.color.cyan_primary
        )

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

    private fun buildChordSectionGrid() {
        val items = ChordCategory.entries.map { category ->
            NavCardItem(category.id, category.title, category.subtitle, chordCategoryIcon(category))
        }
        TrainingGridHelper.populateTwoColumnGrid(
            container = binding.chordSectionsGrid,
            inflater = layoutInflater,
            layoutRes = R.layout.item_section_card,
            items = items,
            selectedId = selectedCategory.id,
            accentColorRes = R.color.cyan_primary,
            bindViews = TrainingGridHelper::bindSectionCard,
            onItemClick = { item -> selectCategory(ChordCategory.fromId(item.id)) }
        )
    }

    private fun chordCategoryIcon(category: ChordCategory): String = when (category) {
        ChordCategory.TRIADS -> "3"
        ChordCategory.SEVENTHS -> "7"
        ChordCategory.EXTENSIONS -> "9"
        ChordCategory.SUSPENDED -> "sus"
        ChordCategory.SPECIAL -> "alt"
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

        TrainingGridHelper.refreshGridSelection(
            binding.chordSectionsGrid,
            category.id,
            R.color.cyan_primary
        )
        populateChordLessonsGrid(chords)
        updateLessonPanel()
        resetEarQuizUi()
    }

    private fun populateChordLessonsGrid(chords: List<ChordType>) {
        val items = chords.map { chord ->
            NavCardItem(
                id = chord.id,
                title = chord.name,
                subtitle = chord.formula,
                icon = chord.displayLabel(chord.exampleRoot)
            )
        }
        TrainingGridHelper.populateLessonGrid(
            container = binding.chordLessonsGrid,
            inflater = layoutInflater,
            items = items,
            selectedId = selectedChord.id,
            accentColorRes = R.color.cyan_primary,
            onItemClick = { item ->
                val chord = chords.first { it.id == item.id }
                selectedChord = chord
                exampleRoot = chord.exampleRoot
                TrainingGridHelper.refreshGridSelection(
                    binding.chordLessonsGrid,
                    chord.id,
                    R.color.cyan_primary
                )
                updateLessonPanel()
                binding.scrollTraining.smoothScrollTo(0, binding.cardLesson.top)
            }
        )
    }

    private fun updateLessonPanel() {
        val chord = selectedChord
        val root = exampleRoot

        binding.tvLessonChordName.text = "${chord.name} (${chord.displayLabel(root)})"
        binding.tvLessonFormula.text = "Formula: ${chord.formula}"
        binding.tvLessonNotes.text = "Notes in $root: ${chord.noteNames(root)}"
        binding.tvLessonStructure.text = chord.structure
    }

    private fun buildReadingSectionGrid() {
        val items = ReadingTopic.entries.map { topic ->
            NavCardItem(topic.id, topic.title, topic.subtitle, readingTopicIcon(topic))
        }
        TrainingGridHelper.populateTwoColumnGrid(
            container = binding.readingSectionsGrid,
            inflater = layoutInflater,
            layoutRes = R.layout.item_section_card,
            items = items,
            selectedId = selectedReadingTopic.id,
            accentColorRes = R.color.gold_primary,
            bindViews = TrainingGridHelper::bindSectionCard,
            onItemClick = { item -> selectReadingTopic(ReadingTopic.fromId(item.id)) }
        )
    }

    private fun readingTopicIcon(topic: ReadingTopic): String = when (topic) {
        ReadingTopic.NOTATION -> "𝄞"
        ReadingTopic.KEY -> "♯"
        ReadingTopic.TIME -> "4/4"
        ReadingTopic.BEATS -> "♩"
        ReadingTopic.MEASURE -> "│"
        ReadingTopic.NOTE_VALUES -> "♪"
    }

    private fun selectReadingTopic(topic: ReadingTopic) {
        selectedReadingTopic = topic
        val lessons = ReadingLesson.forTopic(topic)
        selectedReadingLesson = lessons.first()

        binding.tvReadingTopicOverview.text = topic.overview
        TrainingGridHelper.refreshGridSelection(
            binding.readingSectionsGrid,
            topic.id,
            R.color.gold_primary
        )
        populateReadingLessonsGrid(lessons)
        updateReadingLessonPanel()
    }

    private fun populateReadingLessonsGrid(lessons: List<ReadingLesson>) {
        val items = lessons.map { lesson ->
            NavCardItem(lesson.id, lesson.title, lesson.summary, readingTopicIcon(lesson.topic))
        }
        TrainingGridHelper.populateLessonGrid(
            container = binding.readingLessonsGrid,
            inflater = layoutInflater,
            items = items,
            selectedId = selectedReadingLesson.id,
            accentColorRes = R.color.gold_primary,
            onItemClick = { item ->
                selectedReadingLesson = lessons.first { it.id == item.id }
                TrainingGridHelper.refreshGridSelection(
                    binding.readingLessonsGrid,
                    item.id,
                    R.color.gold_primary
                )
                updateReadingLessonPanel()
                binding.scrollTraining.smoothScrollTo(0, binding.cardReadingLesson.top)
            }
        )
    }

    private fun updateReadingLessonPanel() {
        val lesson = selectedReadingLesson
        binding.tvReadingLessonTitle.text = lesson.title
        binding.tvReadingLessonSummary.text = lesson.summary
        binding.tvReadingLessonContent.text = lesson.content
    }

    private fun buildIntervalSectionGrid() {
        val items = IntervalGroup.entries.map { group ->
            val hint = if (group.overview.length > 42) "${group.overview.take(42)}…" else group.overview
            NavCardItem(group.id, group.title, hint, intervalGroupIcon(group))
        }
        TrainingGridHelper.populateTwoColumnGrid(
            container = binding.intervalSectionsGrid,
            inflater = layoutInflater,
            layoutRes = R.layout.item_section_card,
            items = items,
            selectedId = selectedIntervalGroup.id,
            accentColorRes = R.color.coral_primary,
            bindViews = TrainingGridHelper::bindSectionCard,
            onItemClick = { item -> selectIntervalGroup(IntervalGroup.fromId(item.id)) }
        )
    }

    private fun intervalGroupIcon(group: IntervalGroup): String = when (group) {
        IntervalGroup.SECOND -> "2"
        IntervalGroup.THIRD -> "3"
        IntervalGroup.FOURTH -> "4"
        IntervalGroup.FIFTH -> "5"
        IntervalGroup.SIXTH -> "6"
        IntervalGroup.SEVENTH -> "7"
        IntervalGroup.OCTAVE -> "8"
    }

    private fun selectIntervalGroup(group: IntervalGroup) {
        selectedIntervalGroup = group
        val lessons = IntervalLesson.forGroup(group)
        selectedIntervalLesson = lessons.first()

        binding.tvIntervalGroupOverview.text = group.overview
        TrainingGridHelper.refreshGridSelection(
            binding.intervalSectionsGrid,
            group.id,
            R.color.coral_primary
        )
        populateIntervalLessonsGrid(lessons)
        updateIntervalLessonPanel()
    }

    private fun populateIntervalLessonsGrid(lessons: List<IntervalLesson>) {
        val items = lessons.map { lesson ->
            NavCardItem(lesson.id, lesson.title, lesson.shortLabel, lesson.shortLabel)
        }
        TrainingGridHelper.populateLessonGrid(
            container = binding.intervalLessonsGrid,
            inflater = layoutInflater,
            items = items,
            selectedId = selectedIntervalLesson.id,
            accentColorRes = R.color.coral_primary,
            onItemClick = { item ->
                selectedIntervalLesson = lessons.first { it.id == item.id }
                TrainingGridHelper.refreshGridSelection(
                    binding.intervalLessonsGrid,
                    item.id,
                    R.color.coral_primary
                )
                updateIntervalLessonPanel()
                binding.scrollTraining.smoothScrollTo(0, binding.cardIntervalLesson.top)
            }
        )
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

    private fun buildScaleSectionGrid() {
        val items = ScaleCategory.entries.map { category ->
            NavCardItem(category.id, category.title, category.overview.take(48), "♯")
        }
        TrainingGridHelper.populateTwoColumnGrid(
            container = binding.scaleSectionsGrid,
            inflater = layoutInflater,
            layoutRes = R.layout.item_section_card,
            items = items,
            selectedId = selectedScaleCategory.id,
            accentColorRes = R.color.cyan_primary,
            bindViews = TrainingGridHelper::bindSectionCard,
            onItemClick = { item -> selectScaleCategory(ScaleCategory.fromId(item.id)) }
        )
    }

    private fun selectScaleCategory(category: ScaleCategory) {
        selectedScaleCategory = category
        val lessons = ScaleLesson.forCategory(category)
        selectedScaleLesson = lessons.first()

        binding.tvScaleCategoryOverview.text = category.overview
        TrainingGridHelper.refreshGridSelection(
            binding.scaleSectionsGrid,
            category.id,
            R.color.cyan_primary
        )
        populateScaleLessonsGrid(lessons)
        updateScaleLessonPanel()
    }

    private fun populateScaleLessonsGrid(lessons: List<ScaleLesson>) {
        val items = lessons.map { lesson ->
            NavCardItem(lesson.id, lesson.title, lesson.pattern, "♯")
        }
        TrainingGridHelper.populateLessonGrid(
            container = binding.scaleLessonsGrid,
            inflater = layoutInflater,
            items = items,
            selectedId = selectedScaleLesson.id,
            accentColorRes = R.color.cyan_primary,
            onItemClick = { item ->
                selectedScaleLesson = lessons.first { it.id == item.id }
                TrainingGridHelper.refreshGridSelection(
                    binding.scaleLessonsGrid,
                    item.id,
                    R.color.cyan_primary
                )
                updateScaleLessonPanel()
                binding.scrollTraining.smoothScrollTo(0, binding.cardScaleLesson.top)
            }
        )
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
