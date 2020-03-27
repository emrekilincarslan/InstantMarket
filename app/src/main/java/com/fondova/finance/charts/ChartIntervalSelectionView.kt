package com.fondova.finance.charts

import android.content.Context
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import com.fondova.finance.R
import java.text.SimpleDateFormat
import java.util.*


enum class ChartInterval {

    fiveMinute, thirtyMinute, sixtyMinute, day, week, month;

    companion object {
        private val DATE_FORMAT_SHORT = SimpleDateFormat("M/d/yy\nh:mma z", Locale.US)
        private val DATE_FORMAT_LOCAL = SimpleDateFormat("M/d/yy", Locale.US)
        private val DATE_FORMAT_MONTH = SimpleDateFormat("M/yy", Locale.US)

        fun chartInterval(fromIntValue: Int): ChartInterval {
            for (interval in enumValues<ChartInterval>()) {
                if (interval.intervalValue() == fromIntValue) {
                    return interval
                }
            }
            return ChartInterval.fiveMinute

        }

    }

    fun stringValue(context: Context): String {
        when (this) {
            fiveMinute -> return context.resources.getString(R.string.min_5)
            thirtyMinute -> return context.resources.getString(R.string.min_30)
            sixtyMinute -> return context.resources.getString(R.string.min_60)
            day -> return context.resources.getString(R.string.day)
            week -> return context.resources.getString(R.string.week)
            month -> return context.resources.getString(R.string.month)
        }
    }

    fun intervalValue(): Int {
        when (this) {
            fiveMinute -> return 5
            thirtyMinute -> return 30
            sixtyMinute -> return 60
            day -> return 24 * 60
            week -> return 24 * 60 * 7
            month -> return 24 * 60 * 30
        }
    }

    fun getDateFormatter(): SimpleDateFormat {
        when (this) {
            fiveMinute -> return DATE_FORMAT_SHORT
            thirtyMinute -> return DATE_FORMAT_SHORT
            sixtyMinute -> return DATE_FORMAT_SHORT
            day -> return DATE_FORMAT_LOCAL
            week -> return DATE_FORMAT_LOCAL
            month -> return DATE_FORMAT_MONTH
        }
    }

}

interface OnChartIntervalSelectedListener {
    fun onChartIntervalSelected(interval: ChartInterval)
}

class ChartIntervalSelectionView(context: Context, attr: AttributeSet?): TabLayout(context, attr) {
    constructor(context: Context): this(context, null)

    var onChartIntervalSelectedListener: OnChartIntervalSelectedListener? = null
    val intervals = enumValues<ChartInterval>()

    init {
        tabMode = MODE_SCROLLABLE
        for (index in intervals.indices) {
            addTab(newTab().setText(intervals[index].stringValue(context)))
        }

        setSelectedTabIndicatorColor(ContextCompat.getColor(context, R.color.red))
        addOnTabSelectedListener( object : OnTabSelectedListener {
            override fun onTabReselected(tab: Tab?) {

            }

            override fun onTabUnselected(tab: Tab?) {

            }

            override fun onTabSelected(tab: Tab?) {
                if (tab == null) {
                    return
                }
                onChartIntervalSelectedListener?.onChartIntervalSelected(intervals[tab.position])
            }

        })
    }

    fun setChartInterval(interval: Int) {
        for (index in intervals.indices) {
            if (intervals[index].intervalValue() == interval) {
                val tab = getTabAt(index)
                tab?.select()
            }
        }
    }

}
