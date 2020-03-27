package com.fondova.finance.ui.main;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.NonNull;

import com.fondova.finance.R;
import com.fondova.finance.ui.BaseUseCase;

import javax.inject.Inject;

class MainUseCase extends BaseUseCase<MainView> implements LifecycleObserver {

    private MainState state;

    @Inject
    public MainUseCase() {
    }

    // ---------------------------------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------------------------------
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    void onCreate() {
        view.setupMainScreen();
    }


    // ---------------------------------------------------------------------------------------------
    // Public
    // ---------------------------------------------------------------------------------------------
    void onChartsTabClick() {
        if (updateState(MainState.BottomBarSelection.POSITION_QUOTES)) {
            showSymbolsScreen();
        }
    }

    void onNewsTabClick() {
        if (updateState(MainState.BottomBarSelection.POSITION_NEWS)) {
            showNewsScreen();
        }
    }

    void onSettingsTabClick() {
        if (updateState(MainState.BottomBarSelection.POSITION_SETTINGS)) {
            showSettingsScreen();
        }
    }

    public MainState getState() {
        return state;
    }

    void restoreState(@NonNull final MainState mainState) {
        state = mainState;
        restoreSelection();
    }

    // ---------------------------------------------------------------------------------------------
    // Private
    // ---------------------------------------------------------------------------------------------
    private boolean updateState(@MainState.BottomBarSelection int pos) {
        if (!state.isSamePosition(pos)) {
            state = new MainState(pos);
            return true;
        }
        return false;
    }

    private void restoreSelection() {
        if (view == null) return;

        state = state == null ? new MainState(MainState.BottomBarSelection.POSITION_QUOTES) : state;

        switch (state.getSelectedPosition()) {
            case MainState.BottomBarSelection.POSITION_NEWS:
                showNewsScreen();
                break;
            case MainState.BottomBarSelection.POSITION_QUOTES:
                showSymbolsScreen();
                break;
            case MainState.BottomBarSelection.POSITION_SETTINGS:
                showSettingsScreen();
                break;
        }
    }

    private void showSettingsScreen() {
        view.openSettingsScreen();
    }

    private void showNewsScreen() {
        view.openNewsScreen();
        view.updateToolbarTitle(R.string.news);
    }

    private void showSymbolsScreen() {
        view.openSymbolsScreen();
    }
}
