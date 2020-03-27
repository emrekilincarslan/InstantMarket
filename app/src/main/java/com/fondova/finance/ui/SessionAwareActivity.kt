package com.fondova.finance.ui

import android.app.AlertDialog
import android.arch.lifecycle.LifecycleActivity
import android.arch.lifecycle.Observer
import android.os.Bundle
import com.fondova.finance.App
import com.fondova.finance.BuildConfig
import com.fondova.finance.R
import com.fondova.finance.api.session.SessionService
import com.fondova.finance.api.session.SessionStatus
import com.fondova.finance.diagnostics.FinanceXAnalytics
import com.fondova.finance.ui.user.login.LoginActivity
import net.hockeyapp.android.CrashManager
import net.hockeyapp.android.CrashManagerListener
import javax.inject.Inject

abstract class SessionAwareActivity: LifecycleActivity() {

    private var analytics: FinanceXAnalytics = FinanceXAnalytics(this)

    @Inject
    lateinit var sessionService: SessionService
    private var seatBumpObserver: Observer<SessionStatus> = object : Observer<SessionStatus> {
        override fun onChanged(sessionStatus: SessionStatus?) {
            if (sessionStatus == SessionStatus.seatbump && this.javaClass != LoginActivity::class.java) {
                LoginActivity.startAfterForcedLogout(this@SessionAwareActivity)
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.getAppComponent().inject(this)

    }

    override fun onResume() {
        super.onResume()
        analytics.setActivity(this)

        CrashManager.register(this, BuildConfig.HOCKEYAPP_SDK_APP_ID, MyCrashManagerListener())
        sessionService.incrementActivityCount()
        val observer = sessionService.getSessionStatusLiveData().observe(this, seatBumpObserver)


    }

    override fun onPause() {
        sessionService.decrementActivityCount()
        sessionService.getSessionStatusLiveData().removeObserver(seatBumpObserver)
        super.onPause()
    }

    protected fun checkApiAvailability(): Boolean {
        if (sessionService.getSessionStatusLiveData().value != SessionStatus.connected) {
            showApiError()
            return false
        }
        return true
    }

    private fun showApiError() {
        AlertDialog.Builder(this, R.style.ChartDialog)
                .setTitle(R.string.disconnected)
                .setMessage(R.string.not_connected_to_server_message)
                .setPositiveButton(R.string.ok, null)
                .create()
                .show()
    }

    private class MyCrashManagerListener : CrashManagerListener() {
        override fun shouldAutoUploadCrashes(): Boolean {
            return true
        }
    }

}