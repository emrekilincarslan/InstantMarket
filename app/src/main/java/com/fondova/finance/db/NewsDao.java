package com.fondova.finance.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import com.fondova.finance.vo.NewsCategory;

import java.util.List;

/**
 * Interface for database access for News related operations.
 */
@Dao
public interface NewsDao {

    String QUERY_LOAD_ALL_NEWS_CATEGORIES = "SELECT * FROM news_category ORDER BY `order` ASC";

    @Query(QUERY_LOAD_ALL_NEWS_CATEGORIES)
    List<NewsCategory> loadNewsCategories();

}
