package com.fondova.finance.ui.about;

import android.support.annotation.StringRes;

interface HtmlView {
    void setHtmlFile(String filename);
    void loadUrl(String url);
    void addBackArrow();
    void updateToolbarTitle(@StringRes int titleId);

    void showButton(boolean show);
    void setButtonText(@StringRes int text);

    boolean shouldGoToMainScreen();
    boolean shouldShowAcceptEulaBtn();

    int getFileType();

    void goToMainScreen();

}