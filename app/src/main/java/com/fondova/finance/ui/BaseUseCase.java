package com.fondova.finance.ui;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;

public class BaseUseCase<T extends BaseView> implements LifecycleObserver {

    private static final String TAG = "BaseUseCase";

    protected T view;
    protected LifecycleOwner source;

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    protected void onBaseCreate(LifecycleOwner source) {
        view = (T) source;
        this.source = source;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    void onBaseStart() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    void onBaseStop() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void onBaseDestroy() {
        this.view = null;
        this.source = null;
    }

}
