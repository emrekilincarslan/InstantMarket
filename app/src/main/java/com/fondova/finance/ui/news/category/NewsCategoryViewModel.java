package com.fondova.finance.ui.news.category;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.fondova.finance.App;
import com.fondova.finance.api.Resource;
import com.fondova.finance.api.model.news.NewsWatchResponse;
import com.fondova.finance.repo.NewsRepository;
import com.fondova.finance.vo.NewsCategory;

import javax.inject.Inject;

public class NewsCategoryViewModel extends ViewModel {
    // ---------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------
    @Inject NewsRepository newsRepository;
    private LiveData<Resource<NewsWatchResponse>> categoryArticlesLiveData;

    // ---------------------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------------------
    public NewsCategoryViewModel() {
        App.getAppComponent().inject(NewsCategoryViewModel.this);
    }

    // ---------------------------------------------------------------------------------
    // Public/Package Private
    // ---------------------------------------------------------------------------------
    LiveData<Resource<NewsWatchResponse>> getCategoryArticles(@NonNull final NewsCategory newsCategory) {
        if (categoryArticlesLiveData == null) {
            categoryArticlesLiveData = newsRepository.getNewsCategoryArticlesLiveData(newsCategory);
        }
        return categoryArticlesLiveData;
    }

    void stopListeningForUpdates(long categoryId) {
        newsRepository.clearQuoteRelatedArticlesFull();
        newsRepository.unwatch(categoryId);
    }

    void cancelRequest(long categoryId) {
        newsRepository.clearRequest(categoryId);
    }
}
