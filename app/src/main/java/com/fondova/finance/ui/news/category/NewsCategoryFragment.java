package com.fondova.finance.ui.news.category;

import android.arch.lifecycle.LifecycleObserver;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.fondova.finance.R;
import com.fondova.finance.api.model.news.CategoryArticle;
import com.fondova.finance.di.components.AppComponent;
import com.fondova.finance.ui.PxFragment;
import com.fondova.finance.ui.news.view.ViewArticleActivity;
import com.fondova.finance.ui.util.DialogUtil;
import com.fondova.finance.vo.NewsCategory;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;

public class NewsCategoryFragment extends PxFragment implements NewsCategoryArticlesView {
    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------
    public static final String TAG = NewsCategoryFragment.class.getSimpleName();

    public static final String ARG_CATEGORY = "arg_category";
    // ---------------------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------------------
    @BindView(R.id.rv_list) RecyclerView rvArticles;
    @BindView(R.id.cl_progress_bar) ContentLoadingProgressBar clProgressBar;
    @BindView(R.id.tv_empty) TextView tvEmpty;
    @Inject NewsCategoryArticlesAdapter adapter;
    @Inject NewsCategoryUseCase useCase;
    @Inject DialogUtil dialogUtil;

    // ---------------------------------------------------------------------------------------------
    // Override PxFragment
    // ---------------------------------------------------------------------------------------------
    @Override
    protected int layoutId() {
        return R.layout.fragment_news_category;
    }

    @Override
    protected void doInject(AppComponent component) {
        component.inject(NewsCategoryFragment.this);
    }

    @Override
    protected LifecycleObserver getLifecycleObserver() {
        return useCase;
    }


    // ---------------------------------------------------------------------------------------------
    // NewsCategoryArticlesView View
    // ---------------------------------------------------------------------------------------------
    @Override
    public void setupViews() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());


        rvArticles.setHasFixedSize(true);
        rvArticles.setLayoutManager(layoutManager);
        rvArticles.setAdapter(adapter);
        adapter.setOnQuoteClickListener((storyId, title) -> useCase.onRowClicked(storyId, title));

    }


    @Override
    public void setArticles(@NonNull List<CategoryArticle> categoryArticles) {
        adapter.setArticles(categoryArticles);
    }

    @Override
    public void showLoadingContent(boolean show) {
        if (show) {
            clProgressBar.show();
        } else {
            clProgressBar.hide();
        }
    }

    @Override
    public NewsCategory getCategory() {
        return getArguments().getParcelable(ARG_CATEGORY);
    }

    @Override
    public List<CategoryArticle> getCurrentArticles() {
        return adapter.getArticles();
    }

    @Override
    public void showArticleDetails(@NonNull String storyId, @NonNull String title,
                                   NewsCategory category) {
        ViewArticleActivity.start(getActivity(), storyId, title, category);
    }

    @Override
    public void showEmptyLayout(boolean show) {
        tvEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showMessage(String title, String message) {
        dialogUtil.showMessage(getContext(), title, message);
    }

    // ---------------------------------------------------------------------------------------------
    // New instance
    // ---------------------------------------------------------------------------------------------
    private static NewsCategoryFragment newInstance(@NonNull final Bundle args) {
        final NewsCategoryFragment fragment = new NewsCategoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static NewsCategoryFragment newInstance(NewsCategory category) {
        final Bundle args = new Bundle();
        args.putParcelable(ARG_CATEGORY, category);
        return newInstance(args);
    }
}
