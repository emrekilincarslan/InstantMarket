package com.fondova.finance.charts

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.shinobicontrols.charts.*
import com.fondova.finance.App
import com.fondova.finance.R
import com.fondova.finance.charts.axis.VolumeAxis
import com.fondova.finance.charts.axis.DateAxis
import com.fondova.finance.charts.axis.PriceAxis
import com.fondova.finance.charts.strategies.ChartStrategyFactory
import com.fondova.finance.charts.strategies.ChartType
import com.fondova.finance.charts.strategies.VolumeChartStrategy
import com.fondova.finance.persistance.AppStorage
import com.fondova.finance.util.ui.dipValue
import com.fondova.finance.vo.ChartData
import kotlinx.android.synthetic.main.activity_chart.view.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.*
import javax.inject.Inject
import com.shinobicontrols.charts.ChartView as ShinobiChartView


class ChartView(context: Context, attr: AttributeSet?) : RelativeLayout(context, attr),
        ShinobiChart.OnAxisMotionStateChangeListener,
        Axis.OnRangeChangeListener,
        ShinobiChart.OnTickMarkUpdateListener,
        ShinobiChart.OnCrosshairDrawListener,
        ShinobiChart.OnTrackingInfoChangedForTooltipListener,
        ShinobiChart.OnCrosshairActivationStateChangedListener {
    constructor(context: Context) : this(context, null)

    var showVolume = true

    private val TAG = "ChartView"
    private val volumeAreaPercentage = 0.20
    private val priceAreaPercentage = 0.6
    private val historyLimitTimeBetweenMessages = 15
    private val tag = "ChartView"
    private val priceAxis: PriceAxis = PriceAxis()
    private val dateAxis: DateAxis = DateAxis(this)
    private val progressBar: ProgressBar
    private var barAdapter = ChartDataAdapter()
    private var lineAdapter = ChartDataAdapter()
    private var candlestickAdapter = ChartDataAdapter()
    private var volumeAdapter = ChartVolumeAdapter(volumeAreaPercentage)
    private var volumeSeries: Series<*>? = null
    private var shinobiChartView: ShinobiChartView? = null
    private val errorView: TextView = TextView(context)

    @Inject
    lateinit var appStorage: AppStorage

    private lateinit var shinobiChart: ShinobiChart

    private var barSeries: Series<*>? = null
    private var lineSeries: Series<*>? = null
    private var candlestickSeries: Series<*>? = null
    private var isCrosshairShown = false
    private var lastCrosshairPoint: Pair<Float, Float>? = null

    var data: List<ChartData> = listOf()
        set(value) {
            val isFreshData = field.isEmpty() || value.isEmpty()
            field = value
            reloadData(isFreshData)
        }

    var type: ChartType = ChartType.bar
        set(value) {
            field = value
            setSeries(value)
        }

    fun onPause() {
        shinobiChartView?.onPause()
    }

    fun onResume() {
        shinobiChartView?.onResume()
    }

    fun onDestroy() {
        shinobiChartView?.onDestroy()
    }

    fun onCreate(savedInstanceState: Bundle?) {
        shinobiChartView?.onCreate(savedInstanceState)
    }

    private fun setSeries(type: ChartType) {
        var uninitialized = false
        if (barSeries == null) {
            val priceSeries = ChartStrategyFactory().getStrategy(context, ChartType.bar).getSeries()
            priceSeries.dataAdapter = barAdapter
            shinobiChart.addSeries(priceSeries, dateAxis, priceAxis)
            this.barSeries = priceSeries
            uninitialized = true
        }
        if (lineSeries == null) {
            val priceSeries = ChartStrategyFactory().getStrategy(context, ChartType.line).getSeries()
            priceSeries.dataAdapter = lineAdapter
            shinobiChart.addSeries(priceSeries, dateAxis, priceAxis)
            this.lineSeries = priceSeries
            uninitialized = true
        }
        if (candlestickSeries == null) {
            val priceSeries = ChartStrategyFactory().getStrategy(context, ChartType.candlestick).getSeries()
            priceSeries.dataAdapter = candlestickAdapter
            shinobiChart.addSeries(priceSeries, dateAxis, priceAxis)
            this.candlestickSeries = priceSeries
            uninitialized = true
        }
        if (showVolume && volumeSeries == null) {
            addVolumeSeries()
        }

        barSeries?.visibility = getVisibilityForBoolean(type == ChartType.bar)
        lineSeries?.visibility = getVisibilityForBoolean(type == ChartType.line)
        candlestickSeries?.visibility = getVisibilityForBoolean(type == ChartType.candlestick)

        if (uninitialized) {
            shinobiChart.redrawChart()
            dateAxis.resetToDefaultDateRange()
        }
    }

    private fun getVisibilityForBoolean(boolean: Boolean): Int {
        if (boolean) {
            return View.VISIBLE
        }
        return View.GONE
    }

    fun addVolumeSeries() {
        val volumeSeries = VolumeChartStrategy(context).getSeries()
        volumeSeries.dataAdapter = volumeAdapter
        shinobiChart.addSeries(volumeSeries, dateAxis, VolumeAxis())
        this.volumeSeries = volumeSeries
    }

    init {
        App.getAppComponent().inject(this)

        setupShinobiChart(context)
        setupErrorView()
        progressBar = ProgressBar(context)
        val progressBarLayoutParams = LayoutParams(context.resources.dipValue(100), context.resources.dipValue(100))
        progressBarLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)
        addView(progressBar, progressBarLayoutParams)
        showLoadingIndicator(false)

    }

    fun setupErrorView() {
        errorView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
        val errorViewLayoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        errorViewLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)
        errorView.visibility = View.GONE
        addView(errorView, errorViewLayoutParams)

    }

    fun showLoadingIndicator(visible: Boolean) {
        progressBar.visibility = if (visible) View.VISIBLE else View.GONE
        if (visible) {
            data = emptyList()
            hideError()
        }
    }

    fun showError(message: String) {
        errorView.text = message
        errorView.visibility = View.VISIBLE
    }

    fun hideError() {
        errorView.text = ""
        errorView.visibility = View.GONE
    }

    private fun reloadData(resetRange: Boolean) {
        if (data.isEmpty()) {
            val message = context.getString(R.string.charts_empty_msg)
            showError(message)
        }
        for (series in shinobiChart.series) {
            (series.dataAdapter as? UpdatableDataAdapter)?.updateDataPoints(data)
        }

        val dataRange = dateAxis.dataRange as? NumberRange
        if (dataRange == null) {
            Log.e(tag, "No data range, aborting...")
            return
        }

        if (dateAxis.isViewingLastDataPoint() && dateAxis.isNearDefaultRange()) {
            val wasCrosshairShowBeforeUpdate = isCrosshairShown
            for (series in shinobiChart.series) {
                series.dataAdapter.notifyDataChanged()
            }
            val max = shinobiChart.series.first().dataAdapter.dataPointsForDisplay.lastIndex
            val min = max - dateAxis.currentDisplayedRange.span
            dateAxis.requestCurrentDisplayedRange(min + 0.5, max + 0.5, false, false)
            if (wasCrosshairShowBeforeUpdate) {
                showTooltip()
            }
        }

        if (resetRange) {
            dateAxis.resetToDefaultDateRange()
            updatePriceRange()
        }
    }

    private fun setupShinobiChart(context: Context) {

        val shinobiChartView = ShinobiChartView(context)
        val shinobiChartViewLayoutParams = RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(shinobiChartView, shinobiChartViewLayoutParams)
        this.shinobiChartView = shinobiChartView
        shinobiChart = shinobiChartView.shinobiChart


        shinobiChart.setOnAxisMotionStateChangeListener(this)
        shinobiChart.setOnTickMarkUpdateListener(this)
        shinobiChart.setOnCrosshairDrawListener(this)
        shinobiChart.setOnCrosshairActivationStateChangedListener(this)
        shinobiChart.setOnTrackingInfoChangedForTooltipListener(this)

        shinobiChart.setOnGestureListener(DoubleClickListener(OnClickListener {
            dateAxis.resetToDefaultDateRange()
        }))

        shinobiChart.applyTheme(R.style.Theme_Default_Dark_Black, true)


        setSeries(ChartType.bar)

    }

    override fun onUpdateTickMark(tickMark: TickMark?, axis: Axis<*, *>?) {
        if (axis == dateAxis) {
            formatDateTickMark(tickMark)
        } else {
            val value = tickMark?.value as? Double ?: 0.toDouble()
            tickMark?.labelText = String.format("%.${getSignificantDecimalDigits(value)}f", value)
        }
    }

    fun formatDateTickMark(tickMark: TickMark?) {
        val index = tickMark?.value as? Double ?: 0.toDouble()
        if (index < 0 || index >= data.size) {
            tickMark?.labelText = ""
            return
        }
        val dateFormatter = ChartTimeFormatter(DateTimeZone.getDefault())
        val date = dateFormatter.formatServerTimeForDisplay(data[index.toInt()].dateTime, ChartInterval.chartInterval(appStorage.getChartInterval())) ?: "--"

        tickMark?.labelText = date
    }

    private fun getSignificantDecimalDigits(doubleValue: Double): Int {
        if (doubleValue == 0.0) {
            return 1
        }
        var significantDecimalPlaces = if (Math.abs(doubleValue) < 100) 2 else 1
        while (Math.abs(doubleValue) * Math.pow(10.0, significantDecimalPlaces.toDouble()) < 10) {
            significantDecimalPlaces++
        }

        return significantDecimalPlaces
    }

    override fun onAxisMotionStateChange(axis: Axis<*, *>?) {
    }

    var lastHistoryLimitMessageShown: DateTime = DateTime.now().minusSeconds(historyLimitTimeBetweenMessages)

    override fun onRangeChange(axis: Axis<*, *>?) {
        if (axis == dateAxis) {
            if (dateAxis.isViewingFirstDataPoint()) {
                showHistoryLimitMessage()
            }
            updatePriceRange()
        }
    }

    private fun showHistoryLimitMessage() {
        val dateTimeCheck = DateTime.now().minusSeconds(historyLimitTimeBetweenMessages)
        if (lastHistoryLimitMessageShown.isBefore(dateTimeCheck)) {
            lastHistoryLimitMessageShown = DateTime.now()
            AlertDialog.Builder(context).setMessage(R.string.x_axis_end_reached)
                    .setPositiveButton(R.string.ok, null)
                    .create()
                    .show()
        }
    }

    private fun updatePriceRange() {
        val dateRange = dateAxis.getLastDisplayedRange()
        val startRange = Math.max(0, dateRange.minimum.toInt())
        val endRange = Math.min(barAdapter.dataPointsForDisplay.count() - 1, dateRange.maximum.toInt())
        if (endRange < 0) {
            return
        }
        val visibleDataPoints = barAdapter.dataPointsForDisplay.subList(startRange, endRange)
        priceAxis.scaleForVisibleRange(visibleDataPoints, if (showVolume) (1.0 - priceAreaPercentage) else 0.0)
    }

    override fun onTrackingInfoChanged(tooltip: Tooltip, dataPoint: DataPoint<*, *>, dataPointPosition: DataPoint<*, *>, interpolatedDataPointPosition: DataPoint<*, *>?) {
        @Suppress("UNCHECKED_CAST")
        var workingDataPoint: DataPoint<Double, Double>? = interpolatedDataPointPosition as? DataPoint<Double, Double>
        if (workingDataPoint == null) {
            @Suppress("UNCHECKED_CAST")
            workingDataPoint = dataPointPosition as? DataPoint<Double, Double>
        }
        if (workingDataPoint != null) {
            tooltip.center = DataPoint<Double, Double>(workingDataPoint.x, (priceAxis.currentDisplayedRange as NumberRange).maximum!!)
            val tooltipView = tooltip.view as? DefaultTooltipView
            tooltipView?.setText(buildTooltipText(workingDataPoint))
        }

    }

    override fun onCrosshairActivationStateChanged(chart: ShinobiChart?) {
        isCrosshairShown = chart?.crosshair?.isShown == true
    }

    private fun showTooltip() {
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis()
        val metaState = 0
        val motionEvent = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_DOWN,
                lastCrosshairPoint?.first ?: 0.toFloat(),
                chart_view.height / 2F,
                metaState
        )
        chart_view.dispatchTouchEvent(motionEvent)
    }

    override fun onDrawCrosshair(shinobiChart: ShinobiChart?, canvas: Canvas?, rect: Rect?, x: Float, y: Float, r: Float, paint: Paint?) {
        lastCrosshairPoint = Pair(x, y)
        if (rect == null) {
            return
        }

        canvas?.drawCircle(x, y, r, paint)
        canvas?.drawLine(x, y + r, x, rect.top.toFloat(), paint)
        canvas?.drawLine(x, y - r, x, rect.bottom.toFloat(), paint)
        canvas?.drawLine(x + r, y, rect.right.toFloat(), y, paint)
        canvas?.save()

        if (shinobiChart == null) {
            return
        }
        repositionTooltip(shinobiChart, rect, x)
    }

    private fun buildTooltipText(dataPoint: DataPoint<*, *>): SpannableString? {

        val stringBuilder = StringBuilder()
        val index: Int = (dataPoint.x as Double).toInt()


        val chartData = data.getOrNull(index)
        val prevData = data.getOrNull(index - 1)

        val closeNumber = chartData?.close?.number ?: 0.0
        val closePrevNumber = prevData?.close?.number ?: 0.0
        val change = closeNumber - closePrevNumber
        val changePercent = 100 * (closeNumber / closePrevNumber - 1.0)

        val values = context.resources.getString(R.string.format_chart_data_on_tooltip,
                chartData?.close ?: "--",
                chartData?.high ?: "--",
                chartData?.low ?: "--",
                chartData?.open ?: "--",
                chartData?.volume ?: "--",
                if (index == 0) "--" else String.format(Locale.getDefault(), "%.2f", change), // change
                if (index == 0) "--" else String.format(Locale.getDefault(), "%.2f", changePercent)) // changePercent
        val dateFormatter = ChartTimeFormatter(DateTimeZone.getDefault())
        val date = dateFormatter.formatServerTimeForDisplay(chartData?.dateTime, ChartInterval.chartInterval(appStorage.getChartInterval())) ?: "--"
        stringBuilder.append(date)
        stringBuilder.append("\n").append(values)

        val spannable = SpannableString(stringBuilder.toString())
        spannable.setSpan(ForegroundColorSpan( ContextCompat.getColor(context, R.color.view_all_articles)), 0, date.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        return spannable
    }


    private fun repositionTooltip(chart: ShinobiChart, rect: Rect, x: Float) {
        val translationX = (chart.crosshair.tooltip.right - chart.crosshair.tooltip.left) / 2
        val nexX = if (rect.right - (chart.crosshair.tooltip.right + translationX) >= 0) (x + 2) else (rect.right - chart.crosshair.tooltip.width).toFloat()
        chart.crosshair.tooltip.x = nexX
    }


}
