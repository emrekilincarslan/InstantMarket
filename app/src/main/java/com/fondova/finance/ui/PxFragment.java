package com.fondova.finance.ui;


import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.LifecycleObserver;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fondova.finance.App;
import com.fondova.finance.di.components.AppComponent;

import butterknife.ButterKnife;

public abstract class PxFragment extends LifecycleFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(layoutId(), container, false);
        ButterKnife.bind(this, view);
        doInject(App.getAppComponent());
        addLifecycleObserver();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    protected void onConnectionLost() {
    }


    @LayoutRes
    protected abstract int layoutId();

    protected LifecycleObserver getLifecycleObserver() {
        return null;
    }

    protected void doInject(AppComponent component) {
    }

    private void addLifecycleObserver() {
        LifecycleObserver lifecycleObserver = getLifecycleObserver();
        if (lifecycleObserver != null) {
            getLifecycle().addObserver(lifecycleObserver);
        }
    }


}
