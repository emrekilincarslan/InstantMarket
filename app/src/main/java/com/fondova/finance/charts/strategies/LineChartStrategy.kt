package com.fondova.finance.charts.strategies

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import com.shinobicontrols.charts.DataPoint
import com.shinobicontrols.charts.LineSeries
import com.shinobicontrols.charts.Series
import com.shinobicontrols.charts.SeriesStyle
import com.fondova.finance.R
import com.fondova.finance.vo.ChartData

class LineChartStrategy(val context: Context) : ChartStrategy {

    override fun getSeries(): Series<*> {
        val series = LineSeries()
        series.isCrosshairEnabled = true
        series.linePathInterpolator = null
        series.style.lineWidth = 2.0f

        series.style.lineColor = ContextCompat.getColor(context, R.color.chart_line)
        series.style.lineColorBelowBaseline = ContextCompat.getColor(context, R.color.chart_line)

        series.style.pointStyle.setPointsShown(false)
        series.style.pointStyle.innerRadius = 0.0f
        series.style.pointStyle.color = ContextCompat.getColor(context, R.color.chart_line)
        series.style.pointStyle.colorBelowBaseline = ContextCompat.getColor(context, R.color.chart_line)
        series.style.pointStyle.innerColorBelowBaseline = ContextCompat.getColor(context, R.color.chart_line)

        series.gestureSelectionMode = Series.GestureSelectionMode.POINT_SINGLE
        series.selectedStyle.fillStyle = SeriesStyle.FillStyle.FLAT
        series.selectedStyle.areaColor = Color.WHITE

        return series
    }

    override fun getChartData(dataPoint: ChartData, index: Int): DataPoint<*, *> {
        return DataPoint<Double, Int>(dataPoint.close.number, index)
    }

}
