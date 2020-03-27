package com.fondova.finance.ui.symbol;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import com.fondova.finance.workspace.WorkspaceQuote;
import com.fondova.finance.sync.QuoteSyncItem;
import com.fondova.finance.ui.BaseView;

import java.util.List;

interface QuoteView extends BaseView {
    void updateToolbarTitle(@StringRes int titleId);

    void updateToolbarSubTitle(String subtitleText);

    void updateLastUpdatedTime(String lastUpdated);

    void openAddSymbolsScreen(int highlightedQuotePosition);

    void setupViews();

    void showConnectivityIcon(@DrawableRes int iconRes);

    void showReachedQuotesLimitMessage(String title, String message);


    void goToEditQuotesScreen();

    void quoteDeletedSnackbar(int groupIndex, Integer quoteIndex, QuoteSyncItem quote);

    void showNotConnectedError(@StringRes int  title, @StringRes int message);

    @Deprecated
    List<QuoteSyncItem> getQuotes();

    void openChartScreen(WorkspaceQuote quoteAndValue);

    void showMessage(@StringRes int msgId);

    void forceLogout();



}
