package com.fondova.finance.quotes

import android.app.AlertDialog
import android.arch.lifecycle.LifecycleOwner
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.support.v4.content.res.ResourcesCompat
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import android.widget.LinearLayout
import android.widget.TextView
import com.fondova.finance.App
import com.fondova.finance.R
import com.fondova.finance.api.session.SessionService
import com.fondova.finance.api.session.SessionStatus
import com.fondova.finance.config.AppConfig
import com.fondova.finance.workspace.Workspace
import com.fondova.finance.workspace.WorkspaceFactory
import com.fondova.finance.persistance.AppStorage
import com.fondova.finance.repo.QuoteWatchRepository
import com.fondova.finance.ui.OnScrollObserver
import com.fondova.finance.ui.util.DialogUtil
import com.fondova.finance.util.ui.dipValue
import javax.inject.Inject
import kotlin.math.abs

interface QuoteListViewListener: OnQuoteClickListener, WorkspaceSelectorListener

interface WorkspaceSelectorListener {
    fun onWorkspaceSelected(workspaceId: String)
}

class QuoteListView(context: Context, attr: AttributeSet?):
        LinearLayout(context, attr) {
    constructor(context: Context): this(context, null)

    private var listView: ExpandableListView
    private var workspaceSelector: WorkspaceSelector? = null
    private var listener: QuoteListViewListener? = null
    private var adapter: GroupedQuoteListAdapter? = null
    private var scrollListener: OnScrollObserver? = null
    private var lastScrollPostion: Float = 0.0F
    private val scrollThreshold: Float = 100F
    private var scrollListenerNotified: Boolean = false

    @Inject
    lateinit var appConfig: AppConfig
    @Inject
    lateinit var appStorage: AppStorage


    init {
        App.getAppComponent().inject(this)
        orientation = LinearLayout.VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        if (appConfig.useMultipleWorkspaces()) {
            workspaceSelector = WorkspaceSelector(context)

            addView(workspaceSelector)

        }
        listView = ExpandableListView(context)
        listView.setChildDivider(context.getDrawable(R.drawable.divider))
        listView.setBackgroundColor(Color.BLACK)
        listView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        isClickable = true
        addView(listView)

    }

    fun setOnScrollListener(listener: OnScrollObserver) {
        scrollListener = listener
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            lastScrollPostion = ev.y
            scrollListenerNotified = false
        }
        if (!scrollListenerNotified) {
            if (ev?.action == MotionEvent.ACTION_MOVE && abs(lastScrollPostion - ev.y) > scrollThreshold) {
                if (lastScrollPostion > ev.y) {
                    scrollListener?.onScrollDown()
                } else {
                    scrollListener?.onScrollUp()
                }
                scrollListenerNotified = true
            }
        }
        return listView.dispatchTouchEvent(ev)
    }

    fun listen(lifecycleOwner: LifecycleOwner, listener: QuoteListViewListener, repository: QuoteWatchRepository) {
        this.listener = listener
        adapter = GroupedQuoteListAdapter(lifecycleOwner, listView, context, WorkspaceFactory().emptyWorkspace(), repository, appConfig)
        listView.setAdapter(adapter)
        adapter?.onQuoteClickListener = object : OnQuoteClickListener {
            override fun onQuoteClick(groupIndex: Int, quoteIndex: Int) {
                listener.onQuoteClick(groupIndex, quoteIndex)
            }

            override fun onDeleteClicked(groupIndex: Int, quoteIndex: Int?) {
                listener.onDeleteClicked(groupIndex, quoteIndex)
            }
        }
        workspaceSelector?.listener = listener

    }

    fun updateExpandedState() {
        val workspace = adapter?.workspace
        val workspaceId = workspace?.getWorkspaceId() ?: return

        val expandedStates = appStorage.getWorkspaceExpandedState(workspaceId)

        for (index in expandedStates.indices) {
            if (expandedStates[index]) {
                listView.expandGroup(index, false)
            } else {
                listView.collapseGroup(index)
            }
        }
    }

    fun saveExpandedState() {
        val workspace = adapter?.workspace
        val workspaceId = workspace?.getWorkspaceId() ?: return

        val expandedStates = workspace.getGroups().map { false }.toMutableList()

        for (index in expandedStates.indices) {
            if (listView.isGroupExpanded(index)) {
                expandedStates[index] = true
            }
        }
        appStorage.setWorkspaceExpandedState(workspaceId, expandedStates)
    }

    fun setWorkspace(workspace: Workspace) {
        Log.d("QuoteListView", "Setting workspace: ${workspace.getWorkspaceId()}, reloading quote list")
        saveExpandedState()
        adapter?.workspace = workspace
        workspaceSelector?.workspace = workspace
        adapter?.notifyDataSetChanged()
        updateExpandedState()
        listView.setSelectedGroup(0)

    }

    fun setWorkspaceList(list: List<Workspace>) {
        workspaceSelector?.setWorkspaceList(list)
    }

}

class WorkspaceSelector(context: Context):
        LinearLayout(context) {

    var titleTextView: TextView
    private var caretView: View
    var listener: WorkspaceSelectorListener? = null
    private var workspaces: List<Workspace> = emptyList()
    var workspace: Workspace? = null
    set(newValue) {
        field = newValue
        titleTextView.text = newValue?.getName()
    }

    @Inject
    lateinit var sessionService: SessionService
    @Inject
    lateinit var dialogUtil: DialogUtil

    init {
        App.getAppComponent().inject(this)
        orientation = HORIZONTAL
        setPadding(42, 0, 10, 0)

        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, resources.dipValue(50))
        setBackgroundColor(Color.BLACK)

        titleTextView = TextView(context)
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.toFloat())
        gravity = Gravity.CENTER_VERTICAL
        titleTextView.setTextColor(Color.WHITE)
        titleTextView.setBackgroundColor(Color.BLACK)
        titleTextView.text = "Workspace"
        titleTextView.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)


        addView(titleTextView)
        caretView = View(context)
        caretView.layoutParams = LayoutParams(resources.dipValue(50), resources.dipValue(50))

        caretView.background = ResourcesCompat.getDrawable(context.resources, R.drawable.abc_spinner_mtrl_am_alpha, null)
        addView(caretView)

        this.setOnClickListener {
            if (sessionService.getSessionStatusLiveData().value != SessionStatus.connected) {
                dialogUtil.showErrorDialog(context, R.string.not_connected_to_server_message)
                return@setOnClickListener
            }
            showWorkspacePicker()
        }
    }

    fun showWorkspacePicker() {
        if (!hasMultipleWorkspaces()) {
            return
        }
        val workspaceNames = workspaces.map { it.getName() ?: "Null" }.toTypedArray()
        var selectedWorkspaceIndex = workspaces.indexOfFirst { it.getWorkspaceId() == workspace?.getWorkspaceId() }
        AlertDialog.Builder(context).setSingleChoiceItems(workspaceNames, selectedWorkspaceIndex, object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, indexSelected: Int) {
                listener?.onWorkspaceSelected(workspaces.get(indexSelected).getWorkspaceId()!!)
                dialog?.dismiss()
            }

        }).show()
    }

    fun setWorkspaceList(list: List<Workspace>) {
        workspaces = list
        if (hasMultipleWorkspaces()) {
            caretView.visibility = View.VISIBLE
        } else {
            caretView.visibility = View.GONE
        }
    }

    private fun hasMultipleWorkspaces(): Boolean {
        return workspaces.size > 1
    }


}
