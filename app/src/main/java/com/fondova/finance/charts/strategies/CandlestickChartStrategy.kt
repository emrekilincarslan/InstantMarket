package com.fondova.finance.charts.strategies

import android.content.Context
import android.support.v4.content.ContextCompat
import com.shinobicontrols.charts.*
import com.shinobicontrols.charts.CandlestickSeries
import com.fondova.finance.R
import com.fondova.finance.vo.ChartData

class CandlestickChartStrategy(val context: Context) : ChartStrategy, SeriesStyleProvider<CandlestickSeriesStyle> {
    override fun <S : Series<CandlestickSeriesStyle>?> provide(data: Data<*, *>?, i: Int, series: S): CandlestickSeriesStyle {

        val colorUp = ContextCompat.getColor(context, R.color.green)
        val colorDown = ContextCompat.getColor(context, R.color.red)

        if (series == null) {
            return CandlestickSeriesStyle()
        }

        val defaultStyle = series.createDefaultSeriesStyleProvider()
        val providedStyle = defaultStyle.provide(data, i, series)

        if (data == null) {
            return providedStyle
        }

        if (series.getDataAdapter() != null && series.getDataAdapter().size() > 0 && i < series.getDataAdapter().size()) {
            val currentClose = (data as? MultiValueDataPoint)?.close as? Double ?: 0.0
            val currentOpen = (data as? MultiValueDataPoint)?.open as? Double ?: 0.0
            var priorClose = 0.0
            if (i > 0 && i < series.getDataAdapter().size()) {
                val priorPoint = series.getDataAdapter().get(i - 1) as? MultiValueDataPoint
                priorClose = priorPoint?.close as? Double ?: currentClose
            }

            val color: Int
            if (currentClose > priorClose && currentClose > currentOpen) {
                color = colorUp
            } else {
                color = colorDown
            }
            providedStyle.fallingColor = color
            providedStyle.risingColor = color
            providedStyle.risingColorGradient = color
            providedStyle.fallingColorGradient = color
            providedStyle.outlineColor = color
            providedStyle.stickColor = color
        }
        return providedStyle
    }

    override fun getSeries(): Series<*> {
        val series = CandlestickSeries()
        series.seriesStyleProvider = this
        series.isCrosshairEnabled = true
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
