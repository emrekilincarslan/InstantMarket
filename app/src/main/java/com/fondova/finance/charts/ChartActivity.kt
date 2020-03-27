package com.fondova.finance.charts

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.annotation.StringRes
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.gson.Gson
import com.fondova.finance.App
import com.fondova.finance.R
import com.fondova.finance.api.Resource
import com.fondova.finance.api.Status
import com.fondova.finance.api.chart.ChartService
import com.fondova.finance.api.model.news.CategoryArticle
import com.fondova.finance.api.model.news.NewsWatchResponse
import com.fondova.finance.api.model.quote.QuoteWatchResponse
import com.fondova.finance.api.news.NewsService
import com.fondova.finance.charts.strategies.ChartType
import com.fondova.finance.config.AppConfig
import com.fondova.finance.workspace.WorkspaceQuote
import com.fondova.finance.workspace.WorkspaceQuoteType
import com.fondova.finance.news.InstantMarketNewsActivity
import com.fondova.finance.news.OnAllNewsSelectedListener
import com.fondova.finance.news.OnNewsArticleSelectedListener
import com.fondova.finance.persistance.AppStorage
import com.fondova.finance.persistance.QuoteListConverter
import com.fondova.finance.quotes.QuoteResponseView
import com.fondova.finance.quotes.QuoteWatchResponseViewModel
import com.fondova.finance.repo.ChartWatchRepository
import com.fondova.finance.repo.QuoteWatchRepository
import com.fondova.finance.ui.SessionAwareActivity
import com.fondova.finance.ui.chart.news.NewsActivity
import com.fondova.finance.ui.news.view.ViewArticleActivity
import com.fondova.finance.ui.util.DialogUtil
import com.fondova.finance.util.swipeLayout.SwipeRevealLayout
import com.fondova.finance.vo.ChartData
import com.fondova.finance.workspace.WorkspaceFactory
import kotlinx.android.synthetic.main.activity_chart.*
import javax.inject.Inject

class ChartActivity: SessionAwareActivity(), OnNewsArticleSelectedListener, OnAllNewsSelectedListener {

    private val TAG = "IMChartActivity"

    @Inject
    lateinit var quoteWatchRepository: QuoteWatchRepository
    @Inject
    lateinit var appStorage: AppStorage
    @Inject
    lateinit var appConfig: AppConfig
    @Inject
    lateinit var chartWatchRepository: ChartWatchRepository
    @Inject
    lateinit var chartService: ChartService
    @Inject
    lateinit var newsService: NewsService
    @Inject
    lateinit var dialogUtil: DialogUtil

    lateinit var quote: WorkspaceQuote

    companion object {
        val EXTRA_QUOTE = "extra_quote"
        fun start(context: Context, quote: WorkspaceQuote) {
            val intent = Intent(context, ChartActivity::class.java)
            val json = Gson().toJson(WorkspaceFactory().copyQuote(quote))
            intent.putExtra(EXTRA_QUOTE, json)
            context.startActivity(intent)
        }
    }

    override fun onPause() {
        chart_view.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        chart_view.onResume()
    }

    override fun onDestroy() {
        chart_view.onDestroy()
        super.onDestroy()
    }

    override fun onBackPressed() {
        chartService.unwatchChart()
        chart_view.data = emptyList()
        super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val quoteJson = intent.getStringExtra(ChartActivity.EXTRA_QUOTE)
        val quoteClass = WorkspaceFactory().emptyQuote().javaClass
        quote = Gson().fromJson(quoteJson, quoteClass)
        setTheme(R.style.ChartTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)

        setActionBar(chart_header_view)
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)

        App.getAppComponent().inject(this)


        val chartInterval = appStorage.getChartInterval()
        chart_interval_selection.setChartInterval(chartInterval)

        chart_interval_selection.onChartIntervalSelectedListener = object : OnChartIntervalSelectedListener {
            override fun onChartIntervalSelected(interval: ChartInterval) {
                if (!checkApiAvailability()) {
                    return
                }

                Log.d(TAG, "Chart Interval Selected: $interval")
                appStorage.setChartInterval(interval.intervalValue())
                chartService.unwatchChart()
                watchChart(quote.getValue(), quote.getType()?.toLowerCase() == WorkspaceQuoteType.EXPRESSION)
            }

        }
        val displayMetrics = DisplayMetrics()
        windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels

        getQuoteResponseView()?.showActualSymbol = true
        getQuoteResponseView()?.setFields(appStorage.getWorkspace().getExpandedFields(), width)
        (getQuoteResponseView() as? SwipeRevealLayout)?.setLockDrag(true)

        chart_view.onCreate(savedInstanceState)
        chart_view.showVolume = quote.getType()?.toLowerCase() != WorkspaceQuoteType.EXPRESSION
        if (quote.getType()?.toLowerCase() == WorkspaceQuoteType.EXPRESSION) {
            chart_view.type = ChartType.line
        } else {
            chart_view.type = ChartType.fromLegacyChartStyle(appStorage.getChartStyle())
        }

        setupQuoteListener()

        setupNewsListener()
        checkApiAvailability()

    }

    override fun onNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_chart, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.chart_bar -> {
                onChartTypeSelected(ChartType.bar)
                return true
            }
            R.id.chart_line -> {
                onChartTypeSelected(ChartType.line)
                return true
            }
            R.id.chart_candlestick -> {
                onChartTypeSelected(ChartType.candlestick)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onChartTypeSelected(chartType: ChartType) {

        if (!checkApiAvailability()) {
            return
        }

        appStorage.setChartStyle(chartType.getLegacyChartStyle())
        chart_view.type = chartType
    }

    private fun setupQuoteListener() {
        Log.d(TAG, "Watching chart for quote: ${quote.getValue()}")
        val symbol = quote.getValue() ?: return

        val observable = quoteWatchRepository.subscribe(symbol)
        observable.observe(this,
                android.arch.lifecycle.Observer<QuoteWatchResponse> { it -> processQuoteResponse(it) })
        processQuoteResponse(observable.value)

        chartService.watchChart(symbol, appStorage.getChartInterval(), quote.getType()?.toLowerCase() == WorkspaceQuoteType.EXPRESSION)

        val chartLiveData = chartWatchRepository.subscribe()
        chartLiveData.observe(this, android.arch.lifecycle.Observer<Resource<List<ChartData>>> { it -> processChartResponse(it)})

    }

    private fun setupNewsListener() {
        val symbol = quote.getValue() ?: return
        Log.d(TAG, "Watching news for quote: $symbol")

        news_view?.onNewsArticleSelectedListener = this
        news_view?.onAllNewsSelectedListener = this
        news_view?.showAllNewsButton = false
        news_view?.showHeader = false
        val symbolToWatch = quoteWatchRepository.subscribe(symbol).value?.meta?.symbols?.firstOrNull()?.symbol ?: symbol
        Log.d(TAG, "Using Symbol for news: $symbolToWatch")

        newsService.watchSymbolNews(symbolToWatch, 50)

        newsService.getNewsLiveData().observe(this, android.arch.lifecycle.Observer<Resource<NewsWatchResponse>> { it -> processNewsWatchResponse(it?.data)})

    }

    override fun onNewsArticleSelected(article: CategoryArticle) {
        if (!checkApiAvailability()) {
            return
        }

        ViewArticleActivity.start(this, article.storyId, article.title)
    }

    override fun onAllNewsSelected() {
        if (!checkApiAvailability()) {
            return
        }

        if (appConfig.showNewsTab()) {
            NewsActivity.start(this, QuoteListConverter.convertWorkspaceQuoteToQuote(quote), quote.getDisplayName()
                    ?: "")
        } else {
            InstantMarketNewsActivity.start(this, quote)
        }
    }

    private fun processNewsWatchResponse(response: NewsWatchResponse?) {
        var articles = response?.data ?: emptyList()
        Log.d(TAG, "news articles received: $articles")
        news_view?.showHeader = !articles.isEmpty()
        if (articles.size > 5) {
            articles = articles.take(4)
            news_view?.showAllNewsButton = true
        } else {
            news_view?.showAllNewsButton = false
        }
        news_view?.setArticles(articles)
    }

    private fun watchChart(symbol: String?, isExpression: Boolean) {
        if (symbol == null) {
            return
        }
        chartService.watchChart(symbol, appStorage.getChartInterval(), isExpression)
    }

    private fun processQuoteResponse(response: QuoteWatchResponse?) {
        val viewModel = QuoteWatchResponseViewModel(response, quote)
        title = viewModel.getTitleText(false)
        getQuoteResponseView()?.setResponse(viewModel)
        if (appConfig.showSymbolDataInChartTitle()) {
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                chart_header_view.subtitle = viewModel.getLandscapeSubtitle()
            } else {
                chart_header_view.subtitle = viewModel.getPortraitSubtitle()
            }
        }
    }

    private fun getQuoteResponseView(): QuoteResponseView? {
        return findViewById<View>(R.id.quote_data_view) as? QuoteResponseView
    }

    private fun processChartResponse(response: Resource<List<ChartData>>?) {
        when (response?.status ?: Status.ERROR) {
            Status.ERROR -> showChartError(response?.message?.toIntOrNull())
            Status.LOADING -> showChartLoading()
            Status.SUCCESS -> showChartData(response?.data)
        }
    }

    private fun showChartError(@StringRes messageId: Int?) {
        Log.d(TAG, "Error loading chart data")
        val stringCode = messageId ?: R.string.network_error
        val string = resources.getString(stringCode)
        chart_view.showError(string)
        chart_view.showLoadingIndicator(false)
        news_view?.visibility = View.GONE
    }

    private fun showChartLoading() {
        Log.d(TAG, "Chart Data Loading...")
        chart_view.data = listOf()
        chart_view.showLoadingIndicator(true)
    }

    private fun showChartData(data: List<ChartData>?) {
        Log.d(TAG, "Chart Data Received")
        chart_view.showLoadingIndicator(false)
        chart_view.data = data ?: emptyList()
    }

}