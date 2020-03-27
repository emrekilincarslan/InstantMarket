package com.fondova.finance.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.fondova.finance.vo.KeyValue;

@Dao
public interface KeyValueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(KeyValue keyValue);

    @Query("SELECT * FROM KeyValue WHERE key LIKE :key")
    KeyValue getKeyValue(@KeyValue.Key String key);

}
