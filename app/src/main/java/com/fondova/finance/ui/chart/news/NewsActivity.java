package com.fondova.finance.ui.chart.news;

import android.arch.lifecycle.LifecycleObserver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;

import com.fondova.finance.R;
import com.fondova.finance.di.components.AppComponent;
import com.fondova.finance.workspace.WorkspaceQuote;
import com.fondova.finance.sync.QuoteSyncItem;
import com.fondova.finance.ui.PxActivity;
import com.fondova.finance.ui.news.NewsFragment;

import javax.inject.Inject;


public class NewsActivity extends PxActivity implements ArticleNewsView {

    public static final String EXTRA_QUOTE = "extra_quote";
    public static final String EXTRA_QUOTE_SHORT_DESCRIPTION = "extra_quote_short_description";

    @Inject ArticleNewsUseCase useCase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Bundle extras = getIntent().getExtras();
        WorkspaceQuote quote = QuoteSyncItem.fromJson(extras.getString(EXTRA_QUOTE));
        transaction.replace(R.id.fragment_news, NewsFragment.newInstance(quote, extras.getString(EXTRA_QUOTE_SHORT_DESCRIPTION)), NewsFragment.TAG);
        transaction.commit();
    }

    @Override
    protected int layoutId() {
        return R.layout.activity_news;
    }

    @Override
    protected LifecycleObserver getLifecycleObserver() {
        return useCase;
    }

    @Override
    protected void doInject(AppComponent component) {
        component.inject(this);
    }


    public static void start(Context context, WorkspaceQuote quote, String quoteShortDescription) {
        Intent intent = new Intent(context, NewsActivity.class);
        intent.putExtra(EXTRA_QUOTE, QuoteSyncItem.fromWorkspaceQuote(quote).toJson());
        intent.putExtra(EXTRA_QUOTE_SHORT_DESCRIPTION, quoteShortDescription);
        context.startActivity(intent);
    }
}
