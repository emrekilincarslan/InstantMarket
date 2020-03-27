package com.fondova.finance.quotes

import com.fondova.finance.R
import com.fondova.finance.api.model.quote.*
import com.fondova.finance.workspace.WorkspaceQuote
import com.fondova.finance.workspace.WorkspaceQuoteType
import java.math.BigDecimal

class QuoteWatchResponseViewModel(val response: QuoteWatchResponse?, val quote: WorkspaceQuote?) {

    private val emptyValue = response?.errorOrEmpty() ?: "--"

    fun getLastText(): String? {
        if (notPermissioned()) {
            return "np"
        }

        return response?.last ?: emptyValue
    }

    fun getChangeText(): String {
        return response?.change ?: emptyValue
    }

    fun getActualSymbol(withDelay: Boolean = false): String {
        var value = response?.actualSymbol ?: quote?.getValue() ?: emptyValue
        var quoteDelay = response?.quoteDelay
        if (quoteDelay != null && quoteDelay != 0 && withDelay) {
            value += " [$quoteDelay]"
        }
        return value
    }

    var showExpressionDescriptions: Boolean = true

    fun getTitleText(withDelay: Boolean = true): String? {
        var name = quote?.getValue()


        if (notPermissioned()) {
            return "${quote?.getValue()} - Not Permissioned"
        }
        if (invalid()) {
            return "${quote?.getValue()} - Invalid"
        }
        if (expired()) {
            return "${quote?.getValue()} - Expired"
        }
        if (otherError()) {
            return "${quote?.getValue()} - Invalid"
        }
        var titleText = quote?.getDisplayName()
        if (isSymbol() || ( showExpressionDescriptions && response?.symbolDescription != null)) {
            titleText = stripDelayedText(response?.symbolDescription)
        }
        if (titleText.isNullOrEmpty()) {
            titleText = quote?.getValue()
        }
        if (titleText.isNullOrEmpty()) {
            return emptyValue
        }
        var quoteDelay = response?.quoteDelay
        if (quoteDelay != null && quoteDelay != 0 && withDelay) {
            titleText += " [$quoteDelay]"
        }
        return titleText
    }

    fun stripDelayedText(string: String?): String? {
        val regex = Regex("\\s?\\[[\\w\\s]*\\]")
        return string?.replace(regex, "")
    }

    fun expired() =
            response?.errors?.firstOrNull { it.detail?.toLowerCase()?.contains("expired") == true } != null

    fun invalid() =
            response?.errors?.firstOrNull { it.code?.toLowerCase()?.contains("not found") == true } != null

    fun notPermissioned() =
            response?.errors?.firstOrNull { it.code?.toLowerCase()?.contains("not permissioned") == true } != null

    fun otherError() =
            response?.errors?.firstOrNull() != null

    fun isSymbol() = quote?.getType()?.toLowerCase() != WorkspaceQuoteType.EXPRESSION

    fun getChangeBackgroundResource(): Int {
        val changeDouble = response?.changePercentage?.toDoubleOrNull()
        var changeBackgroundResource = R.drawable.current_price_red_background
        if (changeDouble == null || changeDouble == 0.toDouble()) {
            changeBackgroundResource = R.drawable.current_price_black_background
        } else if (changeDouble > 0) {
            changeBackgroundResource = R.drawable.current_price_green_background
        }
        return changeBackgroundResource
    }

    fun valueForField(field: String): String {
        if (notPermissioned()) {
            return "np"
        }

        val data = response?.data?.first() ?: emptyMap<String, Any>()
        val value = data[field]

        when (value) {
            is Float, is Double -> return BigDecimal("${value}").toPlainString().removeSuffix(".0")
            is String -> return value
            else -> return emptyValue
        }

    }

    fun getSymbol(): String {
        return response?.actualSymbol ?: quote?.getValue() ?: emptyValue
    }

    fun getLandscapeSubtitle(): String {
        if (response?.errorTitle() != null) {
            return response.errorTitle() ?: "--"
        }
        return "${getSymbol()}${getDelayString()}  La:${valueForField("Last")}  O:${valueForField("Open")}  H:${valueForField("High")}  L:${valueForField("Low")}  V:${valueForField("CumVolume")}"
    }

    fun getPortraitSubtitle(): String {
        if (response?.errorTitle() != null) {
            return response.errorTitle() ?: "--"
        }
        return "${getSymbol()}${getDelayString()}"
    }

    fun getDelayString(): String {
        if (response?.quoteDelay != null && response.quoteDelay != 0) {
            return " [${response.quoteDelay}]"
        }
        return ""
    }

}