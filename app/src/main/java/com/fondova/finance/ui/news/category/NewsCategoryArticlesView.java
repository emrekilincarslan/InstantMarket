package com.fondova.finance.ui.news.category;

import android.support.annotation.NonNull;

import com.fondova.finance.api.model.news.CategoryArticle;
import com.fondova.finance.ui.BaseView;
import com.fondova.finance.vo.NewsCategory;

import java.util.List;

interface NewsCategoryArticlesView extends BaseView {
    // ---------------------------------------------------------------------------------------------
    // NewsCategoryArticlesView View
    // ---------------------------------------------------------------------------------------------
    void setupViews();

    void setArticles(@NonNull List<CategoryArticle> categoryArticles);

    void showLoadingContent(boolean show);

    NewsCategory getCategory();

    List<CategoryArticle> getCurrentArticles();

    void showArticleDetails(@NonNull final String storyId, @NonNull final String title, NewsCategory category);

    void showEmptyLayout(boolean show);

    void showMessage(String title, String message);
}
