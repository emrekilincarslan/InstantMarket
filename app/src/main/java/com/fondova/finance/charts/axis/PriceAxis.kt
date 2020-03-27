package com.fondova.finance.charts.axis

import com.shinobicontrols.charts.*
import com.fondova.finance.AppExecutors

class PriceAxis: NumberAxis() {

    val appExecutors: AppExecutors = AppExecutors()
    val padding: Double = 0.1

    init {
        position = Axis.Position.REVERSE
        enableGesturePanning(false)
        enableGestureZooming(false)
        enableMomentumPanning(false)
        expectedLongestLabel = "X.XXXXX"

    }

    fun scaleForVisibleRange(dataPoints: List<Data<Double, Double>>, additionalBottomPaddingPct: Double) {
        appExecutors.dataThread().execute {
            val newRange = calculateNewRange(dataPoints, additionalBottomPaddingPct)
            appExecutors.mainThread().execute {
                requestCurrentDisplayedRange(newRange.minimum, newRange.maximum, false ,false)
                if (newRange.maximum - newRange.minimum > 0) {
                    defaultRange = newRange // For some strange reason, requestCurrentDisplayedRange doesn't draw correctly without this
                }

            }
        }
    }

    private fun calculateNewRange(dataPoints: List<Data<Double, Double>>, additionalBottomPaddingPct: Double): NumberRange {
        if (dataPoints.isEmpty()) {
            return defaultRange as? NumberRange ?: NumberRange(0.0, 0.0)
        }

        var high: Double = Double.NEGATIVE_INFINITY
        var low: Double = Double.POSITIVE_INFINITY
        for (dataPoint in dataPoints) {
            val item = dataPoint as? MultiValueDataPoint<Double, Double>

            val itemHigh = item?.high ?: 0.toDouble()
            val itemLow = item?.low ?: 0.toDouble()
            high = Math.max(high, itemHigh)
            low = Math.min(low, itemLow)
        }

        val diff = Math.abs(high - low)
        val bottomPadding = (diff * (padding + additionalBottomPaddingPct))
        val topPadding = (diff * padding)

        val min = low - bottomPadding
        val max = high + topPadding

        val range = NumberRange(min, max)

        return range

    }
}