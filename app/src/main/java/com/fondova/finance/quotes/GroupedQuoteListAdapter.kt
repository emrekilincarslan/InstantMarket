package com.fondova.finance.quotes

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import com.fondova.finance.config.AppConfig
import com.fondova.finance.repo.QuoteWatchRepository
import com.fondova.finance.workspace.Workspace
import com.fondova.finance.workspace.WorkspaceFactory
import com.fondova.finance.workspace.WorkspaceQuoteType

class GroupedQuoteListAdapter(val lifecycleOwner: LifecycleOwner,
                              val expandableListView: ExpandableListView,
                              val context: Context,
                              var workspace: Workspace,
                              var quoteWatchRepository: QuoteWatchRepository,
                              var appConfig: AppConfig): BaseExpandableListAdapter() {

    var onQuoteClickListener: OnQuoteClickListener? = null

    override fun getGroup(groupIndex: Int): Any {
        return workspace.getGroups().get(groupIndex)
    }

    override fun isChildSelectable(groupIndex: Int, quoteIndex: Int): Boolean {
        return true
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(groupIndex: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        var recycledView = convertView as? GroupHeaderView

        if (recycledView == null) {
            recycledView = GroupHeaderView(context)
        }

        val group = workspace.getGroups().get(groupIndex)
        recycledView.setGroupHeaderViewCollapsedListener( object: GroupHeaderViewCollapsedListener {
            override fun onCollapse() {
                expandableListView.collapseGroup(groupIndex)            }

            override fun onExpand() {
                expandableListView.expandGroup(groupIndex, false)
            }

        })

        recycledView.isExpanded = expandableListView.isGroupExpanded((groupIndex))

        recycledView.setTitle(group.getDisplayName() ?: "--")
        return recycledView
    }

    override fun getChildrenCount(groupIndex: Int): Int {
        val groups = workspace.getGroups()
        if (groupIndex >= groups.size) {
            return 0
        }
        return groups[groupIndex].getListOfQuotes().size
    }

    override fun getChild(groupIndex: Int, quoteIndex: Int): Any {
        val blankQuote = WorkspaceFactory().emptyQuote()
        return workspace.getGroups().get(groupIndex).getListOfQuotes().get(quoteIndex)
    }

    override fun getGroupId(groupIndex: Int): Long {
        return groupIndex.toLong()
    }

    override fun getChildView(groupIndex: Int, quoteIndex: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        if (appConfig.useStockSettings()) {
            return createInstantMarketQuoteView(groupIndex, quoteIndex, parent)
        }

        val helper = FinanceXQuoteItemViewHelper(context, workspace, lifecycleOwner, quoteWatchRepository, onQuoteClickListener)
        return helper.populateQuoteView(convertView, parent, groupIndex, quoteIndex)
    }

    fun createInstantMarketQuoteView(groupIndex: Int, quoteIndex: Int, parent: ViewGroup?): InstantMarketQuoteItemView {
        val groups = workspace.getGroups()
        val listOfQuotes = groups.getOrNull(groupIndex)?.getListOfQuotes()
        var quote = listOfQuotes?.getOrNull(quoteIndex)
        var symbol = quote?.getValue()

        val view = InstantMarketQuoteItemView(context)
        view.setTitle(quote?.getDisplayName() ?: "--", quote?.getType()?.toLowerCase() == WorkspaceQuoteType.EXPRESSION)
        view.setDeletableCellListener( object: DeletableCellListener {
            override fun onSelectClicked() {
                onQuoteClickListener?.onQuoteClick(groupIndex, quoteIndex)
            }

            override fun onDeleteClicked() {
                onQuoteClickListener?.onDeleteClicked(groupIndex, quoteIndex)
            }

        })

        if (symbol == null) {
            return view
        }

        view.setFields(workspace.getExpandedFields(), parent?.measuredWidth
                ?: 0)
        quoteWatchRepository.subscribe(symbol).observe(lifecycleOwner, Observer {
            view.setResponse(QuoteWatchResponseViewModel(it, quote))
        })
        return view
    }

    override fun getChildId(groupIndex: Int, quoteIndex: Int): Long {
        return quoteIndex.toLong()
    }

    override fun getGroupCount(): Int {
        return workspace.getGroups().size
    }

}

interface OnQuoteClickListener {
    fun onQuoteClick(groupIndex: Int, quoteIndex: Int)

    fun onDeleteClicked(groupIndex: Int, quoteIndex: Int?)
}
