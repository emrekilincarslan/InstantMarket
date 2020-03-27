package com.fondova.finance.di.components;

import com.fondova.finance.App;
import com.fondova.finance.charts.ChartView;
import com.fondova.finance.charts.ChartActivity;
import com.fondova.finance.di.modules.AppModule;
import com.fondova.finance.di.modules.DaoModule;
import com.fondova.finance.di.modules.NetworkModule;
import com.fondova.finance.di.modules.RepoModule;
import com.fondova.finance.diagnostics.DiagnosticsActivity;
import com.fondova.finance.news.InstantMarketNewsActivity;
import com.fondova.finance.quotes.QuoteListView;
import com.fondova.finance.quotes.WorkspaceSelector;
import com.fondova.finance.ui.PxActivity;
import com.fondova.finance.ui.SessionAwareActivity;
import com.fondova.finance.ui.about.HtmlActivity;
import com.fondova.finance.ui.chart.detail.ChartViewModel;
import com.fondova.finance.ui.chart.news.NewsActivity;
import com.fondova.finance.ui.main.MainActivity;
import com.fondova.finance.ui.news.NewsFragment;
import com.fondova.finance.ui.news.NewsViewModel;
import com.fondova.finance.ui.news.category.NewsCategoryFragment;
import com.fondova.finance.ui.news.category.NewsCategoryViewModel;
import com.fondova.finance.ui.news.edit.EditNewsActivity;
import com.fondova.finance.ui.news.edit.EditNewsViewModel;
import com.fondova.finance.ui.news.view.ViewArticleActivity;
import com.fondova.finance.ui.news.view.ViewArticleViewModel;
import com.fondova.finance.ui.symbol.QuoteFragment;
import com.fondova.finance.ui.symbol.QuoteViewModel;
import com.fondova.finance.ui.symbol.add.AddSymbolsActivity;
import com.fondova.finance.ui.symbol.add.AddSymbolsViewModel;
import com.fondova.finance.ui.symbol.edit.EditQuoteActivity;
import com.fondova.finance.ui.user.login.LoginActivity;
import com.fondova.finance.ui.user.settings.SettingsFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
        AppModule.class,
        NetworkModule.class,
        RepoModule.class,
        DaoModule.class
})
public interface AppComponent {
    App application();

    void inject(App target);

    // Activities

    void inject(PxActivity target);

    void inject(MainActivity target);

    void inject(LoginActivity target);

    void inject(HtmlActivity target);

    void inject(ViewArticleActivity target);

    void inject(EditNewsActivity target);

    void inject(NewsActivity target);

    void inject(ChartActivity target);

    void inject(SessionAwareActivity target);

    void inject(InstantMarketNewsActivity target);

    void inject(DiagnosticsActivity target);

    // Fragments

    void inject(SettingsFragment target);

    void inject(QuoteFragment target);

    void inject(AddSymbolsActivity target);

    void inject(EditQuoteActivity target);

    void inject(NewsCategoryFragment target);

    void inject(NewsFragment target);

    // ViewModels

    void inject(QuoteViewModel target);

    void inject(AddSymbolsViewModel target);

    void inject(ViewArticleViewModel target);

    void inject(NewsViewModel target);

    void inject(NewsCategoryViewModel target);

    void inject(EditNewsViewModel target);

    void inject(ChartViewModel target);

    // Views
    void inject(QuoteListView target);

    void inject(ChartView target);

    void inject(WorkspaceSelector target);
}