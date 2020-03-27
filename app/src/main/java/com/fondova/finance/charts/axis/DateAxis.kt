package com.fondova.finance.charts.axis

import android.util.Log
import com.shinobicontrols.charts.Axis
import com.shinobicontrols.charts.NumberAxis
import com.shinobicontrols.charts.NumberRange

class DateAxis(val onRangeChangeListener: OnRangeChangeListener): NumberAxis(), Axis.OnRangeChangeListener {

    private var _lastDisplayedRange: NumberRange = NumberRange(0.0, 0.0)
    private val lastDataPointSensitivity: Double = 2.0
    private val spanResetSensitivity = 5

    companion object {
        val defaultSpan: Double = 31.0
    }

    init {
        enableGesturePanning(true)
        enableGestureZooming(true)
        enableMomentumPanning(true)
        enableDoubleTap(false)
        style.gridlineStyle.setGridlinesShown(false)
        style.tickStyle.setMajorTicksShown(false)
        style.tickStyle.setMinorTicksShown(false)
        addOnRangeChangeListener(this)
        expectedLongestLabel = "12/30/00\n12:00PM CDT"

    }

    override fun onRangeChange(axis: Axis<*, *>?) {
        val range = currentDisplayedRange as? NumberRange
        if (range != null) {
            _lastDisplayedRange = range
        }

        majorTickFrequency = (range?.span ?: 0.0) / 3.0
        Log.d("DateAxis", "Setting majorTickFrequency to :${(range?.span ?: 0.0) / 3.0} for span: ${range?.span}")
        onRangeChangeListener.onRangeChange(axis)
    }

    fun getLastDisplayedRange(): NumberRange {
        return _lastDisplayedRange
    }

    fun resetToDefaultDateRange() {
        val dataRange = dataRange as? NumberRange ?: NumberRange(0.0, 0.0)
        val rangeMax = dataRange.maximum
        val rangeMin = Math.max((dataRange.maximum - defaultSpan), 0.toDouble())
        val defaultRange = NumberRange(rangeMin + 0.5, rangeMax + 0.5)
        if (defaultRange.span > 0 && defaultRange.minimum != Double.NEGATIVE_INFINITY && defaultRange.maximum != Double.POSITIVE_INFINITY) {
            this.defaultRange = defaultRange
        }
        requestCurrentDisplayedRange(defaultRange.minimum, defaultRange.maximum, false, false)
    }

    fun isViewingLastDataPoint(): Boolean {
        val dataRange = dataRange as? NumberRange
        val visibleMax = _lastDisplayedRange.maximum ?: 0.toDouble()
        val dataeMax = dataRange?.maximum ?: 0.toDouble()
        return (dataeMax - visibleMax) < lastDataPointSensitivity
    }

    fun isNearDefaultRange(): Boolean {
        val dataRange = dataRange as? NumberRange
        val dataSpan = dataRange?.span ?: 0.0

        return Math.abs(_lastDisplayedRange.span - defaultSpan) < spanResetSensitivity || _lastDisplayedRange.span == 0.0 || dataSpan < defaultSpan
    }

    fun isViewingFirstDataPoint(): Boolean {
        val visibleMin = _lastDisplayedRange.minimum ?: 0.toDouble()
        return visibleMin < 0
    }


}

