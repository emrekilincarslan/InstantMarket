package com.fondova.finance.charts.strategies

import com.shinobicontrols.charts.DataPoint
import com.shinobicontrols.charts.Series
import com.fondova.finance.vo.ChartData

interface ChartStrategy {

    fun getSeries(): Series<*>
    fun getChartData(dataPoint: ChartData, index: Int): DataPoint<*,*>

}