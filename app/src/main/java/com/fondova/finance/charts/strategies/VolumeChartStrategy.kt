package com.fondova.finance.charts.strategies

import android.content.Context
import android.support.v4.content.ContextCompat
import com.shinobicontrols.charts.ColumnSeries
import com.shinobicontrols.charts.DataPoint
import com.shinobicontrols.charts.Series
import com.shinobicontrols.charts.SeriesStyle
import com.fondova.finance.R
import com.fondova.finance.util.ui.dipValue
import com.fondova.finance.vo.ChartData

class VolumeChartStrategy(val context: Context): ChartStrategy {
    override fun getSeries(): Series<*> {
        val series = ColumnSeries()
        series.style.lineColor = ContextCompat.getColor(context, R.color.white)
        series.style.lineWidth = context.resources.dipValue(4).toFloat()
        series.style.areaColor = ContextCompat.getColor(context, R.color.grey_8a)
        series.style.isLineShown = true
        series.style.fillStyle = SeriesStyle.FillStyle.FLAT
        return series
    }

    override fun getChartData(dataPoint: ChartData, index: Int): DataPoint<*, *> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}