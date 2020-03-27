package com.fondova.finance.vo;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Entity
public class KeyValue {

    @PrimaryKey
    @ColumnInfo(name = "key")
    public String key;

    @ColumnInfo(name = "value")
    public String value;

    public KeyValue(@Key String key, String value) {
        this.key = key;
        this.value = value;
    }


    @Retention(RetentionPolicy.CLASS)
    public @interface Key {
    }

    public static KeyValue create(@Key String key, String value) {
        return new KeyValue(key, value);
    }
}
