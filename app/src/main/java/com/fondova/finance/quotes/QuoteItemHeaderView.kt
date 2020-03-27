package com.fondova.finance.quotes

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.fondova.finance.util.ui.dipValue

class QuoteItemHeaderView(context: Context, attr: AttributeSet?): RelativeLayout(context, attr), QuoteResponseView {
    constructor(context: Context): this(context, null)

    private var titleTextView: TextView
    private var lastTextView: TextView
    private var changePercentageTextView: TextView
    private var isExpression: Boolean = false

    override var showActualSymbol = false

    init {

        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        setPadding(10, 10, 10, 10)

        changePercentageTextView = TextView(context)
        changePercentageTextView.setTextColor(Color.WHITE)
        changePercentageTextView.id = View.generateViewId()
        changePercentageTextView.textSize = 14.toFloat()
        changePercentageTextView.gravity = Gravity.CENTER

        val changePercentageTextViewParams = LayoutParams(resources.dipValue(100), resources.dipValue(40))
        changePercentageTextViewParams.setMargins(10, 10, 10, 10)
        changePercentageTextViewParams.addRule(ALIGN_PARENT_RIGHT)
        changePercentageTextViewParams.addRule(CENTER_VERTICAL)
        changePercentageTextView.layoutParams = changePercentageTextViewParams

        lastTextView = TextView(context)
        lastTextView.setTextColor(Color.WHITE)
        lastTextView.id = View.generateViewId()
        lastTextView.setPadding(10, 0, 10, 0)
        lastTextView.textSize = 18.toFloat()

        val lastTextViewParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        lastTextViewParams.setMargins(10, 0, 10, 0)
        lastTextViewParams.addRule(LEFT_OF, changePercentageTextView.id)
        lastTextViewParams.addRule(CENTER_VERTICAL)

        titleTextView = TextView(context)
        titleTextView.setTextColor(Color.WHITE)
        titleTextView.maxLines = 3
        titleTextView.minLines = 1
        titleTextView.setSingleLine(false)

        val titleTextViewParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        titleTextViewParams.setMargins(10, 0, 80, 0)
        titleTextViewParams.addRule(ALIGN_PARENT_LEFT)
        titleTextViewParams.addRule(LEFT_OF, lastTextView.id)
        titleTextViewParams.addRule(CENTER_VERTICAL)

        addView(titleTextView, titleTextViewParams)
        addView(lastTextView, lastTextViewParams)
        addView(changePercentageTextView, changePercentageTextViewParams)

    }

    override fun setResponse(viewModel: QuoteWatchResponseViewModel) {
        changePercentageTextView.setBackgroundResource(viewModel.getChangeBackgroundResource())
        changePercentageTextView.text = viewModel.getChangeText()
        lastTextView.text = viewModel.getLastText()
        if (showActualSymbol) {
            titleTextView.text = viewModel.getActualSymbol()
        } else {
            titleTextView.text = viewModel.getTitleText() ?: titleTextView.text
        }
    }

    override fun setTitle(title: String, isExpression: Boolean) {
        titleTextView.text = title
        this.isExpression = isExpression
    }

    override fun setFields(fields: List<String>, width: Int) {
        // Don't care
    }


}