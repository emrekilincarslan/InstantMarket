package com.fondova.finance.ui.chart.detail;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Handler;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.Pair;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.shinobicontrols.charts.Data;
import com.shinobicontrols.charts.DataPoint;
import com.shinobicontrols.charts.MultiValueDataPoint;
import com.shinobicontrols.charts.NumberRange;
import com.shinobicontrols.charts.Tooltip;
import com.fondova.finance.AppExecutors;
import com.fondova.finance.R;
import com.fondova.finance.api.Resource;
import com.fondova.finance.api.model.chart.ChartWatchResponse;
import com.fondova.finance.api.model.news.CategoryArticle;
import com.fondova.finance.api.model.news.NewsWatchResponse;
import com.fondova.finance.api.model.quote.*;
import com.fondova.finance.api.session.SessionService;
import com.fondova.finance.api.session.SessionStatus;
import com.fondova.finance.workspace.WorkspaceQuote;
import com.fondova.finance.workspace.WorkspaceQuoteType;
import com.fondova.finance.persistance.AppStorage;
import com.fondova.finance.repo.QuoteWatchRepository;
import com.fondova.finance.repo.TextsRepository;
import com.fondova.finance.ui.BaseUseCase;
import com.fondova.finance.ui.util.DateFormatUtil;
import com.fondova.finance.vo.ChartData;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.inject.Inject;
import javax.inject.Singleton;

import static android.text.TextUtils.isEmpty;
import static com.fondova.finance.ui.chart.detail.ChartInterval.MIN_5;

@Singleton
class ChartUseCase extends BaseUseCase<QuoteChartView> implements LifecycleObserver {

    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------
    private static final String TAG = "ChartUseCase";

    private static final int[] TAB_STRINGS = {R.string.min_5, R.string.min_30, R.string.min_60, R.string.day, R.string.week, R.string.month};
    private static final int[] TABS_INTERVALS = {MIN_5, ChartInterval.MIN_30, ChartInterval.MIN_60, ChartInterval.DAY, ChartInterval.WEEK, ChartInterval.MONTH};

    private static final int INITIAL_SELECTED_TAB_POSITION = 3;
    private static final int VISIBLE_X_POSITIONS_PORT = 35;
    protected static final int RELATED_NEWS_COUNT = 4;
    private static final float RANGE_PADDING_PERCENTAGE = 0.1f;
    private static final String MISSING = "---";


    private static final SimpleDateFormat DATE_FORMAT_SHORT = new SimpleDateFormat("M/d/yy\nh:mma z", Locale.US);
    private static final SimpleDateFormat DATE_FORMAT_LOCAL = new SimpleDateFormat("M/d/yy", Locale.US);
    private static final SimpleDateFormat DATE_FORMAT_MONTH = new SimpleDateFormat("M/yy", Locale.US);

    static {
        DATE_FORMAT_LOCAL.setTimeZone(TimeZone.getTimeZone("GMT"));
        DATE_FORMAT_MONTH.setTimeZone(TimeZone.getTimeZone("GMT"));
        DateFormatSymbols amPmSymbols = new DateFormatSymbols();
        amPmSymbols.setAmPmStrings(new String[]{"a", "p"});
        DATE_FORMAT_SHORT.setDateFormatSymbols(amPmSymbols);
    }

    // ---------------------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------------------
    private boolean loadingChartData;
    private boolean hasShownMessageXAxisReached;
    private boolean shouldUpdateChartDataOnResume;
    private boolean isLastChartDataShownInTooltip = false;
    private @ChartStyle int currentChartStyle;
    private LiveData<Resource<ChartWatchResponse>> liveChartData;
    private ChartData latestCachedChartData;
    private LiveData<ChartData> latestChartLive;
    private List<CategoryArticle> categoryArticles;
    private String quoteShortDescription;
    private ChartViewModel viewModel;
    private final TextsRepository textsRepository;
    private final AppExecutors appExecutors;
    private final SessionService sessionService;
    private WorkspaceQuote quote;
    private String tooltipLastDateTime;
    private boolean hasShownTooltip;
    private List<ChartData> chartDataList;
    private Handler updateChartDataHandler;
    private Runnable updateChartDataRunnable;
    private AppStorage appStorage;
    private QuoteWatchRepository quoteWatchRepository;


    @Inject
    public ChartUseCase(TextsRepository textsRepository,
                        AppExecutors appExecutors,
                        QuoteWatchRepository quoteWatchRepository,
                        SessionService sessionService,
                        AppStorage appStorage) {
        this.textsRepository = textsRepository;
        this.appExecutors = appExecutors;
        this.appStorage = appStorage;
        this.quoteWatchRepository = quoteWatchRepository;
        this.sessionService = sessionService;
    }

    // ---------------------------------------------------------------------------------------------
    // LifeCycle
    // ---------------------------------------------------------------------------------------------
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    void setup() {
        viewModel = ViewModelProviders.of((FragmentActivity) source).get(ChartViewModel.class);
        quote = view.getQuote();
        if (view.getChartStyleAfterRotate() != QuoteChartView.INVALID_CHART_STYLE) {
            currentChartStyle = view.getChartStyleAfterRotate();
        } else {
            currentChartStyle = quote.getType().toLowerCase().equals(WorkspaceQuoteType.Companion.getEXPRESSION()) ? ChartStyle.LINE : appStorage.getChartStyle();
        }
        hasShownMessageXAxisReached = false;
        isLastChartDataShownInTooltip = false;
        view.setupTabs(getSelectedTabPosition(), TAB_STRINGS);

        view.initChart(view.loadsForFirstTime());
        if (view.loadsForFirstTime()) {
            view.setChartStyle(currentChartStyle);
        }

        if (checkForNetworkOperationAllowed()) {
            loadQuote();
            shouldUpdateChartDataOnResume = false;
            viewModel.deleteChartData();
            loadChartData();
            if (!view.isLandscape()) loadNews();
        }
    }

    @Override
    protected void onBaseCreate(LifecycleOwner source) {
        super.onBaseCreate(source);
        sessionService.getSessionStatusLiveData().observe(source, this::sessionStatusChanged);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void onResume() {
        if (shouldUpdateChartDataOnResume && hasLiveData()) {
            updateChartData(getChartData());
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    void onPause() {
        shouldUpdateChartDataOnResume = true;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void onStop() {
        viewModel.stopListeningForArticleUpdates(quote.getValue());
        unwatchChartData(quote.getValue(), appStorage.getChartInterval());
        if (updateChartDataHandler != null && updateChartDataRunnable != null)
            updateChartDataHandler.removeCallbacks(updateChartDataRunnable);
    }


    // ---------------------------------------------------------------------------------------------
    // Public
    // ---------------------------------------------------------------------------------------------
    void onChartStyleBarClick() {
        if (currentChartStyle != ChartStyle.BAR) changeChartStyle(ChartStyle.BAR);
    }

    void onChartStyleLineClick() {
        if (currentChartStyle != ChartStyle.LINE) changeChartStyle(ChartStyle.LINE);
    }

    void onChartStyleCandlestickClick() {
        if (currentChartStyle != ChartStyle.CANDLESTICK) changeChartStyle(ChartStyle.CANDLESTICK);
    }

    void onChartTabClicked(int position) {
        if (loadingChartData) return;
        showLoadingData(true);
        hasShownMessageXAxisReached = false;
        isLastChartDataShownInTooltip = false;

        if (appStorage.isManualRefreshEnabled()) {
            appStorage.setChartInterval(TABS_INTERVALS[position]);
            viewModel.deleteChartData();
            loadChartData();
        } else
            unwatchChartData(quote.getValue(), appStorage.getChartInterval()).observe(source, responseResource -> {
                switch (responseResource.status) {
                    case SUCCESS:
                    case ERROR:
                        appStorage.setChartInterval(TABS_INTERVALS[position]);
                        loadChartData();
                        break;
                }
            });
    }

    void onClickedArticle(int position) {
        if (position >= 0 && position < categoryArticles.size()) {
            CategoryArticle article = categoryArticles.get(position);
            view.openArticleDetails(article.storyId, article.title);
        }
    }

    void onViewAllArticlesClicked() {
        if (isShortDescriptionInit()) {
            view.openNewsScreen(quote, quoteShortDescription);
        }
    }

    void onTooltipInfoChanged(Tooltip tooltip, DataPoint<?, ?> dataPoint) {
        SpannableString tooltipString = buildTooltipText(dataPoint);
        if (!isEmpty(tooltipString)) {
            view.populateTooltip(tooltip, tooltipString);
        }
    }

    void onChartLeftEndReached() {
        if (!hasShownMessageXAxisReached) {
            view.showMessage(R.string.x_axis_end_reached);
            hasShownMessageXAxisReached = true;
        }
    }

    void updateYRange(final int minimumRange, final int maximumRange) {

        if (!hasLiveData()) return;

        if (chartDataList == null || chartDataList.size() == 0) return;

        appExecutors.dataThread().execute(() -> {

            final int minIndex = getInBoundsQuotesIndex(minimumRange, chartDataList);
            final int maxIndex = getInBoundsQuotesIndex(maximumRange, chartDataList);

            ChartData minVisible = chartDataList.get(minIndex);

            double high = minVisible.high.number != null ? minVisible.high.number : 0f;
            double low = minVisible.low.number != null ? minVisible.low.number : 0f;

            final double[] result = setLowAndHighForAxisY(low, high, minIndex, maxIndex, chartDataList);
            low = result[0];
            high = result[1];

            final double finalHigh = high;
            final double finalLow = low;

            appExecutors.mainThread().execute(() -> updateAxisYRange(finalHigh, finalLow));
        });
    }

    @NonNull
    String onUpdateAxisXTickMark(@NonNull Double value, @Nullable NumberRange range) {

        if (!hasLiveData()) return MISSING;

        final int dataIndex = value.intValue();

        if (range == null || chartDataList == null) {
            Log.d(TAG, "onUpdateAxisXTickMark() Range is null.");
            return MISSING;
        }

        if (dataIndex >= 0 && dataIndex < chartDataList.size()) {
            final ChartData current = chartDataList.get(dataIndex);
            final String date = current.dateTime;
            if (TextUtils.isEmpty(date)) Log.e(TAG, "onUpdateAxisXTickMark() dateTime is empty");
            return TextUtils.isEmpty(date) ? MISSING : getDateFormat().format(new DateTime(date, DateTimeZone.UTC).toDate());
        }

        return MISSING;
    }

    @NonNull
    String onUpdateAxisYTickMark(@NonNull Double value) {
        return 0.0 == value ? "0.0" : String.format("%." + getSignificantDecimalDigits(value) + "f", value);
    }

    boolean wasMovedOutOfDataRange(double visibleRangeMin) {
        Log.d(TAG, "wasMovedOutOfDataRange: " + visibleRangeMin);
        final double leftMinThreshold = -2.0, leftMaxThreshold = -100.0;
        return visibleRangeMin < leftMinThreshold
                && visibleRangeMin > leftMaxThreshold;
    }

    NumberRange getXAxisRange() {
        int max, min;
        if (!chartDataList.isEmpty()) {
            max = chartDataList.size();
            min = Math.max(0, chartDataList.size() - VISIBLE_X_POSITIONS_PORT);
        } else {
            max = VISIBLE_X_POSITIONS_PORT;
            min = 0;
        }

        return new NumberRange((double) min, (double) max);
    }

    Double getCloseValueForShownTooltipDataPoint(Data<?, ?> dataPoint) {
        if (!(currentChartStyle == ChartStyle.BAR || currentChartStyle == ChartStyle.CANDLESTICK))
            return null; //only bar and candlestick has OHLC values.

        final int index = ((Double) dataPoint.getX()).intValue();
        MultiValueDataPoint<Double, Double> chartDatAtIndex = getChartDatAtIndex(index);
        return chartDatAtIndex == null ? null : chartDatAtIndex.getClose();
    }

    void onCrosshairShowed(boolean isCrosshairShown) {
        hasShownTooltip = isCrosshairShown;
    }

    boolean isLastChartDataShownInTooltip() {
        return isLastChartDataShownInTooltip;
    }

    private void sessionStatusChanged(SessionStatus sessionStatus) {
        Log.i(TAG, "onUserConnected: " + sessionStatus);
        appExecutors.mainThread().execute(() -> {
            switch (sessionStatus) {
                case connected:
                    loadChartData();
                    break;
                case connecting:
                    view.showLoading(true);
                    break;
                default:
                    view.showNotConnectedError(R.string.network_error, R.string.no_connection_on_charts);
                    break;
            }
        });
    }


    // ---------------------------------------------------------------------------------------------
    // Private
    // ---------------------------------------------------------------------------------------------
    @Nullable
    private MultiValueDataPoint<Double, Double> getChartDatAtIndex(int index) {
        ChartData chartData = hasLiveData() && chartDataList.size() > index ? chartDataList.get(index) : null;
        return convertServerDataToUi(index, chartData);
    }

    private boolean isShortDescriptionInit() {
        return !isEmpty(quoteShortDescription);
    }

    private int getSelectedTabPosition() {
        for (int i = 0; i < TABS_INTERVALS.length; i++) {
            if (TABS_INTERVALS[i] == appStorage.getChartInterval()) return i;
        }
        return INITIAL_SELECTED_TAB_POSITION;
    }

    private boolean checkForNetworkOperationAllowed() {
        if (sessionService.getSessionStatusLiveData().getValue() != SessionStatus.connected) {
            view.showNotConnectedError(R.string.network_error, R.string.no_network_message);
            return false;
        }

        return true;
    }

    @Nullable
    private SpannableString buildTooltipText(DataPoint<?, ?> dataPoint) {

        if (!hasLiveData()) return null;

        // Next get the tracked series
        StringBuilder stringBuilder = new StringBuilder();
        int index;
        index = ((Double) dataPoint.getX()).intValue();

        isLastChartDataShownInTooltip = index == chartDataList.size() - 1;

        Log.d(TAG, "buildTooltipText: chartData for index: " + index);
        if (chartDataList == null || index < 0 || index >= chartDataList.size()) return null;

        ChartData chartData = chartDataList.get(index);
        ChartData prevData = index != 0 ? chartDataList.get(index - 1) : null;
        Log.d(TAG, "buildTooltipText: prev data: " + (prevData != null ? prevData.dateTime : null));

        double closeNumber = chartData.close == null || chartData.close.number == null ? 0f : chartData.close.number;
        double closePrevNumber = prevData == null || prevData.close == null || prevData.close.number == null ? 0f : prevData.close.number;
        double change = closeNumber - closePrevNumber;
        double changePercent = 100 * ((closeNumber / closePrevNumber) - 1.0);

        String values = textsRepository.getString(R.string.format_chart_data_on_tooltip,
                chartData.close, // last
                chartData.high, // high
                chartData.low, // low
                chartData.open, // open
                chartData.volume != null ? chartData.volume : MISSING, // volume
                index == 0 ? MISSING : String.format(Locale.getDefault(), "%.2f", change), // change
                index == 0 ? MISSING : String.format(Locale.getDefault(), "%.2f", changePercent)); // changePercent
        DateTime dateTime = new DateTime(chartData.dateTime, DateTimeZone.UTC).withZone(DateTimeZone.getDefault());
        final String date = getDateFormat().format(dateTime.toDate());
        stringBuilder.append(date);
        stringBuilder.append("\n").append(values);

        SpannableString spannable = new SpannableString(stringBuilder.toString());
        spannable.setSpan(new ForegroundColorSpan(textsRepository.getColor(R.color.view_all_articles)), 0, date.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tooltipLastDateTime = chartData.dateTime;

        return spannable;
    }

    private void loadQuote() {
        viewModel.getQuoteWithLatestValue(quote.getValue()).observe(source, quoteAndValue -> {
            if (view == null) return;

            populateToolbar(quote, new QuoteWatchResponse());
            //setToolbarColor(quoteAndValue.value);
            LiveData<QuoteWatchResponse> liveData = quoteWatchRepository.subscribe(quote.getValue());
            liveData.observe(source, new Observer<QuoteWatchResponse>() {
                @Override
                public void onChanged(@Nullable QuoteWatchResponse value) {
                    populateToolbar(quote, value);
                    if (!view.isLandscape()) {
                        updateLatestQuoteValue(value);
                    }
                }
            });
        });
    }

    private void populateToolbar(WorkspaceQuote quote, QuoteWatchResponse value) {
        String empty = QuoteWatchResponseFieldExtensionsKt.errorOrEmpty(value);
        if (view.isLandscape()) {
            view.setupToolbar(textsRepository.quoteTitleLand(quote, value, empty));
            view.updateToolbarSubtitle(textsRepository.quoteDataLand(value, empty));
        } else {
            view.setupToolbar(QuoteWatchResponseFieldExtensionsKt.getSymbolDescription(value));
            view.updateToolbarSubtitle(textsRepository.quoteDataPort(quote, value, empty));
        }
    }

    private void updateLatestQuoteValue(QuoteWatchResponse value) {
        String shortDateString = DateFormatUtil.serverDateStringToUiShortDateString(QuoteWatchResponseFieldExtensionsKt.getSettleDate(value));
        String empty = QuoteWatchResponseFieldExtensionsKt.errorOrEmpty(value);
        String date = isEmpty(shortDateString) ? empty : shortDateString;
        if (isEmpty(QuoteWatchResponseFieldExtensionsKt.getOpen(value))) QuoteWatchResponseFieldExtensionsKt.setOpen(value, empty);
        if (isEmpty(QuoteWatchResponseFieldExtensionsKt.getHigh(value))) QuoteWatchResponseFieldExtensionsKt.setHigh(value, empty);
        if (isEmpty(QuoteWatchResponseFieldExtensionsKt.getLow(value))) QuoteWatchResponseFieldExtensionsKt.setLow(value, empty);
        if (isEmpty(QuoteWatchResponseFieldExtensionsKt.getSettleDate(value))) QuoteWatchResponseFieldExtensionsKt.setSettleDate(value, empty);
        if (isEmpty(QuoteWatchResponseFieldExtensionsKt.getSettlePrice(value))) QuoteWatchResponseFieldExtensionsKt.setSettlePrice(value, empty);
        if (isEmpty(QuoteWatchResponseFieldExtensionsKt.getBid(value))) QuoteWatchResponseFieldExtensionsKt.setBid(value, empty);
        if (isEmpty(QuoteWatchResponseFieldExtensionsKt.getAsk(value))) QuoteWatchResponseFieldExtensionsKt.setAsk(value, empty);
        String volume = QuoteWatchResponseFieldExtensionsKt.getVolume(value) == null ? empty : String.valueOf(QuoteWatchResponseFieldExtensionsKt.getVolume(value));

        view.updateLastQuoteValue(value, date, volume);
    }

    private LiveData<Resource<OldQuoteWatchResponse>> unwatchChartData(String quoteId, int interval) {
        loadingChartData = true;
        if (latestChartLive != null && latestChartLive.hasObservers()) {
            latestChartLive.removeObservers(source);
        }
        return viewModel.stopChartDataReceiving(quoteId, interval);
    }

    private void changeChartStyle(@ChartStyle int newChartStyle) {
        if (loadingChartData) return;

        showLoadingData(true);
        hasShownMessageXAxisReached = false;
        isLastChartDataShownInTooltip = false;
        currentChartStyle = newChartStyle;
        view.setChartStyle(currentChartStyle);
        appStorage.setChartStyle(currentChartStyle);

        updateChartDataRunnable = () -> updateChartData(getChartData());
        getUpdateChartDataHandler().postDelayed(updateChartDataRunnable, 80);
    }

    private Handler getUpdateChartDataHandler() {
        if (updateChartDataHandler == null) updateChartDataHandler = new Handler();
        return updateChartDataHandler;
    }

    private Resource<ChartWatchResponse> getChartData() {
        return viewModel.getLoadedChartData();
    }

    private void loadChartData() {
        view.showLoading(true);
        loadingChartData = true;
        if (appStorage.isManualRefreshEnabled()) {
            liveChartData = viewModel.snapChartDataForQuote(view.getQuote(), appStorage.getChartInterval());
            Log.d(TAG, "loadChartData: snapChartData interval " + appStorage.getChartInterval());
        } else {
            liveChartData = viewModel.watchChartDataForQuote(view.getQuote(), appStorage.getChartInterval());
            Log.d(TAG, "loadChartData: watchChartData interval " + appStorage.getChartInterval());
        }
        liveChartData.observe(source, (responseResource) -> {
            Log.d(TAG, "loadChartData: received" + responseResource);
            switch (responseResource.status) {
                case SUCCESS:
                    updateChartData(responseResource);
                    break;
                case ERROR:
                    loadingChartData = false;
                    view.enableTabClicks(true);
                    handleError(responseResource);
                    break;
                case LOADING:
                    view.enableTabClicks(false);
                    loadingChartData = true;
                    showLoadingData(true);
                    break;
            }
        });
    }

    private void handleError(@NonNull final Resource<ChartWatchResponse> responseResource) {
        if (!ChartsApiErrors.API_ERROR_UNKNOWN_EXPRESSION.equalsIgnoreCase(responseResource.message)) {
            handleGenericError(responseResource);
        }

        showEmptyView();
    }

    private void handleGenericError(@NonNull Resource<ChartWatchResponse> responseResource) {
        showLoadingData(false);
        view.showMessage(responseResource.title, responseResource.message);
    }

    private void showEmptyView() {
        view.showCharts(false);
        view.showLoading(false);
        view.showEmptyView(true);
    }

    private void showLoadingData(final boolean loading) {
        view.showLoading(loading);
        view.showCharts(!loading);
        view.showEmptyView(false);
    }

    private void updateChartData(Resource<ChartWatchResponse> responseResource) {
        if (responseResource == null || responseResource.data == null) return;


        appExecutors.dataThread().execute(() -> {
            ChartWatchResponse theChartWatchResponse = liveChartData.getValue().data;
            if (theChartWatchResponse == null) {
                chartDataList = Collections.emptyList();
            } else {
                chartDataList = new ArrayList<>(theChartWatchResponse.data);
            }
            DataPointsAndRange dataPointsAndRange = null;
            switch (currentChartStyle) {
                case ChartStyle.BAR:
                case ChartStyle.CANDLESTICK:
                    dataPointsAndRange = convertToOhlcData(chartDataList);
                    break;
                case ChartStyle.LINE:
                    dataPointsAndRange = convertToLineData(chartDataList);
                    break;
            }
            final List<Data<Double, Double>> finalDataPoints = dataPointsAndRange.dataPoints;
            final Pair<NumberRange, Double> rangeAndFrequency = constructRangeY();
            appExecutors.mainThread().execute(() -> {
                if (view == null) return;
                loadingChartData = false;
                view.enableTabClicks(true);
                if (finalDataPoints.size() > 0) {
                    liveChartData.removeObservers(source);
                    showLoadingData(false);
                    view.updateChartData(finalDataPoints, getXAxisRange(), rangeAndFrequency.first, rangeAndFrequency.second, currentChartStyle);
                    observeForLatestChartData();
                    Log.d(TAG, "updateChartData: onMainThread");
                } else {
                    showEmptyView();
                }
            });
        });
    }

    private void observeForLatestChartData() {
        if (latestChartLive != null && latestChartLive.hasObservers()) {
            latestChartLive.removeObservers(source);
        }

        latestChartLive = viewModel.getLatestLive();
        latestChartLive.observe(source, newChartData -> {
            if (newChartData == null || !hasLiveData() || chartDataList.size() == 0) {
                return;
            }

            boolean hasEqualValuesLive = newChartData.hasEqualValues(chartDataList.get(chartDataList.size() - 1));
            if (hasEqualValuesLive) {
                latestCachedChartData = newChartData;
                // do not fire update
                Log.d(TAG, "observeForLatestChartData: last charData is NULL or the same as the new one: " + newChartData);
                return;
            }

            updateLatestChartData(newChartData);
        });
    }

    private void updateLatestChartData(ChartData latestChartData) {
        DataPoint<Double, Double> dataPoint = null;
        if (view == null || !hasLiveData() || isInvalidChartDataPoint(latestChartData)) {
            Log.d(TAG, "observeForLatestChartData: invalidChartDataPoints/empty");
            return;
        }

        boolean shouldAdd = latestCachedChartData == null || !latestCachedChartData.dateTime.equals(latestChartData.dateTime);

        switch (currentChartStyle) {
            case ChartStyle.BAR:
            case ChartStyle.CANDLESTICK:
                dataPoint = convertServerDataToUi(getLatestIndex() + (shouldAdd ? 1 : 0), latestChartData);
                break;
            case ChartStyle.LINE:
                dataPoint = new DataPoint<>((double) getLatestIndex() + (shouldAdd ? 1 : 0), latestChartData.open.number);
                break;
        }

        if (shouldAdd) {
            chartDataList.add(latestChartData);

            view.insertNewDataPoint(dataPoint, currentChartStyle, getXAxisRange());
        } else {
            chartDataList.set(getLatestIndex(), latestChartData);
            view.updateLatestData(dataPoint, currentChartStyle);
        }

        if ((!TextUtils.isEmpty(tooltipLastDateTime) && tooltipLastDateTime.equals(latestChartData.dateTime)) || (isLastChartDataShownInTooltip && hasShownTooltip)) {
            // shownTooltip is for the same dateTime.
            view.updateShownTooltipData(buildTooltipText(dataPoint));
        }

        latestCachedChartData = latestChartData;
    }

    @Nullable
    private MultiValueDataPoint<Double, Double> convertServerDataToUi(double index, ChartData chartData) {
        return chartData == null ? null :
                new MultiValueDataPoint<>(
                        index,
                        chartData.low.number,
                        chartData.high.number,
                        chartData.open.number,
                        chartData.close.number);
    }

    private int getLatestIndex() {
        return hasLiveData() ? chartDataList.size() - 1 : 0;
    }

    private boolean hasLiveData() {
        return chartDataList != null && chartDataList.size() > 0;
    }

    private DateFormat getDateFormat() {
        switch (appStorage.getChartInterval()) {
            case MIN_5:
            case ChartInterval.MIN_30:
                return DATE_FORMAT_SHORT;
            case ChartInterval.MIN_60:
                return DATE_FORMAT_SHORT;
            case ChartInterval.DAY:
                return DATE_FORMAT_LOCAL;
            case ChartInterval.WEEK:
                return DATE_FORMAT_LOCAL;
            case ChartInterval.MONTH:
                return DATE_FORMAT_MONTH;
        }
        return null;
    }


    @Nullable
    Pair<NumberRange, Double> constructRangeY() {
        if (chartDataList == null || chartDataList.isEmpty())
            return null;

        final int minIndex = getInBoundsQuotesIndex(chartDataList.size() - VISIBLE_X_POSITIONS_PORT, chartDataList);
        int maxIndex = chartDataList.size() - 1;

        double high = chartDataList.get(minIndex).high.number != null ? chartDataList.get(minIndex).high.number : 0f;
        double low = chartDataList.get(minIndex).low.number != null ? chartDataList.get(minIndex).low.number : 0f;

        final double[] result = setLowAndHighForAxisY(low, high, minIndex, maxIndex, chartDataList);
        low = result[0];
        high = result[1];

        return new Pair<>(createNewYRange(high, low), (high - low) / 10);
    }

    private int getInBoundsQuotesIndex(int index, @NonNull final List<?> forData) {
        return index < 0 ? 0 : (index >= forData.size() ? forData.size() - 1 : index);
    }

    private DataPointsAndRange convertToLineData(List<ChartData> chartDatas) {
        List<Data<Double, Double>> dataPoints = new ArrayList<>(chartDatas.size());
        for (int i = 0; i < chartDatas.size(); i++) {
            // Loop through response array and create a multi value data point for each object
            ChartData chartData = chartDatas.get(i);
            DataPoint<Double, Double> dataPoint;
            dataPoint = new DataPoint<>((double) i, chartData.close.number);

            // Add the data point to the list of data points
            dataPoints.add(dataPoint);
        }

        DataPointsAndRange dataPointsAndRange = new DataPointsAndRange();
        dataPointsAndRange.dataPoints = dataPoints;
        return dataPointsAndRange;
    }

    private DataPointsAndRange convertToOhlcData(@Nullable List<ChartData> chartDataList) {
        DataPointsAndRange dataPointsAndRange = new DataPointsAndRange();

        if (chartDataList == null) return dataPointsAndRange;

        List<Data<Double, Double>> dataPoints = new ArrayList<>(chartDataList.size());

        int size = chartDataList.size();
        for (int i = 0; i < size; i++) {
            // Loop through response array and create a multi value data point for each object
            ChartData chartData = chartDataList.get(i);
            MultiValueDataPoint<Double, Double> dataPoint;

            if (isInvalidChartDataPoint(chartData)) {
                Log.e(TAG, "Problematic DataPoint " + chartData.toString());
                continue;
            }

            dataPoint = convertServerDataToUi(i, chartData);

            // Add the data point to the list of data points
            dataPoints.add(dataPoint);
        }
        dataPointsAndRange.dataPoints = dataPoints;
        return dataPointsAndRange;
    }

    private boolean isInvalidChartDataPoint(ChartData chartData) {
        return chartData == null || chartData.low == null || chartData.high == null || chartData.open == null || chartData.close == null;
    }

    private void loadNews() {
        viewModel.getChartArticles(quote.getValue(), RELATED_NEWS_COUNT).observe(source, articlesResponse -> {
            if (articlesResponse == null) return;

            switch (articlesResponse.status) {
                case SUCCESS:
                    view.enableViewAllArticlesClick(true);
                    updateNews(articlesResponse.data);
                    break;

                case ERROR:
                    view.showMessage(articlesResponse.title, articlesResponse.message);
                    break;

                case LOADING:
                    view.enableViewAllArticlesClick(false);
                    break;
            }
        });
    }

    private void updateNews(NewsWatchResponse articlesResponse) {
       final List<CategoryArticle> allArticles = articlesResponse.data;
        int workingSize = Math.min(allArticles.size(), RELATED_NEWS_COUNT);
        int realSize = allArticles.size();
        for (int i = realSize - 1; i >= 0; i--) {
            CategoryArticle article = allArticles.get(i);
            article.datetime = DateFormatUtil.serverDateStringToUiString(article.datetime);
        }
        int index = Math.max(0, articlesResponse.meta.keywords.size() - 1);
        quoteShortDescription = articlesResponse.meta.keywords.get(index);
        this.categoryArticles = allArticles.subList(realSize - workingSize, realSize);
        view.loadNews(categoryArticles);
    }

    private double[] setLowAndHighForAxisY(double low, double high, int fromIndex, int toIndex, @Nullable final List<ChartData> toBeSearched) {
        if (toBeSearched == null || toBeSearched.isEmpty() || fromIndex > toIndex || fromIndex < 0 || toIndex >= toBeSearched.size()) {
            return new double[]{low, high};
        }

        while (fromIndex < toIndex) {
            fromIndex++;
            final ChartData data = toBeSearched.get(fromIndex);
            low = data.low != null && data.low.number != null && data.low.number < low ? data.low.number : low;
            high = data.high != null && data.high.number != null && data.high.number > high ? data.high.number : high;
        }

        return new double[]{low, high};
    }

    @MainThread
    private void updateAxisYRange(double high, double low) {
        //update the UI
        if (view != null) view.updateAxisY(createNewYRange(high, low), (high - low) / 10);
    }

    private NumberRange createNewYRange(double high, double low) {
        //calculate new padding
        final double padding = (high - low) * RANGE_PADDING_PERCENTAGE;
        return new NumberRange(low - padding, high + padding);
    }

    private int getSignificantDecimalDigits(Double doubleValue) {
        doubleValue = Math.abs(doubleValue);
        int significantDecimalPlaces = doubleValue < 100 ? 2 : 1;
        while (doubleValue * Math.pow(10.0, significantDecimalPlaces) < 10) {
            significantDecimalPlaces++;
        }

        return significantDecimalPlaces;
    }
}
