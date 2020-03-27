package com.fondova.finance.ui.news;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ViewModelProviders;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;

import com.fondova.finance.AppExecutors;
import com.fondova.finance.R;
import com.fondova.finance.api.session.SessionService;
import com.fondova.finance.api.session.SessionStatus;
import com.fondova.finance.workspace.WorkspaceQuote;
import com.fondova.finance.sync.SyncManager;
import com.fondova.finance.ui.BaseUseCase;
import com.fondova.finance.util.NewsCategoryQueryBuilder;
import com.fondova.finance.vo.NewsCategory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class NewsUseCase extends BaseUseCase<NewsView> {

    // ---------------------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------------------
    private final AppExecutors appExecutors;
    private SyncManager syncManager;
    private NewsCategoryQueryBuilder newsCategoryQueryBuilder;
    private NewsViewModel model;
    private NewsCategory quoteRelatedNewsCategory;
    private boolean isTempNewsCategoryAdded;
    private SessionService sessionService;

    @Inject NewsUseCase(AppExecutors appExecutors,
                        NewsCategoryQueryBuilder newsCategoryQueryBuilder,
                        SessionService sessionService,
                        SyncManager syncManager) {
        this.appExecutors = appExecutors;
        this.newsCategoryQueryBuilder = newsCategoryQueryBuilder;
        this.syncManager = syncManager;
        this.sessionService = sessionService;
    }

    // ---------------------------------------------------------------------------------------------
    // LifeCycle
    // ---------------------------------------------------------------------------------------------
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    void setup() {
        view.setupViews(view.hasQuoteAndValue());

        model = ViewModelProviders.of((Fragment) source).get(NewsViewModel.class);
        isTempNewsCategoryAdded = false;

        checkForNetworkOperationAllowed();

        appExecutors.dataThread().execute(() -> {
            if (view.hasQuoteAndValue()) {
                createQuoteRelatedCategory();
            }
            loadCategories();
        });
    }

    @Override
    protected void onBaseCreate(LifecycleOwner source) {
        super.onBaseCreate(source);
        sessionService.getSessionStatusLiveData().observe(source, this::sessionStatusChanged);
    }

    private void loadCategories() {
        if (source != null) {
            appExecutors.mainThread().execute(
                    () -> {
                        final LiveData<List<NewsCategory>> categories = model.getCategoriesLiveData();
                        if (source == null) return;
                        categories.observe(source, this::onNewsCategoriesUpdated);
                    });
        }
    }


    // ---------------------------------------------------------------------------------------------
    // Public
    // ---------------------------------------------------------------------------------------------
    void onAddClicked() {
        if (checkForNetworkOperationAllowed()) {
            view.addNewsCategory();
        }
    }

    void onEditClicked() {
        view.goToEditNewsScreen();
    }

    void onAddedCategory(String name, String keywords, boolean selectedAND) {
        String query = newsCategoryQueryBuilder.makeQuery(keywords, selectedAND);

        incrementTheOrderOfPreviousCategories();

        NewsCategory category = new NewsCategory();
        category.name = name;
        category.query = query;
        category.keywords = keywords;
        category.order = 0;

        model.saveCategory(category);

        syncManager.uploadLocalData();
    }

    // ---------------------------------------------------------------------------------------------
    // Private
    // ---------------------------------------------------------------------------------------------
    private void createQuoteRelatedCategory() {
        WorkspaceQuote quote = view.getQuote();
        quoteRelatedNewsCategory = new NewsCategory();
        quoteRelatedNewsCategory.name = view.getQuoteShortDescription();
        quoteRelatedNewsCategory.order = PagerAdapter.POSITION_NONE;
        quoteRelatedNewsCategory.query = quote.getValue();
        quoteRelatedNewsCategory.isQuoteRelated = true;
    }

    private void incrementTheOrderOfPreviousCategories() {
        List<NewsCategory> categories = model.getCategories();
        for (NewsCategory category : categories) {
            category.order += 1;
        }

        model.updateCategories(categories);
    }

    private void onNewsCategoriesUpdated(List<NewsCategory> newsCategories) {
        List<NewsCategory> copy = new ArrayList<>(newsCategories);
        if (view.hasQuoteAndValue()) {
            copy.add(0, quoteRelatedNewsCategory);
            isTempNewsCategoryAdded = true;
        }
        appExecutors.mainThread().execute(() -> {
            if (view == null) return;
            view.showEmptyView(!areNewsCategoriesAvailable(copy));
            view.setNewsCategoriesAndTheirPosition(copy);
        });
    }

    private boolean areNewsCategoriesAvailable(List<NewsCategory> newsCategoryList) {
        return newsCategoryList != null && !newsCategoryList.isEmpty();
    }

    private boolean checkForNetworkOperationAllowed() {
        if (sessionService.getSessionStatusLiveData().getValue() != SessionStatus.connected) {
            view.showConnectionError(R.string.not_connected_to_server_message);
            return false;
        }
        return true;
    }

    private void sessionStatusChanged(SessionStatus sessionStatus) {
        switch (sessionStatus) {
            case connected:
                // do nothing
                break;
            case connecting:
                // do nothing
                break;
            default:
                if (view != null) checkForNetworkOperationAllowed();
                break;
        }
    }

}
