package com.fondova.finance.ui.symbol.add;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.fondova.finance.App;
import com.fondova.finance.api.Resource;
import com.fondova.finance.api.model.category.CategoriesResponse;
import com.fondova.finance.api.model.category.Category;
import com.fondova.finance.api.model.symbol.SymbolSearchResponse;
import com.fondova.finance.persistance.AppStorage;
import com.fondova.finance.repo.CategoriesRepository;
import com.fondova.finance.repo.QuotesRepository;
import com.fondova.finance.repo.TextsRepository;
import com.fondova.finance.repo.ValuesRepository;

import java.util.List;

import javax.inject.Inject;

public class AddSymbolsViewModel extends ViewModel {

    // ---------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------
    @Inject QuotesRepository quotesRepository;
    @Inject CategoriesRepository categoriesRepository;
    @Inject ValuesRepository valuesRepository;
    @Inject TextsRepository textsRepository;
    @Inject
    AppStorage appStorage;


    // ---------------------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------------------
    public AddSymbolsViewModel() {
        App.getAppComponent().inject(this);
    }


    // ---------------------------------------------------------------------------------
    // Public
    // ---------------------------------------------------------------------------------
    int getQuotesCount() {
        return quotesRepository.getQuotesCount();
    }

    MutableLiveData<Resource<CategoriesResponse>> getAllCategories() {
        return categoriesRepository.getAllCategories();
    }

    MutableLiveData<Resource<SymbolSearchResponse>> getSymbolsForQuery(String latestSearchQuery) {
        return categoriesRepository.getSymbolsForQuery(latestSearchQuery);
    }

    MutableLiveData<Resource<CategoriesResponse>> getSubCategories(List<Category> categoriesHistory) {
        return categoriesRepository.getSubCategories(categoriesHistory);
    }

    MutableLiveData<Resource<CategoriesResponse>> getSymbolsIntoSubCategory(List<Category> categoriesHistory,
                                                                            String query) {
        return categoriesRepository.getSymbolsIntoSubCategory(categoriesHistory, query);
    }

    void stopRestfulRequest() {
        categoriesRepository.stopRestfulRequest();
    }

}
