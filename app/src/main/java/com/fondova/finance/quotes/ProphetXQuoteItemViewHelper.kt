package com.fondova.finance.quotes

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fondova.finance.R
import com.fondova.finance.api.model.quote.*
import com.fondova.finance.repo.QuoteWatchRepository
import com.fondova.finance.ui.util.DateFormatUtil
import com.fondova.finance.util.swipeLayout.SwipeRevealLayout
import com.fondova.finance.util.swipeLayout.ViewBinderHelper
import com.fondova.finance.workspace.Workspace
import com.fondova.finance.workspace.WorkspaceQuote
import kotlinx.android.synthetic.main.adapter_symbol.view.*
import kotlinx.android.synthetic.main.include_quote_delete_reveal.view.*
import java.util.*

class FinanceXQuoteItemViewHelper(val context: Context,
                                  val workspace: Workspace,
                                  val lifecycleOwner: LifecycleOwner,
                                  val quoteWatchRepository: QuoteWatchRepository,
                                  val onQuoteClickListener: OnQuoteClickListener?) {
    fun populateQuoteView(convertView: View?, parent: ViewGroup?, groupIndex: Int, quoteIndex: Int): View {
        val layoutInflater = LayoutInflater.from(context)
        var view = convertView
        if (view == null) {
            view = layoutInflater.inflate(R.layout.adapter_symbol, parent, false)
        }

        var viewHolder = view?.tag as? SymbolViewHolder
        if (viewHolder == null) {
            viewHolder = SymbolViewHolder(view!!, context)
            view.tag = viewHolder
        }


        val groups = workspace.getGroups()
        val listOfQuotes = groups.getOrNull(groupIndex)?.getListOfQuotes()
        var symbol = listOfQuotes?.getOrNull(quoteIndex)

        viewHolder.setSymbol(lifecycleOwner, symbol, quoteWatchRepository)
        view?.root?.setOnClickListener {
            onQuoteClickListener?.onQuoteClick(groupIndex, quoteIndex)
        }
        view?.root?.setOnLongClickListener {
            // TODO: Implement highlight on Long Click
            false
        }
        view?.fl_delete?.setOnClickListener {
            onQuoteClickListener?.onDeleteClicked(groupIndex, quoteIndex)
            viewHolder.viewBinderHelper.closeLayout(symbol?.getValue() ?: "")
        }
        return view!!
    }

}

class SymbolViewHolder(val view: View, val context: Context) {
    var liveData: LiveData<QuoteWatchResponse>? = null
    var viewBinderHelper: ViewBinderHelper = ViewBinderHelper()

    fun setSymbol(lifecycleOwner: LifecycleOwner,
                  quote: WorkspaceQuote?,
                  quoteWatchRepository: QuoteWatchRepository) {
        val swipeView = view.findViewById<SwipeRevealLayout>(R.id.srl_row)
        viewBinderHelper.bind(swipeView, quote?.getValue() ?: "")
        liveData?.removeObservers(lifecycleOwner)
        val symbol = quote?.getValue()
        if (symbol == null) {
            return
        }
        liveData = quoteWatchRepository.subscribe(symbol)
        liveData?.observe(lifecycleOwner, Observer { quoteAndValue ->
            populateData(quote, quoteAndValue)
        })
    }

    fun populateData(quote: WorkspaceQuote?, value: QuoteWatchResponse?) {
        populateQuoteName(quote, value)
        view.tv_open_price.text = context.getString(R.string.open_price_label,
                value?.open ?: "---")
        view.tv_high.text = context.getString(R.string.high_label,
                value?.high ?: "---")
        view.tv_low.text = context.getString(R.string.low_label,
                value?.low ?: "---")
        view.tv_bid.text = context.getString(R.string.bid_label,
                value?.bid ?: "---")
        view.tv_ask.text = context.getString(R.string.ask_label,
                value?.ask ?: "---")
        view.tv_volume.text = context.getString(R.string.volume_label,
                value?.volume ?: "---")
        view.tv_settle.text = context.getString(R.string.settle_label,
                value?.settlePrice ?: "---")
        view.tv_current_price.text = value?.last ?: "---"
        val pctChange = value?.changePercentage ?: "---"

        if (isSettleMissing(value?.settleDate)) {
            view.tv_settle.visibility = View.GONE
            view.tv_date_settled.visibility = View.GONE
        } else {
            view.tv_settle.visibility = View.VISIBLE
            view.tv_date_settled.visibility = View.VISIBLE
            try {
                val parse = DateFormatUtil.serverDateStringToDateTime(value?.settleDate)
                view.tv_date_settled.text = context.getString(R.string.date_settled_label, DateFormatUtil.dateTimeToShortDateString(parse))
            } catch (e: IllegalArgumentException) {
                view.tv_date_settled.text = context.getString(R.string.date_settled_label, value?.settleDate)
            }
        }

        if (value?.change != null) {
            view.tv_change.text = String.format(Locale.US, "%s\n(%s%%)", value.change, pctChange)
        } else {
            view.tv_change.text = "---"
        }

        if (TextUtils.isEmpty(value?.changePercentage)) {
            view.tv_change.setBackgroundResource(R.drawable.selector_black_white_border)
        } else {
            val changeValue: Double = value?.changePercentage?.toDoubleOrNull() ?: 0.toDouble()
            if (changeValue == 0.toDouble()) {
                view.tv_change.setBackgroundResource(R.drawable.selector_black_white_border)
            } else if (changeValue > 0.toDouble()) {
                view.tv_change.setBackgroundResource(R.drawable.current_price_green_background)
            } else {
                view.tv_change.setBackgroundResource(R.drawable.current_price_red_background)
            }
        }
    }

    private fun isSettleMissing(settleDate: String?): Boolean {
        return (settleDate == null || settleDate.equals("null", ignoreCase = true)
                || settleDate.equals("---", ignoreCase = true))
    }


    fun populateQuoteName(quote: WorkspaceQuote?, value: QuoteWatchResponse?) {
        val viewModel = QuoteWatchResponseViewModel(value, quote)
        var quoteName = value?.actualSymbol
        if (quoteName == null || value?.errorValue() != null) {
            quoteName = quote?.getValue()
        }

        view.tv_symbol.text = String.format(Locale.US, "%s\n%s", viewModel.getTitleText(false), viewModel.getActualSymbol(true))
    }

}