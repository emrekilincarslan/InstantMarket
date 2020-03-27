package com.fondova.finance.news

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.fondova.finance.R
import com.fondova.finance.api.model.news.CategoryArticle
import com.fondova.finance.ui.util.DateFormatUtil
import com.fondova.finance.util.ui.dipValue

interface OnNewsArticleSelectedListener {
    fun onNewsArticleSelected(article: CategoryArticle)
}

interface OnAllNewsSelectedListener {
    fun onAllNewsSelected()
}

class NewsListView(context: Context, attr: AttributeSet?): LinearLayout(context, attr) {
    constructor(context: Context): this(context, null)

    private lateinit var listView: RecyclerView

    private lateinit var titleView: TextView
    private lateinit var allNewsTextView: TextView

    var onNewsArticleSelectedListener: OnNewsArticleSelectedListener? = null
    var onAllNewsSelectedListener: OnAllNewsSelectedListener? = null
    var showHeader: Boolean = true
        set(value) {
            field = value
            titleView.visibility = if (value) View.VISIBLE else View.GONE
        }

    var showAllNewsButton: Boolean = true
        set(value) {
            field = value
            allNewsTextView.visibility = if (value) View.VISIBLE else View.GONE
        }
    init {

        orientation = VERTICAL

        attachTitleView(this)

        attachListView(this)

        attachAllNewsButton(this)
    }

    fun setArticles(articles: List<CategoryArticle>) {
        listView.adapter = NewsListAdapter(context, articles, onNewsArticleSelectedListener)
    }

    private fun attachTitleView(view: ViewGroup) {
        titleView = TextView(context)
        val horizontalPadding = context.resources.dipValue(5)
        val verticalPadding = context.resources.dipValue(10)
        titleView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
        titleView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        titleView.setTextColor(ContextCompat.getColor(context, R.color.white))
        titleView.text = context.resources.getText(R.string.related_news)

        view.addView(titleView)
    }

    private fun attachListView(view: ViewGroup) {
        listView = RecyclerView(context)
        ViewCompat.setNestedScrollingEnabled(listView, false)
        listView.layoutManager = LinearLayoutManager(context)

        listView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        view.addView(listView)
    }

    private fun attachAllNewsButton(view: ViewGroup) {
        allNewsTextView = TextView(context)
        allNewsTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
        allNewsTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18.toFloat())

        val horizontalPadding = context.resources.dipValue(5)
        val verticalPadding = context.resources.dipValue(10)
        allNewsTextView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
        allNewsTextView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        allNewsTextView.text = context.resources.getText(R.string.view_all_articles)
        allNewsTextView.setOnClickListener {
            onAllNewsSelectedListener?.onAllNewsSelected()
        }

        view.addView(allNewsTextView)
    }
}

class NewsListAdapter(val context: Context,
                      val articles: List<CategoryArticle>,
                      var onNewsArticleSelectedListener: OnNewsArticleSelectedListener?): RecyclerView.Adapter<NewsListViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): NewsListViewHolder {
        return NewsListViewHolder(NewsListItemView(context, null))
    }

    override fun getItemCount(): Int {
        return articles.size
    }

    override fun onBindViewHolder(holder: NewsListViewHolder?, position: Int) {
        val article = articles[position]
        holder?.newsView?.setArticle(article)
        holder?.newsView?.setOnClickListener {
            val storyId = article.storyId ?: return@setOnClickListener
            onNewsArticleSelectedListener?.onNewsArticleSelected(article)
        }
    }


}

class NewsListViewHolder(val newsView: NewsListItemView): RecyclerView.ViewHolder(newsView)

class NewsListItemView(context: Context, attr: AttributeSet?): LinearLayout(context, attr) {

    private val titleTextView: TextView
    private val subtitleTextView: TextView

    init {
        orientation = VERTICAL

        val horizontalPadding = context.resources.dipValue(5)

        titleTextView = TextView(context)
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.toFloat())
        titleTextView.maxLines = 3
        titleTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
        titleTextView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        titleTextView.setPadding(horizontalPadding, 0, horizontalPadding, 0)
        addView(titleTextView)

        subtitleTextView = TextView(context)
        subtitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.toFloat())
        subtitleTextView.setTextColor(ContextCompat.getColor(context, R.color.grey_8a))
        subtitleTextView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        val bottomPadding = context.resources.dipValue(7)
        subtitleTextView.setPadding(horizontalPadding, 0, horizontalPadding, bottomPadding)

        addView(subtitleTextView)
    }

    fun setArticle(article: CategoryArticle) {
        titleTextView.text = article.title
        subtitleTextView.text = DateFormatUtil.serverDateStringToUiString(article.datetime)
    }
}