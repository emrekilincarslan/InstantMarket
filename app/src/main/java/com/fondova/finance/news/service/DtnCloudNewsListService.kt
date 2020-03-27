package com.fondova.finance.news.service

import android.arch.lifecycle.MutableLiveData
import android.text.TextUtils
import com.fondova.finance.AppExecutors
import com.fondova.finance.api.restful.StockCloudNewsItem
import com.fondova.finance.api.restful.StockCloudNewsListEnvelope
import com.fondova.finance.api.restful.StockCloudNewsListRequest
import com.fondova.finance.vo.NewsCategory

class StockCloudNewsListService: NewsListService {

    var username: String = ""
    var password: String = ""
    private val appExecutors = AppExecutors()
    private var newsCategoriesLiveData: MutableLiveData<List<NewsCategory>>? = null

    override fun fetchNewsList(listener: OnNewsListResponseListener) {
        val request = StockCloudNewsListRequest()
        request.username = username
        request.password = password
        request.successCallback = { response ->
            val items = response?.data?.map { convertStockNewsListToNewsCategoryList(it) }
            appExecutors.mainThread().execute {
                newsListLiveData().value = items
                listener.onNewsListResponse(items)
            }
        }
        request.errorCallback = { error ->
            listener.onNewsListResponse(null)
        }
        request.fetch()
    }

    override fun saveNewsList(news: List<NewsCategory>) {
        appExecutors.mainThread().execute {
            newsListLiveData().value = news
        }
        val request = StockCloudNewsListRequest()
        request.username = username
        request.password = password
        val envelope = StockCloudNewsListEnvelope()
        envelope.data = news.map { convertNewsCategoryListToStockMewsList(it) }
        request.newsList = envelope
        request.fetch()
    }


    override fun currentNewsList(): List<NewsCategory>? {
        return newsCategoriesLiveData?.value
    }

    override fun newsListLiveData(): MutableLiveData<List<NewsCategory>> {
        if (newsCategoriesLiveData == null) {
            newsCategoriesLiveData = MutableLiveData()
        }
        return newsCategoriesLiveData!!
    }

    private fun convertStockNewsListToNewsCategoryList(stockNewsItem: StockCloudNewsItem): NewsCategory {
        val newsCategory = NewsCategory()
        newsCategory.name = stockNewsItem.name
        newsCategory.query = stockNewsItem.query
        newsCategory.keywords = TextUtils.join(" ", stockNewsItem.keywords)
        return newsCategory
    }

    private fun convertNewsCategoryListToStockMewsList(newsCategory: NewsCategory): StockCloudNewsItem {
        val item = StockCloudNewsItem()
        item.name = newsCategory.name
        item.query = newsCategory.query
        item.keywords = newsCategory.keywords?.split(" ") ?: emptyList()
        return item
    }

    override fun clearCache() {
        newsCategoriesLiveData?.value = null
    }
}