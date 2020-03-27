package com.fondova.finance.charts

import android.util.Log
import com.shinobicontrols.charts.Data
import com.shinobicontrols.charts.DataAdapter
import com.shinobicontrols.charts.MultiValueDataPoint
import com.fondova.finance.vo.ChartData

interface UpdatableDataAdapter {
    fun updateDataPoints(chartData: List<ChartData>): Boolean
}

class ChartDataAdapter: DataAdapter<Double, Double>(), UpdatableDataAdapter {

    private fun createDataPoints(chartData: List<ChartData>): MutableList<Data<Double, Double>> {
        val list: MutableList<Data<Double, Double>> = mutableListOf()
        for (index in chartData.indices) {
            val item = chartData[index]
            val open = item.open.number.toDouble()
            val close = item.close.number.toDouble()
            val high = item.high.number.toDouble()
            val low = item.low.number.toDouble()
            val dataPoint = MultiValueDataPoint(index.toDouble(), low, high, open, close)
            list.add(dataPoint)
        }
        return list
    }

    override fun updateDataPoints(chartData: List<ChartData>): Boolean {
        val dataPoints = createDataPoints(chartData)

        if (chartData.isEmpty()) {
            clear()
            return true
        }

        if (this.count() == 0) {
            return addAll(dataPoints)
        }

        return replaceLastDataPoint(dataPoints)

    }

    private fun replaceLastDataPoint(dataPoints: MutableList<Data<Double, Double>>): Boolean {
        var updated = false
        if (dataPoints.size == this.count()) {
            Log.d("Adapter", "Removing last data point")

            super.remove(this.count() - 1)
        }
        if (dataPoints.size == this.count() + 1) {
            Log.d("Adapter", "Re-Adding last data point")

            updated = super.add(dataPoints.last())
        }
        return updated
    }

}