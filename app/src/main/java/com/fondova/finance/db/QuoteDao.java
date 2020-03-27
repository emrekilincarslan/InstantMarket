package com.fondova.finance.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.fondova.finance.vo.Quote;

import java.util.List;

/**
 * Interface for database access for User related operations.
 */
@Dao
public interface QuoteDao {

    @Query("SELECT * FROM quote ORDER BY `order` ASC")
    List<Quote> getQuotes();

}
