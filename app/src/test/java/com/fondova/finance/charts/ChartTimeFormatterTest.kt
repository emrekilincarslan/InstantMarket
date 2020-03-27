package com.fondova.finance.charts

import org.joda.time.DateTimeZone
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ChartTimeFormatterTest {

    lateinit var testObject: ChartTimeFormatter

    @Before
    fun setup() {
        testObject = ChartTimeFormatter(DateTimeZone.forOffsetHours(-5))
    }

    @Test
    fun convertFiveMinuteTime() {
        val serverTime= "2018-08-27T10:30"
        val expectedTime = "8/27/18\n5:30AM CDT"

        assertEquals(expectedTime, testObject.formatServerTimeForDisplay(serverTime, ChartInterval.fiveMinute))
    }

    @Test
    fun convertThirtyMinuteTime() {
        val serverTime= "2018-08-24T11:30"
        val expectedTime = "8/24/18\n6:30AM CDT"

        assertEquals(expectedTime, testObject.formatServerTimeForDisplay(serverTime, ChartInterval.thirtyMinute))
    }

    @Test
    fun convertSixtyMinuteTime() {
        val serverTime= "2018-08-23T08:00"
        val expectedTime = "8/23/18\n3:00AM CDT"

        assertEquals(expectedTime, testObject.formatServerTimeForDisplay(serverTime, ChartInterval.sixtyMinute))
    }

    @Test
    fun convertDayTime() {
        val serverTime = "2018-06-12"
        val expectedTime = "6/12/18"

        assertEquals(expectedTime, testObject.formatServerTimeForDisplay(serverTime, ChartInterval.day))
    }

    @Test
    fun convertWeekTime() {
        val serverTime= "2017-10-23"
        val expectedTime = "10/23/17"

        assertEquals(expectedTime, testObject.formatServerTimeForDisplay(serverTime, ChartInterval.week))
    }

    @Test
    fun convertMonthTime() {
        val serverTime= "2008-01-01"
        val expectedTime = "1/08"

        assertEquals(expectedTime, testObject.formatServerTimeForDisplay(serverTime, ChartInterval.month))
    }

}