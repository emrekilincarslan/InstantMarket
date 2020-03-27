package com.fondova.finance.quotes

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.widget.LinearLayout
import com.fondova.finance.R
import com.fondova.finance.util.swipeLayout.SwipeRevealLayout
import com.fondova.finance.util.ui.dipValue

interface QuoteResponseView {
    var showActualSymbol: Boolean

    fun setTitle(title: String, isExpression: Boolean)
    fun setResponse(viewModel: QuoteWatchResponseViewModel)
    fun setFields(fields: List<String>, width: Int)
}

interface DeletableCellListener {
    fun onSelectClicked()

    fun onDeleteClicked()
}

interface DeletableItemSelectable {
    fun setDeletableCellListener(listener: DeletableCellListener)
}

class InstantMarketQuoteItemView(context: Context, attr: AttributeSet?):
        SwipeRevealLayout(context, attr), QuoteResponseView, DeletableItemSelectable {
    constructor(context: Context): this(context, null)


    private var headerView: QuoteItemHeaderView
    private var dynamicFieldView: QuoteItemDynamicFieldView
    private var listener: DeletableCellListener? = null

    override var showActualSymbol = false
        set(value) {
            field = value
            headerView.showActualSymbol = value
        }

    init {

        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        minimumHeight = resources.dipValue(128)
        dragEdge = SwipeRevealLayout.DRAG_EDGE_RIGHT
        setMode(SwipeRevealLayout.MODE_SAME_LEVEL)

        val deleteView = DeleteView(context)
        val primaryLayout = LinearLayout(context)
        primaryLayout.orientation = LinearLayout.VERTICAL
        primaryLayout.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        primaryLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.symbol_label_background))

        headerView = QuoteItemHeaderView(context)
        primaryLayout.addView(headerView)
        dynamicFieldView = QuoteItemDynamicFieldView(context)

        primaryLayout.addView(dynamicFieldView)

        setMainView(primaryLayout)
        setSecondaryView(deleteView)

        addView(deleteView)
        addView(primaryLayout)


        primaryLayout.setOnClickListener {
            listener?.onSelectClicked()
        }

        deleteView.setOnClickListener {
            listener?.onDeleteClicked()
        }

    }

    override fun setDeletableCellListener(listener: DeletableCellListener) {
        this.listener = listener
    }

    override fun setFields(fields: List<String>, width: Int) {
        dynamicFieldView.setFields(fields, width)
    }

    override fun setTitle(title: String, isExpression: Boolean) {
        headerView.setTitle(title, isExpression)
    }

    override fun setResponse(viewModel: QuoteWatchResponseViewModel) {
        headerView.setResponse(viewModel)
        dynamicFieldView.setResponse(viewModel)
    }

}

