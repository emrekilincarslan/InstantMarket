package com.fondova.finance.charts.strategies

import android.content.Context
import android.support.v4.content.ContextCompat
import com.shinobicontrols.charts.*
import com.fondova.finance.R
import com.fondova.finance.vo.ChartData

class BarChartStrategy(val context: Context): ChartStrategy {
    override fun getSeries(): Series<*> {
        val series = OHLCSeries()
        series.isCrosshairEnabled = true
        series.gestureSelectionMode = Series.GestureSelectionMode.POINT_SINGLE
        series.style.fallingColor = ContextCompat.getColor(context, R.color.red)
        series.style.fallingColorGradient = ContextCompat.getColor(context, R.color.red)
        series.style.risingColor = ContextCompat.getColor(context, R.color.green)
        series.style.risingColorGradient = ContextCompat.getColor(context, R.color.green)
        series.style.armWidth = 2F
        series.style.trunkWidth = 2F
        return series
    }

    override fun getChartData(dataPoint: ChartData, index: Int): DataPoint<*, *> {
        return MultiValueDataPoint<Int, Double>(
                index,
                dataPoint.low.number,
                dataPoint.high.number,
                dataPoint.open.number,
                dataPoint.close.number)

    }
}