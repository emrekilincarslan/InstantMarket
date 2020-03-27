package com.fondova.finance.quotes.edit

import android.content.Context
import com.fondova.finance.R

interface AddExpressionListener {
    fun onAddExpressionTapped(expression: String, name: String)
}

interface ValidationListener {
    fun onValidationChanged(isValid: Boolean)
}

class AddExpressionDialog {

    companion object {
        fun showDialog(context: Context,
                       quoteListCount: Int,
                       quoteListLimit: Int,
                       showNameField: Boolean = true,
                       listener: AddExpressionListener) {
            val title = context.resources.getText(R.string.enter_expression_hint).toString()
            val subtitle = context.resources.getString(R.string.symbols_count, quoteListCount, quoteListLimit).toString()
            val message = context.resources.getText(R.string.enter_expression_message).toString()
            val expressionHint = context.resources.getString(R.string.enter_expression_hint).toString()
            val expressionNameHint = context.resources.getString(R.string.enter_expression_name_hint).toString()
            AddItemDialogView.showDialog(context,
                    title,
                    subtitle,
                    message,
                    expressionHint,
                    if (showNameField) expressionNameHint else null,
                    object : AddItemListener {
                        override fun onAddItemTapped(values: List<String>) {
                            listener.onAddExpressionTapped(values.firstOrNull() ?: "--", values.lastOrNull() ?: "--")
                        }
                    })
        }
    }

}