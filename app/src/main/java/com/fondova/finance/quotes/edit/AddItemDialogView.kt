package com.fondova.finance.quotes.edit

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.fondova.finance.R
import com.fondova.finance.util.ui.dipValue

interface AddItemListener {
    fun onAddItemTapped(values: List<String>)
}


class AddItemDialogView(context: Context,
                            attr: AttributeSet?,
                            title: String?,
                            subTitle: String,
                            detailMessage: String,
                            firstFieldHint: String?,
                            secondFieldHint: String?,
                            validationListener: ValidationListener?): ConstraintLayout(context, attr) {
        constructor(context: Context, attr: AttributeSet?):
                this(context, attr,
                        "Title",
                        "Subtitle",
                        "Message",
                        "Field 1",
                        "Field 2",
                        null)
        constructor(context: Context,
                    title: String?,
                    subTitle: String,
                    detailMessage: String,
                    firstFieldHint: String?,
                    secondFieldHint: String?,
                    validationListener: ValidationListener?):
                this(context,
                        null,
                        title,
                        subTitle,
                        detailMessage,
                        firstFieldHint,
                        secondFieldHint,
                        validationListener)
        constructor(context: Context):
                this(context, null)

        val titleTextView: TextView
        val quoteCountTextView: TextView
        val messageTextView: TextView
        val expressionEditText: EditText
        val nameEditText: EditText

        companion object {
            fun showDialog(context: Context,
                           title: String,
                           subTitle: String,
                           detailMessage: String,
                           firstFieldHint: String,
                           secondFieldHint: String?,
                           listener: AddItemListener) {
                val disabledAlpha = 0.3.toFloat()
                var addButton: Button? = null
                val addExpressionView = AddItemDialogView(context,
                        title,
                        subTitle,
                        detailMessage,
                        firstFieldHint,
                        secondFieldHint,
                        object: ValidationListener {
                    override fun onValidationChanged(isValid: Boolean) {
                        addButton?.isEnabled = isValid
                        addButton?.alpha = if (isValid) 1.toFloat() else disabledAlpha
                    }
                })
                val dialog = AlertDialog.Builder(context)
                        .setView(addExpressionView)
                        .setPositiveButton(R.string.add, null)
                        .setNegativeButton(R.string.cancel, null)
                        .setCancelable(true)
                        .create()
                dialog.show()
                addButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                var cancelButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)

                if (addButton != null) {
                    addButton.setTextColor(ContextCompat.getColor(context, R.color.blue))
                    addButton.alpha = disabledAlpha
                    addButton.isEnabled = false
                    addButton.setOnClickListener { v ->
                        val expression = addExpressionView.getExpression()
                        val name = addExpressionView.getName()
                        val values: MutableList<String> = mutableListOf()
                        values.add(expression)
                        values.add(name)
                        listener.onAddItemTapped(values)
                        dialog.dismiss()
                    }
                }

                if (cancelButton != null) {
                    cancelButton.setTextColor(ContextCompat.getColor(context, R.color.blue))
                }
            }
        }

        init {

            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            val padding = resources.getDimension(R.dimen.padding_large).toInt()
            setPadding(padding, padding, padding, padding)

            titleTextView = TextView(context)
            titleTextView.id = View.generateViewId()
            titleTextView.text = title
            titleTextView.setTextColor(Color.parseColor("#DE000000"))
            titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18.toFloat())
            val titleTextViewLayoutParams = ConstraintLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

            addView(titleTextView, titleTextViewLayoutParams)

            quoteCountTextView = TextView(context)
            quoteCountTextView.id = View.generateViewId()
            quoteCountTextView.text = subTitle
            quoteCountTextView.setTextColor(Color.parseColor("#61000000"))
            quoteCountTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.toFloat())
            quoteCountTextView.gravity = Gravity.RIGHT

            val quoteCountTextViewLayoutParams = ConstraintLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT)
            quoteCountTextViewLayoutParams.baselineToBaseline = titleTextView.id
            quoteCountTextViewLayoutParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
            quoteCountTextViewLayoutParams.leftToRight = titleTextView.id
            quoteCountTextViewLayoutParams.setMargins(resources.dipValue(8), 0, 0, 0)
            quoteCountTextViewLayoutParams.horizontalBias = 0.413.toFloat()

            addView(quoteCountTextView, quoteCountTextViewLayoutParams)

            messageTextView = TextView(context)
            messageTextView.id = View.generateViewId()
            messageTextView.text = detailMessage
            messageTextView.setTextColor(Color.parseColor("#8A000000"))
            messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.toFloat())

            val messageTextViewLayoutParams = ConstraintLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            messageTextViewLayoutParams.setMargins(0, resources.dipValue(16), 0, 0)
            messageTextViewLayoutParams.topToBottom = titleTextView.id

            addView(messageTextView, messageTextViewLayoutParams)


            var lastView: View = messageTextView

            if (secondFieldHint != null) {
                val editText = createEditText(context, secondFieldHint, lastView)
                nameEditText = editText
                lastView = nameEditText
            } else {
                nameEditText = EditText(context)
            }

            expressionEditText = createEditText(context, firstFieldHint, lastView)

            expressionEditText.addTextChangedListener(createValidationWatcher(validationListener))

            expressionEditText.requestFocus()

        }

    fun createEditText(context: Context, hint: String?, lastView: View): EditText {
        val editText = EditText(context)
        editText.id = View.generateViewId()
        editText.background.setColorFilter(Color.parseColor("#61000000"), PorterDuff.Mode.SRC_ATOP)
        editText.hint = hint
        editText.inputType = InputType.TYPE_CLASS_TEXT
        editText.maxLines = 1
        editText.setTextColor(Color.parseColor("#DE000000"))
        editText.setHintTextColor(Color.parseColor("#61000000"))
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.toFloat())
        val nameEditTextLayoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        nameEditTextLayoutParams.setMargins(0, resources.dipValue(24), 0, 0)
        nameEditTextLayoutParams.topToBottom = lastView.id

        addView(editText, nameEditTextLayoutParams)
        return editText
    }

    fun createValidationWatcher(validationListener: ValidationListener?): TextWatcher {
            return object : TextWatcher {
                override fun afterTextChanged(newValue: Editable?) {
                    validationListener?.onValidationChanged((newValue?.length ?: 0) > 0)
                }

                override fun beforeTextChanged(string: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(string: CharSequence?, start: Int, before: Int, count: Int) {
                    if (string == null) {
                        return
                    }
                    val currentName = nameEditText.text.toString()
                    if ((string.startsWith(currentName) && currentName.length == string.length - 1)
                            || (currentName.startsWith(string) && string.length == currentName.length - 1)) {
                        nameEditText.text = expressionEditText.text
                    }
                }
            }
        }

        fun getExpression(): String {
            return expressionEditText.text.toString()
        }

        fun getName(): String {
            return nameEditText.text.toString()
        }


    }