package com.fondova.finance.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.fondova.finance.App;
import com.fondova.finance.db.converter.ChartConverter;
import com.fondova.finance.vo.ChartData;
import com.fondova.finance.vo.KeyValue;
import com.fondova.finance.vo.NewsCategory;
import com.fondova.finance.vo.Quote;
import com.fondova.finance.vo.QuoteValue;
import com.fondova.finance.vo.Suggestion;
import com.fondova.finance.vo.User;

@Database(entities = {User.class, KeyValue.class, Quote.class, Suggestion.class, QuoteValue.class, NewsCategory.class, ChartData.class},
        version = 2)
@TypeConverters({ChartConverter.class, User.UserFeatureTypeConverter.class})
public abstract class AppDb extends RoomDatabase {

    private static final String DATABASE_NAME = "financeX.db";

    abstract public KeyValueDao keyValueDao();

    abstract public QuoteDao quoteDao();

    abstract public NewsDao newsDao();

    public static AppDb create(App app) {
        return Room
                .databaseBuilder(app, AppDb.class, DATABASE_NAME)
                .allowMainThreadQueries()
                .build();
    }
}
