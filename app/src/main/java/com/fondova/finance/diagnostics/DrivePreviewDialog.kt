package com.fondova.finance.diagnostics

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.fondova.finance.R
import com.fondova.finance.util.ui.dipValue

class DrivePreviewDialog(context: Context, val data: List<DriveItem>, val confirmationListener: () -> Unit): Dialog(context) {

    val preferredWidth = 300
    val titlePadding = 5
    val listHeight = 400

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val buttonLayout = LinearLayout(context)
        buttonLayout.orientation = LinearLayout.HORIZONTAL
        buttonLayout.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val listView = ListView(context)
        listView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, context.resources.dipValue(listHeight))
        val padding = context.resources.dipValue(titlePadding)
        listView.setPadding(padding, padding, padding, padding)
        listView.adapter = DriveListAdapter(context, data)

        layout.addView(createTitleLabel(context))
        layout.addView(listView)
        layout.addView(buttonLayout)
        buttonLayout.addView(createCancelButton(context))
        buttonLayout.addView(createYesButton(context))
        setContentView(layout)
    }

    fun createTitleLabel(context: Context): TextView {
        val textView = TextView(context)
        textView.setTextColor(context.resources.getColor(R.color.black))
        val padding = context.resources.dipValue(titlePadding)
        textView.setPadding(padding, padding, padding, padding)
        textView.setText(R.string.replace_data_message)
        textView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        return textView
    }

    fun createYesButton(context: Context): Button {
        return createButton(context, R.string.ok) {
            confirmationListener()
            dismiss()
        }
    }

    fun createCancelButton(context: Context): Button {
        return createButton(context, R.string.cancel)
    }

    fun createButton(context: Context, text: Int, listener: ((DrivePreviewDialog) -> Unit)? = null): Button {
        val button = Button(context)
        button.width = context.resources.dipValue(preferredWidth / 2)
        button.setText(text)
        button.setOnClickListener {
            if (listener != null) {
                listener(this)
            } else {
                dismiss()
            }
        }
        return button
    }
}

data class DriveItem(val name: String, val isSection: Boolean)

class DriveListAdapter(context: Context, var items: List<DriveItem>): ArrayAdapter<DriveItem>(context, android.R.layout.simple_list_item_1, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView as? TextView
        if (view == null) {
            view = TextView(context)
        }
        if (items[position].isSection) {
            view.setBackgroundColor(context.resources.getColor(R.color.grey_cf))
        } else {
            view.setBackgroundColor(context.resources.getColor(R.color.white))
        }
        view.setTextColor(context.resources.getColor(R.color.black))
        view.text = items[position].name
        return view
    }
}