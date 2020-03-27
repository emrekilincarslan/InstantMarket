package com.fondova.finance.ui.chart.detail;

import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.SpannableString;

import com.shinobicontrols.charts.Data;
import com.shinobicontrols.charts.DataPoint;
import com.shinobicontrols.charts.NumberRange;
import com.shinobicontrols.charts.Tooltip;
import com.fondova.finance.api.model.news.CategoryArticle;
import com.fondova.finance.api.model.quote.QuoteWatchResponse;
import com.fondova.finance.workspace.WorkspaceQuote;
import com.fondova.finance.ui.BaseView;

import java.util.List;

interface QuoteChartView extends BaseView {

    @ChartStyle int INVALID_CHART_STYLE = -1;

    boolean isLandscape();

    @ChartStyle int getChartStyleAfterRotate();

    WorkspaceQuote getQuote();

    void initChart(boolean setStyle);

    void setupToolbar(String name);

    void setChartStyle(@ChartStyle int chartStyle);

    void updateToolbarSubtitle(String quoteData);

    void updateChartData(List<Data<Double, Double>> chartDataList, NumberRange dateRange, NumberRange rangeY, double frequencyY, @ChartStyle int chartStyle);

    void updateLastQuoteValue(QuoteWatchResponse value, String date, String volume);

    void setupTabs(int selectedPosition, @StringRes int... titles);

    void loadNews(List<CategoryArticle> categoryArticles);

    void populateTooltip(Tooltip tooltip, SpannableString tooltipString);

    void setUpOrDownColor(@ColorRes int color);

    void showLoading(final boolean loading);

    void showCharts(final boolean show);

    void showMessage(@StringRes int message);

    void showNotConnectedError(@StringRes int title, @StringRes int message);

    void updateLatestData(DataPoint latestChartData, @ChartStyle int chartStyle);

    void insertNewDataPoint(DataPoint latestChartData, @ChartStyle int chartStyle, NumberRange dateRange);

    void openNewsScreen(WorkspaceQuote quote, String quoteShortDescription);

    void openArticleDetails(String storyId, String title);

    boolean loadsForFirstTime();

    void enableTabClicks(boolean enable);

    void updateAxisY(@NonNull final NumberRange newRange, double v);

    void showMessage(String title, String message);

    void showEmptyView(final boolean show);

    void updateShownTooltipData(SpannableString data);

    void enableViewAllArticlesClick(boolean enable);
}
