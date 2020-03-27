package com.fondova.finance.quotes.edit

import android.content.Context
import com.fondova.finance.R

interface AddGroupListener {
    fun onAddGroupTapped(name: String)
}

class AddGroupDialog {

    companion object {
        fun showDialog(context: Context,
                       quoteListCount: Int,
                       quoteListLimit: Int,
                       listener: AddGroupListener) {
            val title = context.resources.getText(R.string.add_a_label).toString()
            val subtitle = context.resources.getString(R.string.symbols_count, quoteListCount, quoteListLimit).toString()
            val message = context.resources.getText(R.string.enter_a_label_name_to_add_to_your_symbols_page).toString()
            val expressionHint = context.resources.getString(R.string.enter_label_name).toString()
            AddItemDialogView.showDialog(context,
                    title,
                    subtitle,
                    message,
                    expressionHint,
                    null,
                    object : AddItemListener {
                        override fun onAddItemTapped(values: List<String>) {
                            listener.onAddGroupTapped(values.firstOrNull() ?: "--")
                        }
                    })
        }
    }

}