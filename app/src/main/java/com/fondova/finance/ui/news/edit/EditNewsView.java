package com.fondova.finance.ui.news.edit;


import android.support.annotation.StringRes;

import com.fondova.finance.ui.BaseView;
import com.fondova.finance.vo.NewsCategory;

import java.util.List;

interface EditNewsView extends BaseView {

    void setupViews();

    void updateToolbarTitle(@StringRes int titleId);

    void showLoading(boolean show);

    void setCategories(List<NewsCategory> categories);

    void hideItem(int position);

    List<NewsCategory> getCategoriesFromAdapter();

    void finishActivity();

    void showDeletedCategorySnackbar(@StringRes int message, NewsCategory newsCategory, int position);

    void addItem(NewsCategory category, int position);

    void updateItem(NewsCategory category, int position);
}

