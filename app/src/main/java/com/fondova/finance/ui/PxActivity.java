package com.fondova.finance.ui;

import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.widget.Toolbar;

import com.fondova.finance.App;
import com.fondova.finance.BuildConfig;
import com.fondova.finance.R;
import com.fondova.finance.api.session.SessionService;
import com.fondova.finance.api.session.SessionStatus;
import com.fondova.finance.api.socket.ApiService;
import com.fondova.finance.di.components.AppComponent;
import com.fondova.finance.diagnostics.FinanceXAnalytics;
import com.fondova.finance.ui.user.login.LoginActivity;
import com.fondova.finance.util.IMMLeaks;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;


public abstract class PxActivity extends LifecycleActivity  {



    private static final String TAG = "PxActivity";
    @Nullable
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    @Inject ApiService apiService;
    @Inject
    SessionService sessionService;
    private Observer<SessionStatus> sessionStatusObserver;
    private FinanceXAnalytics analytics;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        baseInjection();
        this.analytics = new FinanceXAnalytics(this);
        doInject(App.getAppComponent());
        addLifecycleObserver();
        IMMLeaks.fixFocusedViewLeak(getApplication());
        super.onCreate(savedInstanceState);
        setContentView(layoutId());
        ButterKnife.bind(this);
        if (toolbar != null) {
            setActionBar(toolbar);
        }
        sessionStatusObserver = sessionStatus -> {
            if (sessionStatus == SessionStatus.seatbump) {
                LoginActivity.startAfterForcedLogout(this);
            }
        };

    }

    @Override
    public void onResume() {
        super.onResume();
        analytics.setActivity(this);

        CrashManager.register(this, BuildConfig.HOCKEYAPP_SDK_APP_ID, new MyCrashManagerListener());
        sessionService.incrementActivityCount();
        sessionService.getSessionStatusLiveData().observe(this, sessionStatusObserver);
    }

    @Override
    protected void onPause() {
        sessionService.decrementActivityCount();
        sessionService.getSessionStatusLiveData().removeObserver(sessionStatusObserver);
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    protected void onDestroy() {
        removeLifecycleObserver();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addLifecycleObserver() {
        LifecycleObserver lifecycleObserver = getLifecycleObserver();
        if (lifecycleObserver != null) {
            getLifecycle().addObserver(lifecycleObserver);
        }
    }

    private void removeLifecycleObserver() {
        LifecycleObserver lifecycleObserver = getLifecycleObserver();
        if (lifecycleObserver != null) {
            getLifecycle().removeObserver(lifecycleObserver);
        }
    }

    private static class MyCrashManagerListener extends CrashManagerListener {
        @Override
        public boolean shouldAutoUploadCrashes() {
            return true;
        }
    }

    protected void onConnectionLost() {
    }

    @Nullable
    public Toolbar getToolbar() {
        return toolbar;
    }

    private void baseInjection() {
        App.getAppComponent().inject(this);
    }

    /**
     * @return layout resource which is to be inflated
     */
    @LayoutRes
    protected abstract int layoutId();

    protected abstract LifecycleObserver getLifecycleObserver();

    protected abstract void doInject(AppComponent component);

}
