package com.fondova.finance.quotes.edit

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.fondova.finance.R
import com.fondova.finance.util.ui.dipValue

interface ItemSelectedListener {
    fun onItemSelected(index: Int)
}


class SelectStringDialog {

    companion object {
        fun showDialog(context: Context, itemName: String, values: List<String>, listener: ItemSelectedListener?) {
            var dialog: AlertDialog? = null
            val stringSelectionView = StringSelectionView(context, itemName, values, object : ItemSelectedListener {
                override fun onItemSelected(index: Int) {
                    listener?.onItemSelected(index)
                    dialog?.dismiss()
                }

            })
            dialog = AlertDialog.Builder(context)
                    .setView(stringSelectionView)
                    .setNegativeButton(R.string.cancel, null)
                    .setCancelable(true)
                    .create()
            dialog?.show()

            var cancelButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)

            if (cancelButton != null) {
                cancelButton.setTextColor(ContextCompat.getColor(context, R.color.red))
            }


        }
    }

}

class StringSelectionView(context: Context, itemName: String, values: List<String>, listener: ItemSelectedListener?, attr: AttributeSet?): LinearLayout(context, attr) {
    constructor(context: Context, itemName: String, values: List<String>, listener: ItemSelectedListener?): this(context, itemName, values, listener,null)
    constructor(context: Context, attr: AttributeSet?): this(context, "", listOf(), null, attr)
    constructor(context: Context): this(context, null)

    var listView: ListView
    var listAdapter: ListAdapter

    init {

        orientation = VERTICAL

        val padding = context.resources.dipValue(15)

        val titleView = TextView(context)
        titleView.setPadding(padding, padding, padding, padding)
        titleView.maxLines = 3
        titleView.gravity = Gravity.CENTER
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.toFloat())
        titleView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        titleView.text = context.resources.getString(R.string.add_to_group_title, itemName)
        addView(titleView)

        addView(createDivider())

        listView = ListView(context)
        listView.dividerHeight = 0
        listView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(listView)

        addView(createDivider())

        listAdapter = StringSelectionAdapter(context, values, listener)
        listView.adapter = listAdapter
    }

    private fun createDivider(): View {
        val divider = View(context)
        divider.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, context.resources.dipValue(1))
        divider.setBackgroundColor(ContextCompat.getColor(context, R.color.grey_8a))
        return divider
    }
}

class StringSelectionAdapter(val context: Context, val values: List<String>, val listener: ItemSelectedListener?): BaseAdapter() {

    override fun getView(index: Int, recycledView: View?, parent: ViewGroup?): View {
        val textView = TextView(context)
        textView.setTextColor(ContextCompat.getColor(context, R.color.green))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18.toFloat())
        textView.text = values.get(index)
        textView.gravity = Gravity.CENTER
        val padding = context.resources.dipValue(10)
        textView.setPadding(padding, padding, padding, padding)
        textView.setOnClickListener {
            listener?.onItemSelected(index)
        }
        return textView
    }

    override fun getItem(index: Int): Any {
        return values.get(index)
    }

    override fun getItemId(index: Int): Long {
        return index.toLong()
    }

    override fun getCount(): Int {
        return values.size
    }



}