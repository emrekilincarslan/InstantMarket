package com.fondova.finance.diagnostics

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.fondova.finance.FlavorConstants

class FinanceXAnalytics(val context: Context) {

    val firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    fun setActivity(activity: Activity) {
        if (!FlavorConstants.ENABLE_GOOGLE_ANALYTICS) {
            return
        }
        val longName = activity.javaClass.name
        val shortName = longName.split(".").last()
        firebaseAnalytics.setCurrentScreen(activity, shortName, null)
    }

    fun appLaunched() {
        if (!FlavorConstants.ENABLE_GOOGLE_ANALYTICS) {
            return
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, Bundle())
    }

    fun setUserId(id: String) {
        if (!FlavorConstants.ENABLE_GOOGLE_ANALYTICS) {
            return
        }
        firebaseAnalytics.setUserId(id)
    }
}