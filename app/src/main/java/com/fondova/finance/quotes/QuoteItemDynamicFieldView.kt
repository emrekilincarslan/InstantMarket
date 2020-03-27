package com.fondova.finance.quotes

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.fondova.finance.util.ui.fromCamelCaseToSpaces

class QuoteItemDynamicFieldView(context: Context, attr: AttributeSet?): LinearLayout(context, attr), QuoteResponseView {

    constructor(context: Context): this(context, null)

    private var _fields: List<String> = emptyList()
    private var valueTextViewMap: Map<String, TextView> = mapOf()

    override var showActualSymbol: Boolean = true

    var stripStaticFields = true

    init {
        orientation = VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

    }

    override fun setResponse(viewModel: QuoteWatchResponseViewModel) {
        //val data = response?.data?.first() ?: emptyMap<String, Any>()
        for (field in _fields) {
            val textView = valueTextViewMap[field]
            textView?.text = viewModel.valueForField(field)
        }

    }

    private fun getColumnWidth(width: Int): Int {
        return width / 2
    }

    override fun setFields(fields: List<String>, width: Int) {
        if (_fields != fields) {
            _fields = fields
            if (stripStaticFields) {
                createViews(fieldsWithoutStaticFields(), width)
            } else {
                createViews(_fields, width)
            }
        }
    }

    override fun setTitle(title: String, isExpression: Boolean) {
        // No title displayed in this view
    }

    fun createViews(fields: List<String>, width: Int) {
        removeAllViews()
        val map: MutableMap<String, TextView> = mutableMapOf()
        var columnCount = width / getColumnWidth(width)
        var columns: MutableList<LinearLayout> = mutableListOf()
        var rowCount = Math.ceil(fields.size.toDouble() / columnCount.toDouble()).toInt()

        for (columnIndex in 1..rowCount) {
            val column = LinearLayout(context)
            column.orientation = HORIZONTAL
            column.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            column.setPadding(0, 5, 0, 5)
            columns.add(column)
            addView(column)
        }

        for (index in fields.indices) {
            val column = index % columns.count()
            val field = fields[index]
            val labelTextView = createLabelView("${field.fromCamelCaseToSpaces()}: ")
            val valueTextField = createValueView("--")
            val linearLayout = LinearLayout(context)
            val horizontalPadding = 20
            linearLayout.layoutParams = LayoutParams(getColumnWidth(width) - (horizontalPadding * 2), ViewGroup.LayoutParams.WRAP_CONTENT)
            linearLayout.setPadding(horizontalPadding, 0, horizontalPadding, 0)
            linearLayout.orientation = HORIZONTAL
            linearLayout.addView(labelTextView)
            linearLayout.addView(valueTextField)
            map.set(field, valueTextField)
            columns[column].addView(linearLayout)
        }


        valueTextViewMap = map

    }

    fun createValueView(field: String): TextView {
        val valueTextField = TextView(context)
        valueTextField.setTextColor(Color.WHITE)
        valueTextField.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        valueTextField.text = field
        valueTextField.gravity = Gravity.RIGHT
        valueTextField.alpha = 0.7F
        return valueTextField
    }

    fun createLabelView(field: String): TextView {
        val valueTextField = TextView(context)
        valueTextField.setTextColor(Color.WHITE)
        valueTextField.layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        valueTextField.text = field
        return valueTextField
    }


    fun fieldsWithoutStaticFields(): List<String> {
        val staticFields = listOf("Last", "Change", "IssueDescription")
        return _fields.filter { !staticFields.contains(it) }
    }

}