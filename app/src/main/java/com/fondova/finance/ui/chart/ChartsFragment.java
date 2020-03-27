package com.fondova.finance.ui.chart;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;


public class ChartsFragment extends Fragment {

    public static ChartsFragment newInstance() {
        Bundle args = new Bundle();

        ChartsFragment fragment = new ChartsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
}
