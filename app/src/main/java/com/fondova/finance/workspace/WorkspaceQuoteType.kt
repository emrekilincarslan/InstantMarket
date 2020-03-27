package com.fondova.finance.workspace

import com.fondova.finance.enums.QuoteType

class WorkspaceQuoteType {

    companion object {
        val EXPRESSION = "expression"
        val SYMBOL = "symbol"
        val GROUP = "group"

        fun fromQuoteType(type: Int?): String {
            if (type == QuoteType.EXPRESSION) {
                return EXPRESSION
            } else if (type == QuoteType.SYMBOL) {
                return SYMBOL
            } else {
                return GROUP
            }

        }

        fun toQuoteType(type: String?): Int {
            if (type == EXPRESSION) {
                return QuoteType.EXPRESSION
            } else if (type == SYMBOL) {
                return QuoteType.SYMBOL
            } else {
                return QuoteType.LABEL
            }
        }
    }
}