package com.fondova.finance.ui.news.view;


import android.support.annotation.StringRes;

import com.fondova.finance.ui.BaseView;
import com.fondova.finance.vo.NewsCategory;

interface ViewArticleView extends BaseView {

    void updateToolbarTitle(@StringRes int titleId);

    void setupViews(String title);

    String getStoryId();

    String getArticleTitle();

    NewsCategory getCategory();

    boolean hasCategory();

    void showLoading(boolean show);

    void loadHtml(String html);
}
