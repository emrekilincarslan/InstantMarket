package com.fondova.finance.charts

import android.util.Log
import com.shinobicontrols.charts.Data
import com.shinobicontrols.charts.DataAdapter
import com.shinobicontrols.charts.DataPoint
import com.fondova.finance.vo.ChartData

class ChartVolumeAdapter(val allowedChartPct: Double): DataAdapter<Double, Double>(), UpdatableDataAdapter {

    private fun createDataPoints(chartData: List<ChartData>): MutableList<Data<Double, Double>> {
        var max = chartData.maxBy { it.volume?.number ?: 0.0 }?.volume?.number ?: 1.0
        val list: MutableList<Data<Double, Double>> = mutableListOf()
        for (index in chartData.indices) {
            val item = chartData[index]
            val volume = item.volume?.number?.toDouble() ?: 0.0
            val dataPoint = DataPoint(index.toDouble(), (volume / max) * allowedChartPct)
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