package com.fondova.finance.charts.strategies

enum class ChartType {
    line, bar, candlestick;

    companion object {
        fun fromLegacyChartStyle(value: Int): ChartType {
            when (value) {
                0 -> return bar
                1 -> return line
                2 -> return candlestick
            }
            return bar
        }
    }

    fun getLegacyChartStyle(): Int {
        when (this) {
            bar -> return 0
            line -> return 1
            candlestick -> return 2
        }
    }
}
