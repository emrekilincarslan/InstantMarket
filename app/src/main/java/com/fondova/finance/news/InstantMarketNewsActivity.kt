package com.fondova.finance.news

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.gson.Gson
import com.fondova.finance.App
import com.fondova.finance.R
import com.fondova.finance.api.Resource
import com.fondova.finance.api.model.news.CategoryArticle
import com.fondova.finance.api.model.news.NewsWatchResponse
import com.fondova.finance.api.model.quote.QuoteWatchResponse
import com.fondova.finance.api.news.NewsService
import com.fondova.finance.workspace.WorkspaceQuote
import com.fondova.finance.quotes.QuoteWatchResponseViewModel
import com.fondova.finance.repo.QuoteWatchRepository
import com.fondova.finance.ui.SessionAwareActivity
import com.fondova.finance.ui.news.view.ViewArticleActivity
import com.fondova.finance.workspace.WorkspaceFactory
import kotlinx.android.synthetic.main.activity_instant_market_news.*
import javax.inject.Inject

class InstantMarketNewsActivity: SessionAwareActivity(), OnNewsArticleSelectedListener {

    @Inject
    lateinit var newsService: NewsService
    @Inject
    lateinit var quoteWatchRepository: QuoteWatchRepository

    lateinit var quote: WorkspaceQuote

    companion object {

        val quoteExtraKey = "title.extra.key"

        fun start(context: Context, quote: WorkspaceQuote) {
            val intent = Intent(context, InstantMarketNewsActivity::class.java)
            val json = Gson().toJson(WorkspaceFactory().copyQuote(quote))
            intent.putExtra(quoteExtraKey, json)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.getAppComponent().inject(this)

        setContentView(R.layout.activity_instant_market_news)
        setTheme(R.style.ChartTheme)
        setActionBar(title_toolbar)
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)

        val quoteJson = intent.getStringExtra(quoteExtraKey)
        quote = Gson().fromJson(quoteJson, WorkspaceFactory().emptyQuote().javaClass)
        val symbol = quote.getValue()
        if (symbol == null) {
            return
        }
        quoteWatchRepository.subscribe(symbol).observe(this,
                android.arch.lifecycle.Observer<QuoteWatchResponse> { it -> processQuoteResponse(it) })

        news_list_view?.showHeader = false
        news_list_view?.showAllNewsButton = false
        news_list_view?.onNewsArticleSelectedListener = this

        newsService.getNewsLiveData().observe(this, android.arch.lifecycle.Observer<Resource<NewsWatchResponse>> { it -> processNewsWatchResponse(it?.data)})
    }

    override fun onNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun processQuoteResponse(response: QuoteWatchResponse?) {
        val viewModel = QuoteWatchResponseViewModel(response, quote)
        val relatedNewsText = resources.getString(R.string.related_news)
        val titleText = "${viewModel.getTitleText()} $relatedNewsText"
        title = titleText
        description_text_view?.text = viewModel.getActualSymbol()
    }


    private fun processNewsWatchResponse(response: NewsWatchResponse?) {
        var articles = response?.data ?: emptyList()
        news_list_view?.setArticles(articles)
    }

    override fun onNewsArticleSelected(article: CategoryArticle) {
        if (!checkApiAvailability()) {
            return
        }

        ViewArticleActivity.start(this, article.storyId, article.title)
    }


}