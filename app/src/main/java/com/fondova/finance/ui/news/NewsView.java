package com.fondova.finance.ui.news;

import android.support.annotation.StringRes;

import com.fondova.finance.workspace.WorkspaceQuote;
import com.fondova.finance.ui.BaseView;
import com.fondova.finance.vo.NewsCategory;

import java.util.List;

interface NewsView extends BaseView {

    void setupViews(boolean showHomeAsUpEnabled);

    void setNewsCategoriesAndTheirPosition(final List<NewsCategory> newsCategories);

    void goToEditNewsScreen();

    void showEmptyView(boolean show);

    void addNewsCategory();

    boolean hasQuoteAndValue();

    WorkspaceQuote getQuote();

    String getQuoteShortDescription();

    void showConnectionError(@StringRes int errorRes);
}
