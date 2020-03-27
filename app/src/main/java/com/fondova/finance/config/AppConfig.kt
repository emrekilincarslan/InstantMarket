package com.fondova.finance.config

import android.content.res.Resources
import com.fondova.finance.R


interface AppConfig {
    fun useStockSettings(): Boolean
    fun useMultipleWorkspaces(): Boolean
    fun useGoogleDrive(): Boolean
    fun showNewsTab(): Boolean
    fun showSymbolDataInChartTitle(): Boolean
}

class ValuesResourceAppConfig(val resources: Resources): AppConfig {

    override fun showNewsTab(): Boolean {
        return resources.getBoolean(R.bool.showNewsTab)
    }


    override fun useStockSettings(): Boolean {
        return resources.getBoolean(R.bool.use_stock_settings)
    }

    override fun useMultipleWorkspaces(): Boolean {
        return resources.getBoolean(R.bool.use_multiple_workspaces)
    }

    override fun useGoogleDrive(): Boolean {
        return resources.getBoolean(R.bool.use_google_drive)
    }

    override fun showSymbolDataInChartTitle(): Boolean {
        return resources.getBoolean(R.bool.show_symbol_data_in_chart_title)
    }
}