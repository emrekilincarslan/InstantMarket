package com.fondova.finance.ui.main;

import android.support.annotation.StringRes;

import com.fondova.finance.ui.BaseView;

interface MainView extends BaseView {
    void setupMainScreen();

    void openSymbolsScreen();

    void openNewsScreen();

    void openSettingsScreen();

    void updateToolbarTitle(@StringRes int titleId);
}