package com.fondova.finance.ui.symbol.add;

import android.support.annotation.StringRes;

import com.fondova.finance.api.model.category.Category;
import com.fondova.finance.ui.BaseView;
import com.fondova.finance.vo.Quote;

import java.util.List;

interface AddSymbolsView extends BaseView {

    void setupViews();

    void showLoading(boolean show);

    void showCategories(int symbolsResultCount, List<String> categoriesHistory, List<Category> categories);

    @Deprecated
    void showSymbols(List<Quote> quotes, List<String> categoriesHistory);

    void showAddedSymbolsCount(String countString);

    void showApiError(String title, String message);

    void showReachedSymbolsLimitMessage(String title, String message);

    void updateSearchHint(String hint);

    void showMessageInSnackbar(@StringRes int message);

    void closeScreen();

    void updateSearchText(String query);

    void updateSuggestions();

    void showNotConnectedError(@StringRes int title, @StringRes int message);

    void hideKeyboard();

    int getHighlightedPosition();
}
