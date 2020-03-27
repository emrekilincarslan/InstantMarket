package com.fondova.finance.ui.news.view;


import android.arch.lifecycle.ViewModel;

import com.fondova.finance.App;
import com.fondova.finance.api.OnLoadedListener;
import com.fondova.finance.repo.NewsRepository;

import javax.inject.Inject;

public class ViewArticleViewModel extends ViewModel {

    @Inject NewsRepository newsRepository;

    // ---------------------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------------------
    public ViewArticleViewModel() {
        App.getAppComponent().inject(this);
    }


    void getArticle(String storyId, OnLoadedListener listener) {
        newsRepository.getArticle(storyId, listener);
    }
}
