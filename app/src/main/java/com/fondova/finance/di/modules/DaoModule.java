package com.fondova.finance.di.modules;

import com.fondova.finance.db.AppDb;
import com.fondova.finance.db.KeyValueDao;
import com.fondova.finance.db.NewsDao;
import com.fondova.finance.db.QuoteDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DaoModule {

    @Singleton
    @Provides
    KeyValueDao provideKeyValueDao(AppDb db) {
        return db.keyValueDao();
    }

    @Singleton
    @Provides
    QuoteDao provideQuoteDao(AppDb db) {
        return db.quoteDao();
    }

    @Singleton
    @Provides
    NewsDao provideNewsDao(AppDb db) {
        return db.newsDao();
    }

}


