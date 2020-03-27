package com.fondova.finance.ui.news.edit;


import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.support.v4.app.FragmentActivity;

import com.fondova.finance.AppExecutors;
import com.fondova.finance.R;
import com.fondova.finance.sync.SyncManager;
import com.fondova.finance.ui.BaseUseCase;
import com.fondova.finance.ui.util.DialogUtil;
import com.fondova.finance.vo.NewsCategory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class EditNewsUseCase extends BaseUseCase<EditNewsView> {

    // ---------------------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------------------
    private EditNewsViewModel model;
    private AppExecutors appExecutors;
    private SyncManager syncManager;
    private List<NewsCategory> forDeleting = new ArrayList<>();

    @Inject
    DialogUtil dialogUtil;

    @Inject
    EditNewsUseCase(AppExecutors appExecutors, SyncManager syncManager) {
        this.appExecutors = appExecutors;
        this.syncManager = syncManager;
    }

    // ---------------------------------------------------------------------------------------------
    // LifeCycle
    // ---------------------------------------------------------------------------------------------
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    void setup() {
        model = ViewModelProviders.of((FragmentActivity) source).get(EditNewsViewModel.class);

        view.setupViews();
        view.showLoading(true);

        List<NewsCategory> categories = model.getCategories();

        if (view == null) return;
        view.setCategories(categories);
        view.showLoading(false);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void onResume() {
        view.updateToolbarTitle(R.string.edit_news);
    }

    void onDeleteClick(int position, NewsCategory category) {
        if (!forDeleting.contains(category)) {
            forDeleting.add(category);
        }
        view.hideItem(position);
        view.showDeletedCategorySnackbar(R.string.category_has_been_deleted, category, position);
    }

    void undoDeleteCategory(NewsCategory category, int position) {
        forDeleting.remove(category);

        view.addItem(category, position);
    }

    void onDoneClicked() {
        if (view != null) {
            view.showLoading(true);
        }

        List<NewsCategory> orderedCategories = new ArrayList<>();

        if (view == null) return;
        // calculate order
        List<NewsCategory> categories = view.getCategoriesFromAdapter();
        for (int i = 0; i < categories.size(); i++) {
            NewsCategory category = categories.get(i);

            category.order = i;
            orderedCategories.add(category);
        }

        if (model == null) return;
        model.saveCategories(orderedCategories);
        model.removeCategories(forDeleting);

        syncManager.uploadLocalData();

        if (source == null) return;
        forDeleting.clear();
        view.showLoading(false);
        view.finishActivity();

    }

    void onEditClicked(Context context, int position, NewsCategory category) {
        dialogUtil.editNewsCategory(context, category,
                category1 -> view.updateItem(category1, position));
    }

    // ---------------------------------------------------------------------------------------------
    // Private
    // ---------------------------------------------------------------------------------------------

}
