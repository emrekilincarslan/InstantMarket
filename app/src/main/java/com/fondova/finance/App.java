package com.fondova.finance;

import android.app.Application;

import com.fondova.finance.di.components.AppComponent;
import com.fondova.finance.di.components.DaggerAppComponent;
import com.fondova.finance.di.modules.AppModule;
import com.fondova.finance.diagnostics.FinanceXAnalytics;

import net.danlew.android.joda.JodaTimeAndroid;


public class App extends Application {

    private static AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        FinanceXAnalytics analytics = new FinanceXAnalytics(this);
        analytics.appLaunched();


        appComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
        appComponent.inject(this);
        JodaTimeAndroid.init(this);

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public static AppComponent getAppComponent() {
        return appComponent;
    }
}


