package com.fondova.finance.quotes

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.TextUtils.isEmpty
import android.util.AttributeSet
import android.widget.LinearLayout
import com.fondova.finance.R
import com.fondova.finance.api.model.quote.*
import com.fondova.finance.ui.util.DateFormatUtil
import com.fondova.finance.util.swipeLayout.SwipeRevealLayout
import com.fondova.finance.util.ui.dipValue

class FinanceXQuuoteItemView(context: Context, attr: AttributeSet?):
        SwipeRevealLayout(context, attr), QuoteResponseView, DeletableItemSelectable {


    private var dynamicFieldView: QuoteItemDynamicFieldView
    private var listener: DeletableCellListener? = null

    override var showActualSymbol = false

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
        dynamicFieldView.stripStaticFields = false
        var items: List<String> = listOf("Last", "Open", "High", "Low", "Settle", "Chg", "Bid", "Ask", "Volume", "Date")
        dynamicFieldView.setFields(items, width)
    }

    override fun setTitle(title: String, isExpression: Boolean) {
    }

    override fun setResponse(viewModel: QuoteWatchResponseViewModel) {
        val model = QuoteWatchResponseViewModel(updateLatestQuoteValue(viewModel.response), viewModel.quote)
        dynamicFieldView.setResponse(model)
    }


    private val MISSING = "--"
    private fun updateLatestQuoteValue(value: QuoteWatchResponse?): QuoteWatchResponse? {
        var mutableMap = value?.data?.first()?.toMutableMap() ?: mutableMapOf()
        val shortDateString = DateFormatUtil.serverDateStringToUiShortDateString(value?.settleDate)
        val date = if (isEmpty(shortDateString)) MISSING else shortDateString

        if (isEmpty(value?.open)) value?.open = MISSING
        if (isEmpty(value?.high)) value?.high = MISSING
        if (isEmpty(value?.low)) value?.low = MISSING
        if (isEmpty(value?.last)) value?.last = MISSING
        val changeString = "${value?.change ?: MISSING}(${value?.changePercentage ?: MISSING}%)"
        mutableMap["Chg"] = changeString
        mutableMap["Date"] = date
        mutableMap["Settle"] = value?.settlePrice ?: MISSING
        val volume = if (value?.volume == null) MISSING else value.volume.toString()
        mutableMap["Volume"] = volume
        if (isEmpty(value?.bid)) value?.bid = MISSING
        if (isEmpty(value?.ask)) value?.ask = MISSING
        value?.data = mutableListOf(mutableMap)

        return value
    }

}