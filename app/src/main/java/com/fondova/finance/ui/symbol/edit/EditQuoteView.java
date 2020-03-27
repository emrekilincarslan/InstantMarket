package com.fondova.finance.ui.symbol.edit;


import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import com.fondova.finance.sync.QuoteSyncItem;
import com.fondova.finance.ui.BaseView;

interface EditQuoteView extends BaseView {

    void updateToolbarTitle(@StringRes int titleId);

    void updateToolbarSubTitle(String subtitleText);

    void showConnectivityIcon(@DrawableRes int iconRes);

    void hideConnectivityIcon();

    void setupViews();

    void finishActivity();

    void showSnackbarAfterDeletion(QuoteSyncItem quote, int position);

    void removeItemFromAdapter(int position);

    void addItemToAdapter(QuoteSyncItem quote, int position);

    void showLoading(boolean show);

    void showLoadingContent(boolean show);

    void forceLogout();
}
