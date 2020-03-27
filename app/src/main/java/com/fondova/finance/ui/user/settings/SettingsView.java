package com.fondova.finance.ui.user.settings;


import android.content.Intent;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.fondova.finance.ui.BaseView;

interface SettingsView extends BaseView {
    void uncheckAll();

    void goToAbout();

    void goToEula();

    void goToPrivacyPolicy(@ColorRes int ctColor, @DrawableRes int backIcon, @NonNull final String url);

    void openUrl(String url);

    void openEmailIntent(Intent intent);

    void updateToolbarTitle(@StringRes int titleId);

    void goToLoginScreen();

    void setAboutVersion(String version);

    void showError(String title, String msg);

    void showError(@StringRes int title, @StringRes int msg);

    void mark5SecondsCheckbox(boolean checked, boolean skipAnimation);

    void mark30SecondsCheckbox(boolean checked, boolean skipAnimation);

    void mark60SecondsCheckbox(boolean checked, boolean skipAnimation);

    void mark5MinutesCheckbox(boolean checked, boolean skipAnimation);

    void markOffCheckbox(boolean checked, boolean skipAnimation);

    void populateConnectString(@StringRes int stringRes);

    void setDisconnectButtonEnabled(boolean enabled);

    void showConnectivityIcon(@DrawableRes int iconRes);

    void goToWelcome();

    void forceLogout();

    void showLoading();

    void hideLoading();
}
