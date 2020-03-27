package com.fondova.finance.api.socket;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.fondova.finance.api.Resource;
import com.fondova.finance.api.chart.ChartService;
import com.fondova.finance.api.model.base.ApiErrorResponse;
import com.fondova.finance.api.model.base.BaseResponse;
import com.fondova.finance.api.model.base.MetaResponse;
import com.fondova.finance.api.model.chart.ChartSnapRequest;
import com.fondova.finance.api.model.chart.ChartWatchRequest;
import com.fondova.finance.api.model.chart.ChartWatchResponse;
import com.fondova.finance.api.model.news.NewsPageSnapRequest;
import com.fondova.finance.api.model.news.NewsPageSnapResponse;
import com.fondova.finance.api.model.news.NewsWatchRequest;
import com.fondova.finance.api.model.news.NewsWatchResponse;
import com.fondova.finance.api.model.quote.OldQuoteWatchResponse;
import com.fondova.finance.api.model.refresh_rate.RefreshRateRequest;
import com.fondova.finance.api.model.refresh_rate.RefreshRateResponse;
import com.fondova.finance.api.model.status.StatusResponse;
import com.fondova.finance.api.model.symbol.SymbolSearchRequest;
import com.fondova.finance.api.model.symbol.SymbolSearchResponse;
import com.fondova.finance.api.model.unwatch.UnwatchRequest;
import com.fondova.finance.api.news.NewsService;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;


/**
 * REST API access points
 */
@Singleton
public class ApiService implements WebsocketServiceListener {

    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------
    private static final String TAG = "ApiService";


    // ---------------------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------------------
    private final Gson gson;
    private final Gson searchResultDeserializer;

    private final Map<String, ApiRequestListener> listenersMap;
    private final Map<String, ApiRequestListener> listenersWatchMap;
    private final Map<String, ApiRequestListener> listenersQuoteUnWatchMap;
    private final Map<String, ApiRequestListener> listenersQuoteSnapMap;
    private final WebsocketService websocketService;
    private final ChartService chartService;
    private final NewsService newsService;

    // ---------------------------------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------------------------------
    @Inject
    public ApiService(Gson gson,
                      @Named("gsonSearchResultDeserializer") Gson searchResultDeserializer,
                      WebsocketService websocketService,
                      ChartService chartService,
                      NewsService newsService) {
        this.gson = gson;
        this.searchResultDeserializer = searchResultDeserializer;
        this.listenersMap = new HashMap<>();
        this.listenersWatchMap = new HashMap<>();
        this.listenersQuoteUnWatchMap = new HashMap<>();
        this.listenersQuoteSnapMap = new HashMap<>();
        this.websocketService = websocketService;
        this.chartService = chartService;
        this.newsService = newsService;
        websocketService.addListener(this);
    }

    // ---------------------------------------------------------------------------------------------
    // Public
    // ---------------------------------------------------------------------------------------------
    public void refreshRate(int refreshRate,
                            ApiRequestListener<Resource<RefreshRateResponse>> listener) {
        Log.i(TAG, "setting refreshRate: " + refreshRate);
        listenersMap.put(RefreshRateRequest.COMMAND_REFRESH_RATE, listener);
        send(generateRefreshRateRequestString(refreshRate));
    }


    public void unwatch(String reqId,
                        @Nullable ApiRequestListener<Resource<OldQuoteWatchResponse>> listener) {
        listenersMap.put(UnwatchRequest.COMMAND_UNWATCH + reqId, listener);
        send(generateUnwatchRequestString(reqId));
    }

    public void searchForSymbols(String query,
                                 ApiRequestListener<Resource<SymbolSearchResponse>> listener) {
        Log.i(TAG, "searchForSymbols: " + query);
        listenersMap.put(SymbolSearchRequest.COMMAND_SYMBOL_SEARCH, listener);
        send(generateSymbolSearchRequestString(query));
    }

    @Deprecated
    public void newsWatch(String reqId, String query, ApiRequestListener<Resource<NewsWatchResponse>> listener) {
        Log.i(TAG, "newsWatch: " + reqId);
        Log.d(TAG, "newsWatch: " + listener);
        listenersMap.put(NewsWatchRequest.COMMAND_NEWS_CATEGORY_WATCH + reqId, listener);


        if (!isDisconnected()) {
            send(generateNewsWatchRequestString(reqId, query));
        }
    }

    @Deprecated
    public void watchChartNews(String reqId, String symbol, int limit, ApiRequestListener<Resource<NewsWatchResponse>> listener) {
        Log.i(TAG, "watchChartNews: " + reqId);
        Log.d(TAG, "watchChartNews: " + listener);
        listenersMap.put(NewsWatchRequest.COMMAND_NEWS_CATEGORY_WATCH + reqId, listener);


        if (!isDisconnected()) {
            send(generateNewsWatchForChartsRequestString(reqId, symbol, limit));
        }
    }

    @Deprecated
    public void clearNewsWatch(String reqId) {
        listenersMap.remove(reqId);
    }

    @Deprecated
    public void newsPageSnap(String reqId, String storyId,
                             ApiRequestListener<Resource<NewsPageSnapResponse>> listener) {

        listenersMap.put(NewsPageSnapRequest.COMMAND_NEWS_PAGE_SNAP, listener);

        if (!isDisconnected()) {
            send(generateNewsPageSnapRequestString(reqId, storyId));
        }
    }

    @Deprecated
    public void chartSnap(String reqId, String expression, int intervalCount,
                          ApiRequestListener<Resource<ChartWatchResponse>> listener) {
        Log.i(TAG, "chartSnap: " + reqId);
        Log.d(TAG, "chartSnap: " + listener);
        listenersWatchMap.put(reqId, listener);
        listenersMap.put(ChartSnapRequest.COMMAND_CHART_SNAP, listener);

        if (hasEstablishedConnection()) {
            send(generateChartSnapRequestString(reqId, expression, intervalCount));
        }
    }

    @Deprecated
    public void chartWatch(String reqId, String expression, int intervalCount,
                           ApiRequestListener<Resource<ChartWatchResponse>> listener) {
        Log.i(TAG, "chartWatch: " + reqId);
        Log.d(TAG, "chartWatch: " + listener);
        listenersWatchMap.put(reqId, listener);
        listenersMap.put(ChartWatchRequest.COMMAND_CHART_WATCH, listener);

        if (hasEstablishedConnection()) {
            send(generateChartWatchRequestString(reqId, expression, intervalCount));
        }
    }

    private boolean hasEstablishedConnection() {
        return websocketService.hasWebsocket();
    }

    public boolean isDisconnected() {
        return !websocketService.isConnected();

    }

    // ---------------------------------------------------------------------------------------------
    // Private
    // ---------------------------------------------------------------------------------------------

    private void send(String message) {
        websocketService.sendMessage(message);
    }

    @Override
    public boolean handleMessage(String message) {
        if (chartService.handleMessage(message)) {
            return true;
        }
        if (newsService.handleMessage(message)) {
            return true;
        }
        BaseResponse baseResponse = gson.fromJson(message, BaseResponse.class);
        String command = baseResponse.meta.command;

        switch (command) {
            case RefreshRateRequest.COMMAND_REFRESH_RATE:
                handleRefreshRateResponse(message);
                break;
            case SymbolSearchRequest.COMMAND_SYMBOL_SEARCH:
                handleSymbolSearchResponse(message);
                break;
            case StatusResponse.COMMAND:
                handleStatusResponse(message);
                break;
            case UnwatchRequest.COMMAND_UNWATCH:
                handleUnwatchResponse(message, baseResponse.meta.requestId);
                break;
            case NewsPageSnapRequest.COMMAND_NEWS_PAGE_SNAP:
                handleNewsPageSnapResponse(message);
                break;
            case NewsWatchRequest.COMMAND_NEWS_CATEGORY_WATCH:
                handleNewsWatchResponse(message);
                break;
            case ChartSnapRequest.COMMAND_CHART_SNAP:
                handleChartSnapResponse(message);
                break;
            case ChartWatchRequest.COMMAND_CHART_WATCH:
                handleChartWatchResponse(message);
                break;
        }
        return true;
    }

    private boolean isResponseOk(String message) {
        BaseResponse baseResponse = gson.fromJson(message, BaseResponse.class);
        MetaResponse meta = baseResponse.meta;
        return (meta.status >= 200 && meta.status < 300);
    }


    private void notifyForRequestError(String message, String key) {
        ApiErrorResponse errorResponse = gson.fromJson(message, ApiErrorResponse.class);
        notifyListener(listenersMap.get(key), Resource.error(errorResponse.errors.get(0).code, errorResponse.errors.get(0).detail));
    }


    private void notifyListener(ApiRequestListener listener, Object result) {
        if (listener != null) listener.onResponse(result);
    }

    // ---------------------------------------------------------------------------------------------
    // Quote
    // ---------------------------------------------------------------------------------------------
    private String generateNewsWatchRequestString(String reqId, String query) {
        final NewsWatchRequest newsWatchRequest = NewsWatchRequest.create(reqId, query);
        return gson.toJson(newsWatchRequest);
    }

    private String generateNewsWatchForChartsRequestString(String reqId, String symbol, int limit) {
        final NewsWatchRequest newsWatchRequest = NewsWatchRequest.createForCharts(reqId, symbol, limit);
        return gson.toJson(newsWatchRequest);
    }

    private String generateNewsPageSnapRequestString(String reqId, String storyId) {
        return gson.toJson(NewsPageSnapRequest.create(reqId, storyId));
    }

    private void handleNewsPageSnapResponse(String message) {
        Log.i(TAG, "handleNewsPageSnapResponse: ");
        if (isResponseOk(message)) {
            notifyForNewsPageSnap(message);
        } else {
            notifyForRequestError(message, NewsPageSnapRequest.COMMAND_NEWS_PAGE_SNAP);
        }
    }

    private void handleNewsWatchResponse(String message) {
        Log.i(TAG, "handleNewsWatchResponse: ");
        if (isResponseOk(message)) {
            notifyForNewsWatched(message);
        } else {
            notifyForRequestError(message, NewsPageSnapRequest.COMMAND_NEWS_PAGE_SNAP);
        }
    }

    private void notifyForNewsPageSnap(String message) {
        NewsPageSnapResponse newsPageSnapResponse = gson.fromJson(message, NewsPageSnapResponse.class);
        Resource<NewsPageSnapResponse> result = Resource.success(newsPageSnapResponse);
        notifyListener(listenersMap.get(NewsPageSnapRequest.COMMAND_NEWS_PAGE_SNAP), result);
        listenersMap.remove(NewsPageSnapRequest.COMMAND_NEWS_PAGE_SNAP);
    }

    private void notifyForNewsWatched(String message) {
        NewsWatchResponse newsWatchResponse = gson.fromJson(message, NewsWatchResponse.class);
        Resource<NewsWatchResponse> result = Resource.success(newsWatchResponse);
        notifyListener(listenersMap.get(NewsWatchRequest.COMMAND_NEWS_CATEGORY_WATCH + newsWatchResponse.meta.requestId), result);
    }

    private String generateUnwatchRequestString(String reqId) {
        return gson.toJson(UnwatchRequest.create(reqId));
    }

    private void handleUnwatchResponse(String message, String requestId) {
        Log.i(TAG, "handleUnwatchResponse: ");
        if (isResponseOk(message)) {
            notifyForUnWatched(message);
        } else {
            notifyForRequestError(message, UnwatchRequest.COMMAND_UNWATCH + requestId);
        }
    }

    private void notifyForUnWatched(String message) {
        BaseResponse quoteUnwatchResponse = gson.fromJson(message, BaseResponse.class);
        Resource<BaseResponse> result = Resource.success(quoteUnwatchResponse);
        String key = quoteUnwatchResponse.meta.requestId;

        ApiRequestListener apiRequestListener = listenersQuoteUnWatchMap.containsKey(key) ? listenersQuoteUnWatchMap.get(key) : listenersMap.get(key);
        notifyListener(apiRequestListener, result);
        listenersQuoteUnWatchMap.remove(key);
        listenersMap.remove(key);
    }

    // ---------------------------------------------------------------------------------------------
    // Refresh rate
    // ---------------------------------------------------------------------------------------------
    private String generateRefreshRateRequestString(int refreshRate) {
        return gson.toJson(RefreshRateRequest.create(refreshRate));
    }

    private void handleRefreshRateResponse(String message) {
        if (isResponseOk(message)) {
            notifyForRefreshRate(message);
        } else {
            notifyForRequestError(message, RefreshRateRequest.COMMAND_REFRESH_RATE);
        }
    }

    private void notifyForRefreshRate(String message) {
        RefreshRateResponse quoteWatchResponse = gson.fromJson(message, RefreshRateResponse.class);
        Resource<RefreshRateResponse> result = Resource.success(quoteWatchResponse);
        notifyListener(listenersMap.get(RefreshRateRequest.COMMAND_REFRESH_RATE), result);
        listenersMap.remove(RefreshRateRequest.COMMAND_REFRESH_RATE);
    }


    // ---------------------------------------------------------------------------------------------
    // Symbol Search
    // ---------------------------------------------------------------------------------------------
    private String generateSymbolSearchRequestString(String query) {
        return gson.toJson(SymbolSearchRequest.create(query));
    }

    private void handleSymbolSearchResponse(String message) {
        if (isResponseOk(message)) {
            notifyForSymbolFound(message);
        } else {
            notifyForRequestError(message, SymbolSearchRequest.COMMAND_SYMBOL_SEARCH);
        }
    }

    private void notifyForSymbolFound(String message) {
        SymbolSearchResponse symbolSearchResponse = searchResultDeserializer.fromJson(message,
                SymbolSearchResponse.class);

        Resource<SymbolSearchResponse> result = Resource.success(symbolSearchResponse);
        notifyListener(listenersMap.get(SymbolSearchRequest.COMMAND_SYMBOL_SEARCH), result);
        listenersMap.remove(SymbolSearchRequest.COMMAND_SYMBOL_SEARCH);
    }


    // ---------------------------------------------------------------------------------------------
    // Chart
    // ---------------------------------------------------------------------------------------------
    private String generateChartSnapRequestString(String reqId, String expression, int intervalCount) {
        return gson.toJson(ChartSnapRequest.create(reqId, expression, intervalCount));
    }

    private void handleChartSnapResponse(String message) {
        if (isResponseOk(message)) {
            notifyForChartData(message);
        } else {
            notifyForRequestError(message, ChartSnapRequest.COMMAND_CHART_SNAP);
        }
    }

    private String generateChartWatchRequestString(String reqId, String expression, int intervalCount) {
        return gson.toJson(ChartWatchRequest.create(reqId, expression, intervalCount));
    }

    private void handleChartWatchResponse(String message) {
        if (isResponseOk(message)) {
            notifyForChartData(message);
        } else {
            notifyForRequestError(message, ChartWatchRequest.COMMAND_CHART_WATCH);
        }
    }

    private void notifyForChartData(String message) {
        ChartWatchResponse chartWatchResponse = gson.fromJson(message,
                ChartWatchResponse.class);
        Resource<ChartWatchResponse> result = Resource.success(chartWatchResponse);
        notifyListener(listenersWatchMap.get(chartWatchResponse.meta.requestId), result);
    }


    // ---------------------------------------------------------------------------------------------
    // Status
    // ---------------------------------------------------------------------------------------------

    /**
     * A status message is the one message that a client may receive at any time that is not
     * associated
     * with a request. There are two general forms of status messages:
     * - informational
     * - server shutdown
     * The status message will have a data property with an array of notification objects.
     * All notification objects will contain a type property and may have other properties as well.
     * Based on the value of the type key, the client should take appropriate action.
     * The following sections cover the defined notification types and the action should do with the
     * object.
     * <p>
     * Handle responses like this:
     * { "meta":{
     * "command":"status",
     * "status":503},
     * "data":[{
     * "type":"ServerShutdown",
     * "status":0,
     * "name":"PAG",
     * "message":""
     * }]}
     *
     * @param message response
     */
    private void handleStatusResponse(String message) {
        StatusResponse errorResponse = gson.fromJson(message, StatusResponse.class);
        for (Map.Entry<String, ApiRequestListener> entry : listenersMap.entrySet()) {
            com.fondova.finance.api.model.status.Status status = errorResponse.data.get(0);
            notifyListener(entry.getValue(), Resource.error(status.type, status.message));
        }
    }


    @Override
    public void onConnected(@NotNull WebsocketService websocketService) {

    }

    @Override
    public void onDisconnected(@NotNull WebsocketService websocketService, int code, @NotNull String reason, boolean closedByServer) {
        listenersMap.clear();
        listenersQuoteSnapMap.clear();
        listenersWatchMap.clear();
        listenersQuoteUnWatchMap.clear();
    }

    @Override
    public void onSocketError(@NotNull WebsocketService websocketService, @NotNull IOException exception) {

    }

    @Override
    public void onErrorMessage(@NotNull String message) {

    }
}
