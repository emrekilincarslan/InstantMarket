package com.fondova.finance.quotes

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.fondova.finance.R
import com.fondova.finance.util.ui.dipValue

interface GroupHeaderViewCollapsedListener {
    fun onCollapse()
    fun onExpand()
}

class GroupHeaderView(context: Context, attr: AttributeSet?): RelativeLayout(context, attr) {
    constructor(context: Context): this(context, null)

    private val textView: TextView
    private val imageView: ImageView
    private var listener: GroupHeaderViewCollapsedListener? = null

    var isExpanded = true
        set(value) {
            if (value != field) {
                field = value
                updateCollapsedState(value)
            }
        }

    init {

        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, context.resources.getDimension(R.dimen.quote_label_row_height).toInt())
        setBackgroundColor(ContextCompat.getColor(context, R.color.symbol_label_background))

        textView = TextView(context)
        textView.setTextColor(Color.WHITE)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.toFloat())
        textView.setPadding(16, 8, 8, 8)
        textView.gravity = Gravity.CENTER_VERTICAL

        imageView = ImageView(context)
        imageView.id = View.generateViewId()
        imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.down_caret))

        setOnClickListener {
            isExpanded = !isExpanded
            notifyListener(isExpanded)
        }

        val imageViewParams = LayoutParams(resources.dipValue(50), resources.dipValue(50))
        imageViewParams.setMargins(10, 10, 0, 10)
        imageViewParams.addRule(ALIGN_PARENT_RIGHT)
        imageViewParams.addRule(CENTER_VERTICAL)

        val textViewParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        textViewParams.setMargins(10, 10, 10, 10)
        textViewParams.addRule(ALIGN_PARENT_LEFT)
        textViewParams.addRule(LEFT_OF, imageView.id)
        textViewParams.addRule(CENTER_VERTICAL)

        addView(textView, textViewParams)
        addView(imageView, imageViewParams)
    }

    fun updateImageView(expanded: Boolean) {
        var rotation = 270.toFloat()
        if (expanded) {
            rotation = 0.toFloat()
        }
        imageView.rotation = rotation
    }

    fun setGroupHeaderViewCollapsedListener(listener: GroupHeaderViewCollapsedListener) {
        this.listener = listener
    }

    fun setTitle(title: String) {
        textView.text = title
    }

    private fun notifyListener(expanded: Boolean) {
        if (expanded) {
            listener?.onExpand()
        } else {
            listener?.onCollapse()
        }
    }

    private fun updateCollapsedState(expanded: Boolean) {
        updateImageView(expanded)
    }
}