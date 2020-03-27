package com.fondova.finance.di.modules;

import android.content.Context;
import android.content.SharedPreferences;

import com.fondova.finance.App;
import com.fondova.finance.db.TextsDao;
import com.fondova.finance.persistance.KeyValueStorage;
import com.fondova.finance.persistance.SharedPreferencesStorage;
import com.fondova.finance.repo.EncryptionService;
import com.fondova.finance.repo.SecretRepository;
import com.fondova.finance.repo.TextsRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class RepoModule {

    @Provides
    @Singleton
    TextsRepository provideTextsRepository(App app) {
        return new TextsDao(app);
    }

    @Provides
    @Singleton
    EncryptionService provideEncryptionService(App app) {
        return new SecretRepository(app);
    }

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences(App app) {
        return app.getSharedPreferences("com.fondova.financex", Context.MODE_PRIVATE);
    }

    @Provides
    @Singleton
    KeyValueStorage provideKeyValueStorage(SharedPreferences sharedPreferences) {
        return new SharedPreferencesStorage(sharedPreferences);
    }

}
