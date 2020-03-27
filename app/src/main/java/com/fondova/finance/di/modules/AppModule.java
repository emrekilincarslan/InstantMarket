package com.fondova.finance.di.modules;

import com.fondova.finance.App;
import com.fondova.finance.config.AppConfig;
import com.fondova.finance.config.ValuesResourceAppConfig;
import com.fondova.finance.db.AppDb;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private App app;

    public AppModule(App app) {
        this.app = app;
    }

    @Provides
    @Singleton
    public App provideApplication() {
        return app;
    }

    @Singleton
    @Provides
    AppDb provideDb(App app) {
        return AppDb.create(app);
    }

    @Singleton
    @Provides
    AppConfig provideAppConfig(App app) {
        return new ValuesResourceAppConfig(app.getResources());
    }

}

