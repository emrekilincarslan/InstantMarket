package com.fondova.finance.ui.news;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.fondova.finance.App;
import com.fondova.finance.repo.NewsRepository;
import com.fondova.finance.vo.NewsCategory;

import java.util.List;

import javax.inject.Inject;

public class NewsViewModel extends ViewModel {

    // ---------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------
    @Inject NewsRepository newsRepository;
    private LiveData<List<NewsCategory>> newsCategoryLiveData;

    // ---------------------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------------------
    public NewsViewModel() {
        App.getAppComponent().inject(NewsViewModel.this);
    }

    // ---------------------------------------------------------------------------------
    // Public/Package Private
    // ---------------------------------------------------------------------------------
    LiveData<List<NewsCategory>> getCategoriesLiveData() {
        if (newsCategoryLiveData == null) {
            newsCategoryLiveData = newsRepository.getNewsCategoriesLiveData();
        }
        return newsCategoryLiveData;
    }

    List<NewsCategory> getCategories() {
        return newsRepository.getNewsCategories();
    }

    void updateCategories(List<NewsCategory> categories) {
        newsRepository.updateCategories(categories);
    }

    void saveCategory(NewsCategory category) {
        newsRepository.insertCategory(category);
    }
}
