package com.abc.musicadventure.ui

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.abc.musicadventure.R
import com.google.android.material.card.MaterialCardView
data class NavCardItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: String
)

object TrainingGridHelper {

    fun populateTwoColumnGrid(
        container: LinearLayout,
        inflater: LayoutInflater,
        layoutRes: Int,
        items: List<NavCardItem>,
        selectedId: String,
        accentColorRes: Int,
        bindViews: (View, NavCardItem) -> Unit,
        onItemClick: (NavCardItem) -> Unit
    ) {
        container.removeAllViews()
        items.chunked(2).forEach { rowItems ->
            val row = LinearLayout(container.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
            }

            rowItems.forEach { item ->
                val cardRoot = inflater.inflate(layoutRes, row, false)
                val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                cardRoot.layoutParams = params
                bindViews(cardRoot, item)
                applySelection(cardRoot, item.id == selectedId, accentColorRes)
                cardRoot.setOnClickListener {
                    onItemClick(item)
                }
                row.addView(cardRoot)
            }

            if (rowItems.size == 1) {
                val spacer = View(container.context).apply {
                    layoutParams = LinearLayout.LayoutParams(0, 0, 1f)
                }
                row.addView(spacer)
            }

            container.addView(row)
        }
    }

    fun refreshGridSelection(
        container: ViewGroup,
        selectedId: String,
        accentColorRes: Int
    ) {
        fun visit(view: View) {
            if (view is MaterialCardView && view.tag is String) {
                applySelection(view, view.tag == selectedId, accentColorRes)
            }
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    visit(view.getChildAt(i))
                }
            }
        }
        visit(container)
    }

    fun applySelection(root: View, selected: Boolean, accentColorRes: Int) {
        val card = when (root) {
            is MaterialCardView -> root
            else -> root.findViewById<MaterialCardView>(R.id.cardLessonTile)
                ?: root as? MaterialCardView
                ?: return
        }
        val context = card.context
        val accent = ContextCompat.getColor(context, accentColorRes)
        val light = ContextCompat.getColor(context, R.color.blue_light)
        card.strokeColor = if (selected) accent else light
        card.strokeWidth = if (selected) 2 else 1
        card.cardElevation = if (selected) 6f else 2f
        card.setCardBackgroundColor(
            ContextCompat.getColor(
                context,
                if (selected) R.color.surface_card_alt else R.color.white_off
            )
        )

        styleIconBadge(card.findViewById(R.id.tvSectionIcon), selected, accent)
        styleIconBadge(card.findViewById(R.id.tvLessonTileIcon), selected, accent)
    }

    private fun styleIconBadge(icon: TextView?, selected: Boolean, accentColor: Int) {
        if (icon == null) return
        val context = icon.context
        val cornerPx = 12f * context.resources.displayMetrics.density
        val background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            this.cornerRadius = cornerPx
            setColor(
                if (selected) accentColor
                else ContextCompat.getColor(context, R.color.icon_badge_bg)
            )
        }
        icon.background = background
        icon.setTextColor(
            ContextCompat.getColor(
                context,
                if (selected) R.color.icon_badge_text_selected
                else R.color.icon_badge_text
            )
        )
        icon.paint.isFakeBoldText = true
    }

    fun bindSectionCard(root: View, item: NavCardItem) {
        root.tag = item.id
        root.findViewById<TextView>(R.id.tvSectionIcon)?.text = item.icon
        root.findViewById<TextView>(R.id.tvSectionTitle)?.text = item.title
        root.findViewById<TextView>(R.id.tvSectionSubtitle)?.text = item.subtitle
    }

    fun bindModeCard(root: View, item: NavCardItem) {
        root.tag = item.id
        root.findViewById<TextView>(R.id.tvModeIcon)?.text = item.icon
        root.findViewById<TextView>(R.id.tvModeTitle)?.text = item.title
        root.findViewById<TextView>(R.id.tvModeDesc)?.text = item.subtitle
    }

    fun bindLessonTile(root: View, item: NavCardItem) {
        root.tag = item.id
        root.findViewById<TextView>(R.id.tvLessonTileIcon)?.text = item.icon
        root.findViewById<TextView>(R.id.tvLessonTileTitle)?.text = item.title
        root.findViewById<TextView>(R.id.tvLessonTileMeta)?.text = item.subtitle
    }

    fun populateLessonGrid(
        container: LinearLayout,
        inflater: LayoutInflater,
        items: List<NavCardItem>,
        selectedId: String,
        accentColorRes: Int,
        onItemClick: (NavCardItem) -> Unit
    ) {
        populateTwoColumnGrid(
            container = container,
            inflater = inflater,
            layoutRes = R.layout.item_lesson_tile,
            items = items,
            selectedId = selectedId,
            accentColorRes = accentColorRes,
            bindViews = { view, item -> bindLessonTile(view, item) },
            onItemClick = onItemClick
        )
    }
}
