package com.fondova.finance.util.ui

import android.content.res.Resources
import android.util.TypedValue

fun Resources.dipValue(value: Int): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,value.toFloat(), displayMetrics).toInt()
}