package com.fondova.finance.charts

import org.joda.time.DateTime
import org.joda.time.DateTimeZone

class ChartTimeFormatter(val dateTimeZone: DateTimeZone) {

    fun formatServerTimeForDisplay(time: String?, chartInterval: ChartInterval): String? {

        var serverTime = DateTime(time, DateTimeZone.UTC)
        if (time?.length ?: 0 <= 10) {
            serverTime = serverTime.plusHours(12)
        }
        var adjustedServerTime = if (time?.length ?: 0 < 10) serverTime.plusHours(12) else serverTime

        val localTime = adjustedServerTime.withZone(dateTimeZone)
        val date = ChartInterval.chartInterval(chartInterval.intervalValue()).getDateFormatter().format(localTime.toDate())

        return date
    }

}