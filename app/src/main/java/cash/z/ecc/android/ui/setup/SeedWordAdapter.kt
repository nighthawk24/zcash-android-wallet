package com.tylersuehr.chips

import android.content.Context
import android.text.TextUtils
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cash.z.ecc.android.R
import cash.z.ecc.android.ext.toAppColor
import cash.z.ecc.android.ui.setup.SeedWordChip

class SeedWordAdapter : ChipsAdapter {

    constructor(existingAdapter: ChipsAdapter) : super(existingAdapter.mDataSource, existingAdapter.mEditText, existingAdapter.mOptions)

    val editText = mEditText
    private var onDataSetChangedListener: (() -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == CHIP) SeedWordHolder(SeedWordChipView(parent.context))
        else object : RecyclerView.ViewHolder(mEditText) {}
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == CHIP) { // Chips
            // Display the chip information on the chip view
            (holder as SeedWordHolder).seedChipView.bind(mDataSource.getSelectedChip(position), position)
        } else {
            val size = mDataSource.selectedChips.size

            // tricky bugfix:
            // keep this always enabled otherwise older versions of android crash when this
            // view is given focus. As a work around, just hide the cursor when the user is done
            // editing. This is not ideal but it's better than a crash during wallet restore!
            mEditText.isEnabled = true
            mEditText.hint = if (size < 3) {
                mEditText.isCursorVisible = true
                mEditText.setHintTextColor(R.color.text_light_dimmed.toAppColor())
                val ordinal = when (size) { 2 -> "3rd"; 1 -> "2nd"; else -> "1st" }
                "Enter $ordinal seed word"
            } else if (size >= 24) {
                mEditText.setHintTextColor(R.color.zcashGreen.toAppColor())
                mEditText.isCursorVisible = false
                "done"
            } else {
                mEditText.isCursorVisible = true
                mEditText.setHintTextColor(R.color.zcashYellow.toAppColor())
                "${size + 1}"
            }
        }
    }

    override fun onChipDataSourceChanged() {
        super.onChipDataSourceChanged()
        onDataSetChangedListener?.invoke()
    }

    fun onDataSetChanged(block: () -> Unit): SeedWordAdapter {
        onDataSetChangedListener = block
        return this
    }

    override fun onKeyboardActionDone(text: String?) {
        if (TextUtils.isEmpty(text)) return

        if (mDataSource.originalChips.firstOrNull { it.title == text } != null) {
            mDataSource.addSelectedChip(DefaultCustomChip(text))
            mEditText.apply {
                postDelayed(
                    {
                        setText("")
                        requestFocus()
                    },
                    50L
                )
            }
        }
    }

    // this function is called with the contents of the field, split by the delimiter
    override fun onKeyboardDelimiter(text: String) {
        val firstMatchingWord = (mDataSource.filteredChips.firstOrNull() as? SeedWordChip)?.word?.takeUnless {
            !it.startsWith(text)
        }
        if (firstMatchingWord != null) {
            onKeyboardActionDone(firstMatchingWord)
        } else {
            onKeyboardActionDone(text)
        }
    }

    private inner class SeedWordHolder(chipView: SeedWordChipView) : ChipsAdapter.ChipHolder(chipView) {
        val seedChipView = super.chipView as SeedWordChipView
    }

    private inner class SeedWordChipView(context: Context) : ChipView(context) {
        private val indexView: TextView = findViewById(R.id.chip_index)

        fun bind(chip: Chip, index: Int) {
            super.inflateFromChip(chip)
            indexView.text = (index + 1).toString()
        }
    }
}
