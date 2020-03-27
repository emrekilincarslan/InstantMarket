package com.fondova.finance.ui.news.view;


import android.arch.lifecycle.LifecycleObserver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.StringRes;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.webkit.WebView;
import android.widget.TextView;

import com.fondova.finance.R;
import com.fondova.finance.di.components.AppComponent;
import com.fondova.finance.ui.PxActivity;
import com.fondova.finance.vo.NewsCategory;

import javax.inject.Inject;

import butterknife.BindView;

public class ViewArticleActivity extends PxActivity implements ViewArticleView {

    // ---------------------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------------------
    private static final String STORY_ID = "storyId";
    private static final String TITLE = "title";
    private static final String CATEGORY = "category";


    @BindView(R.id.tv_title) TextView tvTitle;
    @BindView(R.id.wv_view) WebView wvView;
    @BindView(R.id.cl_loading) ContentLoadingProgressBar clLoading;

    @Inject ViewArticleUseCase useCase;


    // ---------------------------------------------------------------------------------------------
    // View
    // ---------------------------------------------------------------------------------------------

    @Override
    public void setupViews(String title) {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle.setText(getIntent().getStringExtra(TITLE));
        wvView.setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public String getStoryId() {
        return getIntent().getStringExtra(STORY_ID);
    }

    @Override
    public String getArticleTitle() {
        return getIntent().getStringExtra(TITLE);
    }

    @Override
    public void updateToolbarTitle(@StringRes int titleId) {
        toolbar.setTitle(titleId);
    }

    @Override
    public void showLoading(boolean show) {
        if (show) {
            clLoading.show();
        } else {
            clLoading.hide();
        }
    }

    @Override
    public void loadHtml(String html) {
        wvView.loadData(html, "text/html; charset=utf-8", "UTF-8");
    }

    @Override
    public NewsCategory getCategory() {
        return getIntent().getParcelableExtra(CATEGORY);
    }

    @Override
    public boolean hasCategory() {
        return getIntent().hasExtra(CATEGORY);
    }

    // ---------------------------------------------------------------------------------------------
    // Override
    // ---------------------------------------------------------------------------------------------
    @Override
    protected int layoutId() {
        return R.layout.activity_view_article;
    }

    @Override
    protected LifecycleObserver getLifecycleObserver() {
        return useCase;
    }

    @Override
    protected void doInject(AppComponent component) {
        component.inject(this);
    }

    // ---------------------------------------------------------------------------------------------
    // New
    // ---------------------------------------------------------------------------------------------
    public static void start(Context context, String storyId, String title) {
        Intent intent = new Intent(context, ViewArticleActivity.class);
        intent.putExtra(STORY_ID, storyId);
        intent.putExtra(TITLE, title);
        context.startActivity(intent);
    }

    public static void start(Context context, String storyId, String title, NewsCategory category) {
        Intent intent = new Intent(context, ViewArticleActivity.class);
        intent.putExtra(STORY_ID, storyId);
        intent.putExtra(TITLE, title);
        intent.putExtra(CATEGORY, category);
        context.startActivity(intent);
    }
}
