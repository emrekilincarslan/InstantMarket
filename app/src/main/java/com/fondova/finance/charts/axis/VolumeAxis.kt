package com.fondova.finance.charts.axis

import com.shinobicontrols.charts.NumberAxis
import com.shinobicontrols.charts.NumberRange
import com.fondova.finance.AppExecutors

class VolumeAxis: NumberAxis() {

    val appExecutors: AppExecutors = AppExecutors()

    init {
        enableGesturePanning(false)
        enableGestureZooming(false)
        enableMomentumPanning(false)
        style.tickStyle.setLabelsShown(false)
        style.gridlineStyle.setGridlinesShown(false)
        style.tickStyle.setMajorTicksShown(false)
        style.tickStyle.setMinorTicksShown(false)
        defaultRange = NumberRange(0.0, 1.0)
    }

}