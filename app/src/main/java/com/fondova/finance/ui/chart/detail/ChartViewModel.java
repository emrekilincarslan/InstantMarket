package com.fondova.finance.ui.chart.detail;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.fondova.finance.App;
import com.fondova.finance.api.Resource;
import com.fondova.finance.api.model.chart.ChartWatchResponse;
import com.fondova.finance.api.model.news.NewsWatchResponse;
import com.fondova.finance.api.model.quote.OldQuoteWatchResponse;
import com.fondova.finance.api.model.quote.QuoteWatchResponse;
import com.fondova.finance.workspace.WorkspaceQuote;
import com.fondova.finance.repo.ChartDataRepository;
import com.fondova.finance.repo.NewsRepository;
import com.fondova.finance.repo.QuoteWatchRepository;
import com.fondova.finance.repo.QuotesRepository;
import com.fondova.finance.repo.ValuesRepository;
import com.fondova.finance.vo.ChartData;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ChartViewModel extends ViewModel {

    // ---------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------
    @Inject NewsRepository newsRepository;
    private LiveData<Resource<NewsWatchResponse>> categoryArticlesLiveData;
    @Inject ValuesRepository valuesRepository;
    @Inject ChartDataRepository chartDataRepository;
    @Inject QuotesRepository quotesRepository;
    @Inject
    QuoteWatchRepository quoteWatchRepository;


    // ---------------------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------------------
    public ChartViewModel() {
        App.getAppComponent().inject(this);
    }


    LiveData<Resource<ChartWatchResponse>> snapChartDataForQuote(WorkspaceQuote quote, int interval) {
        return chartDataRepository.snapChartDataForQuote(quote, interval);
    }

    LiveData<Resource<ChartWatchResponse>> watchChartDataForQuote(WorkspaceQuote quote, int interval) {
        return chartDataRepository.watchDataForQuote(quote, interval);
    }

    LiveData<QuoteWatchResponse> getQuoteWithLatestValue(String key) {
        return quoteWatchRepository.subscribe(key);
    }

    void deleteChartData() {
        chartDataRepository.deleteCachedChartData();
    }

    LiveData<Resource<OldQuoteWatchResponse>> stopChartDataReceiving(String quoteId, @ChartInterval int interval) {
        chartDataRepository.deleteCachedChartData();
        return chartDataRepository.unwatchChartData(quoteId, interval);
    }

    Resource<ChartWatchResponse> getLoadedChartData() {
        return chartDataRepository.getLoadedChartData();
    }

    LiveData<ChartData> getLatestLive() {
        return chartDataRepository.getLatestChartData();
    }

    LiveData<Resource<NewsWatchResponse>> getChartArticles(@NonNull final String symbol, int limit) {
        if (categoryArticlesLiveData == null) {
            categoryArticlesLiveData = newsRepository.getChartArticlesLiveData(symbol, limit);
        }
        return categoryArticlesLiveData;
    }

    void stopListeningForArticleUpdates(String query) {
        newsRepository.clearQuoteRelatedArticlesLimited();
        newsRepository.unwatch(query);
    }
}
