package com.fondova.finance.charts.strategies

import android.content.Context

class ChartStrategyFactory {

    fun getStrategy(context: Context, type: ChartType): ChartStrategy {
        when (type) {
            ChartType.bar -> return BarChartStrategy(context)
            ChartType.candlestick -> return CandlestickChartStrategy(context)
            ChartType.line -> return LineChartStrategy(context)
        }
    }
}