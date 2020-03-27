package com.fondova.finance.ui.main;

import android.support.annotation.IntDef;
import android.support.annotation.IntRange;

import static com.fondova.finance.ui.main.MainState.BottomBarSelection.POSITION_NEWS;
import static com.fondova.finance.ui.main.MainState.BottomBarSelection.POSITION_QUOTES;
import static com.fondova.finance.ui.main.MainState.BottomBarSelection.POSITION_SETTINGS;

public class MainState {

    static final String KEY_SELECTED_POSITION = "key_selected_position";

    @IntDef({POSITION_QUOTES, POSITION_NEWS, POSITION_SETTINGS})
    @interface BottomBarSelection {
        int POSITION_QUOTES = 0;
        int POSITION_NEWS = 1;
        int POSITION_SETTINGS = 2;
    }

    private final int selectedPosition;

    MainState(@IntRange(from = POSITION_QUOTES, to = POSITION_SETTINGS) final int selectedPos) {
        this.selectedPosition = selectedPos;
    }

    @BottomBarSelection
    int getSelectedPosition() {
        return selectedPosition;
    }

    boolean isSamePosition(@IntRange(from = POSITION_QUOTES, to = POSITION_SETTINGS) final int newPos) {
        return selectedPosition == newPos;
    }
}
