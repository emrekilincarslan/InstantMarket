package com.fondova.finance.repo;

import android.database.Cursor;
import android.database.MatrixCursor;

import com.fondova.finance.persistance.AppStorage;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SuggestionRepository {

    private AppStorage appStorage;
    private final int MAX_SEARCH_HISTORY = 10;
    public static final String COLUMN_QUERY = "query";

    @Inject
    public SuggestionRepository(AppStorage appStorage) {
        this.appStorage = appStorage;
    }

    public void saveSearchQuery(String query) {
        List<String> searchHistory = appStorage.getSearchHistory();
        int existingIndex = searchHistory.indexOf(query);
        if (existingIndex != -1) {
            searchHistory.remove(existingIndex);
        }
        searchHistory.add(0, query);
        if (searchHistory.size() > MAX_SEARCH_HISTORY) {
            searchHistory.remove(searchHistory.size() - 1);
        }
        appStorage.setSearchHistory(searchHistory);
    }

    public Cursor getAllSuggestionsCursor() {
        String[] columnNames = {"_id","query"};
        MatrixCursor cursor = new MatrixCursor(columnNames);

        String[] temp = new String[2];
        int id = 0;
        for(String item : appStorage.getSearchHistory()){
            temp[0] = Integer.toString(id++);
            temp[1] = item;
            cursor.addRow(temp);
        }
        return cursor;
    }

    public String getSuggestionAtPosition(int position) {
        return appStorage.getSearchHistory().get(position);
    }

}
