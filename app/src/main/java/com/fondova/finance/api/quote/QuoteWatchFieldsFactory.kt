package com.fondova.finance.api.quote

import com.fondova.finance.config.AppConfig
import com.fondova.finance.persistance.AppStorage
import javax.inject.Inject
import javax.inject.Singleton

interface QuoteFieldsFactory {
    fun getQuoteRequestFields(): List<String>
    fun getQuoteRequestFieldsArray(): Array<String>
}

@Singleton
class QuoteWatchFieldsFactory @Inject constructor(val appStorage: AppStorage, val appConfig: AppConfig): QuoteFieldsFactory {

    override fun getQuoteRequestFields(): List<String> {
        if (!appConfig.useStockSettings()) {
            return  defaultFields
        }
        val workspaceFields: MutableList<String> = appStorage.getWorkspace().getExpandedFields().toMutableList()
        workspaceFields.addAll(commonFields)
        return workspaceFields
    }

    override fun getQuoteRequestFieldsArray(): Array<String> {
        return getQuoteRequestFields().toTypedArray()
    }

    private val commonFields = listOf(
            "UserDescription",
            "IssueDescription",
            "QuoteDelay",
            "PctChange",
            "ActualSymbol"
    )

    private val defaultFields = listOf(
            "Last",
            "LastTicknum",
            "Change",
            "UserDescription",
            "IssueDescription",
            "PctChange",
            "High",
            "Low",
            "Open",
            "Bid",
            "Ask",
            "CumVolume",
            "TradeDateTime",
            "SettlementPrice",
            "Settledate",
            "QuoteDelay",
            "ActualSymbol"
    )
}