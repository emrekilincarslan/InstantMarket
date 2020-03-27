package com.fondova.finance.repo;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.fondova.finance.AppExecutors;
import com.fondova.finance.api.Resource;
import com.fondova.finance.api.Status;
import com.fondova.finance.api.model.chart.ChartWatchResponse;
import com.fondova.finance.api.model.quote.OldQuoteWatchResponse;
import com.fondova.finance.api.socket.ApiService;
import com.fondova.finance.workspace.WorkspaceQuote;
import com.fondova.finance.persistance.LocalQuoteListManager;
import com.fondova.finance.ui.chart.ChartDataValidator;
import com.fondova.finance.ui.chart.detail.ChartInterval;
import com.fondova.finance.vo.ChartData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ChartDataRepository {

    private static final String TAG = "ChartDataRepository";
    private static final int TIMEOUT = 30 * 1000;

    @Inject ApiService apiService;
    @Inject
    LocalQuoteListManager quoteDao;
    @Inject AppExecutors appExecutors;
    private final Map<String, String> mapReqIdChart;
    private final List<ChartData> chartDataListInsert;
    private Handler handler;
    private MutableLiveData<Resource<ChartWatchResponse>> liveChartData = new MutableLiveData<>();
    private MutableLiveData<ChartData> lastestChartData = new MutableLiveData<>();

    @Inject
    public ChartDataRepository() {
        mapReqIdChart = new HashMap<>();
        chartDataListInsert = new ArrayList<>();
        handler = new Handler();
    }


    public LiveData<Resource<ChartWatchResponse>> snapChartDataForQuote(WorkspaceQuote quote, int intervalCount) {
        liveChartData = new MutableLiveData<>();
        String reqId = UUID.randomUUID().toString();
        Log.i(TAG, "snap chart request for " + intervalCount);
        appExecutors.networkIO().execute(() -> {
            Runnable r = () -> appExecutors.mainThread().execute(() -> liveChartData.setValue(Resource.success(ChartWatchResponse.empty())));
            handler.postDelayed(r, TIMEOUT);

            apiService.chartSnap(reqId, quote.getValue(), intervalCount, resourceResponse -> {
                handler.removeCallbacks(r);

                if (resourceResponse.status == Status.SUCCESS) {
                    insertChartData(quote.getValue(), intervalCount, resourceResponse, liveChartData);
                } else {
                    appExecutors.mainThread().execute(() -> liveChartData.setValue(resourceResponse));
                }
            });
        });

        return liveChartData;
    }


    public LiveData<Resource<ChartWatchResponse>> watchDataForQuote(WorkspaceQuote quote, int intervalCount) {
        liveChartData = new MutableLiveData<>();
        liveChartData.setValue(Resource.loading());
        String reqId = UUID.randomUUID().toString();
        mapReqIdChart.put(quote.getValue(), reqId);
        Log.i(TAG, "watch chart request for " + intervalCount);
        String expression = quote.getValue();
        appExecutors.networkIO().execute(() -> {

            Runnable r = () -> appExecutors.mainThread().execute(() -> liveChartData.setValue(Resource.loading()));
            handler.postDelayed(r, TIMEOUT);

            apiService.chartWatch(reqId, expression, intervalCount,
                    resourceResponse -> {
                        handler.removeCallbacks(r);
                        if (resourceResponse.status == Status.SUCCESS) {
                            insertChartData(quote.getValue(), intervalCount, resourceResponse, liveChartData);
                        } else {
                            appExecutors.mainThread().execute(() -> liveChartData.setValue(resourceResponse));
                        }
                    });
        });

        return liveChartData;
    }

    private void updateLiveData(@Nullable List<ChartData> chartDatas, MutableLiveData<Resource<ChartWatchResponse>> liveChartData) {
        ChartWatchResponse chartWatchResponse = new ChartWatchResponse();
        chartWatchResponse.data = chartDatas;
        liveChartData.setValue(Resource.success(chartWatchResponse));

        List<ChartData> chartDataList = liveChartData.getValue().data.data;
        if (!chartDataList.isEmpty()) {
            ChartData chartData = chartDataList.get(chartDataList.size() - 1);
            lastestChartData.setValue(chartData);
        }

    }

    public LiveData<ChartData> getLatestChartData() {
        return lastestChartData;
    }

    public void deleteCachedChartData() {
        liveChartData.setValue(Resource.success(new ChartWatchResponse()));
    }

    public LiveData<Resource<OldQuoteWatchResponse>> unwatchChartData(String quoteId, @ChartInterval int interval) {
        MutableLiveData<Resource<OldQuoteWatchResponse>> unwatchLiveData = new MutableLiveData<>();
        String reqId = mapReqIdChart.get(quoteId);
        mapReqIdChart.remove(quoteId + interval);
        appExecutors.networkIO().execute(() -> {
            apiService.unwatch(reqId,
                    resourceResponse -> {
                        Log.d(TAG, "unwatch, remove " + quoteId);
                        appExecutors.mainThread().execute(() -> unwatchLiveData.setValue(resourceResponse));
                    });
        });
        return unwatchLiveData;
    }

    public Resource<ChartWatchResponse> getLoadedChartData() {
        return liveChartData.getValue();
    }

    private void insertChartData(String quoteId, int intervalCount, Resource<ChartWatchResponse> resourceResponse, @NonNull final MutableLiveData<Resource<ChartWatchResponse>> liveData) {

        for (ChartData chartData : resourceResponse.data.data) {
            if (!ChartDataValidator.isValidChartData(chartData)) {
                //chart data is not valid => skip it
                continue;
            }

            chartData.quoteId = 0;
            chartData.interval = intervalCount;

            Integer existingIndex = indexOf(chartData);
            if (existingIndex != null) {
                chartDataListInsert.set(existingIndex, chartData);
            } else {
                chartDataListInsert.add(chartData);
            }
        }


        if (resourceResponse.data.meta.upToDate) {
            appExecutors.mainThread().execute(() -> updateLiveData(chartDataListInsert, liveData));
            //chartDataListInsert.clear();
        }
    }


    private Integer indexOf(ChartData chartDataPoint) {
        String dateTime = chartDataPoint.dateTime;
        for (int i = 0; i<chartDataListInsert.size(); i++) {
            if (chartDataListInsert.get(i).dateTime.equals(dateTime)) {
                return i;
            }
        }
        return null;
    }
}
