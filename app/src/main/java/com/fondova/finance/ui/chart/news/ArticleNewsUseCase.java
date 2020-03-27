package com.fondova.finance.ui.chart.news;

import android.arch.lifecycle.LifecycleObserver;

import com.fondova.finance.ui.BaseUseCase;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class ArticleNewsUseCase extends BaseUseCase<ArticleNewsView> implements LifecycleObserver {

    @Inject

    public ArticleNewsUseCase() {
    }
}
