package com.fondova.finance.vo;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Suggestion {

    public static final String COLUMN_QUERY = "query";

    // needed to be used with CursorAdapter
    @ColumnInfo(name = "_id")
    @PrimaryKey public String id;

    @ColumnInfo(name = COLUMN_QUERY)
    public String query;

    @ColumnInfo(name = "user_id")
    public long userId;

    public Suggestion(String query, long userId) {
        this.query = query;
        this.userId = userId;
    }
}
