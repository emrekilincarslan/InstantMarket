package com.fondova.finance.ui.news.category;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.fondova.finance.AppExecutors;
import com.fondova.finance.api.Resource;
import com.fondova.finance.api.model.news.CategoryArticle;
import com.fondova.finance.api.model.news.NewsData;
import com.fondova.finance.api.model.news.NewsWatchResponse;
import com.fondova.finance.api.session.SessionService;
import com.fondova.finance.api.session.SessionStatus;
import com.fondova.finance.enums.RefreshType;
import com.fondova.finance.ui.BaseUseCase;
import com.fondova.finance.ui.util.DateFormatUtil;
import com.fondova.finance.vo.NewsCategory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class NewsCategoryUseCase extends BaseUseCase<NewsCategoryArticlesView> {
    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------
    private static final int MAX_ARTICLES = NewsData.DEFAULT_LIMIT * 2;


    // ---------------------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------------------
    private final AppExecutors appExecutors;
    private NewsCategoryViewModel model;
    private NewsCategory newsCategory;
    private final SessionService sessionService;

    @Inject NewsCategoryUseCase(AppExecutors appExecutors,  SessionService sessionService) {
        this.appExecutors = appExecutors;
        this.sessionService = sessionService;
    }


    @Override
    protected void onBaseCreate(LifecycleOwner source) {
        super.onBaseCreate(source);
        sessionService.getSessionStatusLiveData().observe(source, this::sessionStatusChanged);
    }

    // ---------------------------------------------------------------------------------------------
    // LifeCycle
    // ---------------------------------------------------------------------------------------------
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    void setup() {
        model = ViewModelProviders.of((Fragment) source).get(NewsCategoryViewModel.class);

        view.setupViews();
        newsCategory = view.getCategory();

        loadArticles();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void onDestroy() {
        if (newsCategory != null)
            model.stopListeningForUpdates(newsCategory.id);
    }

    // ---------------------------------------------------------------------------------------------
    // Public
    // ---------------------------------------------------------------------------------------------
    void onRowClicked(String storyId, String title) {
        view.showArticleDetails(storyId, title, newsCategory);
    }

    // ---------------------------------------------------------------------------------------------
    // Private
    // ---------------------------------------------------------------------------------------------
    private void loadArticles() {
        if (checkForNetworkOperationAllowed()) {
            view.showLoadingContent(true);
            final LiveData<Resource<NewsWatchResponse>> categoryArticlesLiveData = model.getCategoryArticles(newsCategory);

            if (categoryArticlesLiveData != null && source != null) {
                appExecutors.mainThread().execute(() -> {
                    if (categoryArticlesLiveData.hasActiveObservers()) {
                        categoryArticlesLiveData.removeObservers(source);
                    }

                    if(source == null) return;

                    categoryArticlesLiveData.observe(source, (newsWatchResponse) -> {
                        if (newsWatchResponse != null) {
                            switch (newsWatchResponse.status) {
                                case SUCCESS:
                                    onNewArticlesLoaded(newsWatchResponse);
                                    break;
                                case ERROR:
                                    view.showEmptyLayout(true);
                                    view.showLoadingContent(false);
                                    view.showMessage(newsWatchResponse.title, newsWatchResponse.message);
                                    break;
                                case LOADING:
                                    view.showLoadingContent(true);
                                    break;
                            }
                        }
                    });
                });
            }
        }
    }

    private void onNewArticlesLoaded(@Nullable Resource<NewsWatchResponse> newsWatchResponse) {
        appExecutors.dataThread().execute(() -> {
            if (view == null || newsWatchResponse == null) return;

            final List<CategoryArticle> result = new ArrayList<>(view.getCurrentArticles());

            for (CategoryArticle article : newsWatchResponse.data.data) {

                article.datetime = DateFormatUtil.serverDateStringToUiString(article.datetime);
                if (RefreshType.REAL_TIME.equalsIgnoreCase(article.refreshType)) {
                    //insert the article at the begging
                    result.add(0, article);
                } else {
                    result.add(article);
                }
            }

            while (MAX_ARTICLES < result.size()) {
                result.remove(result.size() - 1);
            }

            appExecutors.mainThread().execute(() -> {
                if (view == null) {
                    return;
                }
                view.showLoadingContent(false);
                view.setArticles(result);
                view.showEmptyLayout(result.isEmpty());
            });
        });

    }

    private boolean checkForNetworkOperationAllowed() {
        if (sessionService.getSessionStatusLiveData().getValue() != SessionStatus.connected) {
            onConnectionError();
            return false;
        }

        return true;

    }

    private void onConnectionError() {
        view.showLoadingContent(false);
        view.showEmptyLayout(true);
    }

    private void sessionStatusChanged(SessionStatus sessionStatus) {

        switch (sessionStatus) {
            case connected:
                loadArticles();
                break;

            case connecting:
                break;

            default:
                model.cancelRequest(newsCategory.id);
                break;
        }

    }

}
