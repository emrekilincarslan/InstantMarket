package com.fondova.finance.charts

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.widget.Toolbar
import com.fondova.finance.R
import com.fondova.finance.util.ui.dipValue

class ChartHeaderView(context: Context, attr: AttributeSet?): Toolbar(context, attr) {
    constructor(context: Context): this(context, null)

    init {
        setBackgroundColor(ContextCompat.getColor(context, R.color.chart_header_color))

        minimumHeight = resources.dipValue(70)

        setTitleTextAppearance(context, R.style.ToolbarTextAppearanceTitle)
        setSubtitleTextAppearance(context, R.style.ToolbarTextAppearanceSubtitle)

    }

}
