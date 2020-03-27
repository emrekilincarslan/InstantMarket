package com.fondova.finance.ui

import android.os.Handler
import android.util.Log
import android.widget.AbsListView


abstract class OnScrollObserver: AbsListView.OnScrollListener {

    abstract fun onScrollUp()
    abstract fun onScrollDown()

    private var lastVisibleView = 0
    private var control = true
    private var scrollBlock = false
    private val scrollSpamThrottlePeriod: Long = 1000

    override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
    }


    override fun onScroll(view: AbsListView?, current: Int, visibles: Int, total: Int) {
        var newPosition = current
        if (scrollBlock || (newPosition == 0 && !control)) {
            return
        }
        if (newPosition < lastVisibleView && !control) {
            Log.d("OnScrollObserver", "Did scroll up: current: $newPosition, lastVisibleView: $lastVisibleView, control: $control, scrollBlock: $scrollBlock")
            blockScrollReporting()
            onScrollUp()
            newPosition += 1 // prevents it from auto-scrolling back up on cell refresh
            control = true
        } else if (newPosition > lastVisibleView && control) {
            Log.d("OnScrollObserver", "Did scroll down: current: $newPosition, lastVisibleView: $lastVisibleView, control: $control, scrollBlock: $scrollBlock")
            blockScrollReporting()
            onScrollDown()
            control = false
        }
        lastVisibleView = newPosition
    }

    fun blockScrollReporting() {
        scrollBlock = true
        Handler().postDelayed({
            scrollBlock = false
        }, scrollSpamThrottlePeriod)
    }
}