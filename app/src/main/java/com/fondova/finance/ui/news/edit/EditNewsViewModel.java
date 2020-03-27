package com.fondova.finance.ui.news.edit;


import android.arch.lifecycle.ViewModel;

import com.fondova.finance.App;
import com.fondova.finance.repo.NewsRepository;
import com.fondova.finance.vo.NewsCategory;

import java.util.List;

import javax.inject.Inject;

public class EditNewsViewModel extends ViewModel {

    @Inject NewsRepository newsRepository;

    // ---------------------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------------------
    public EditNewsViewModel() {
        App.getAppComponent().inject(this);
    }


    List<NewsCategory> getCategories() {
        return newsRepository.getNewsCategories();
    }

    void saveCategories(List<NewsCategory> categories) {
        newsRepository.updateCategories(categories);
    }

    void removeCategories(List<NewsCategory> categories) {
        newsRepository.removeCategories(categories);
    }

    void updateCategory(NewsCategory category) {
        newsRepository.insertCategory(category);
    }
}
