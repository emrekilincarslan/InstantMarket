package com.fondova.finance.ui.util

import android.view.MotionEvent
import android.view.View

class MultiTapListener(val numberOfTaps: Int, val listener: View.OnTouchListener): View.OnTouchListener {

    var lastTapTime: Long = 0
    val tapInterval: Long = 500
    var pendingTaps: Int = 1

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        if (event?.action != MotionEvent.ACTION_UP) {
            return false
        }
        val time = System.currentTimeMillis()
        if (time - lastTapTime < tapInterval) {
            pendingTaps += 1
            if (pendingTaps >= numberOfTaps) {
                return listener.onTouch(view, event)
            }
        } else {
            pendingTaps = 1
        }
        lastTapTime = time
        return false
    }


}