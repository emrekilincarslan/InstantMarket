package com.fondova.finance.news.service

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.fondova.finance.AppExecutors
import com.fondova.finance.db.NewsDao
import com.fondova.finance.sync.NewsCategorySyncItem
import com.fondova.finance.vo.NewsCategory
import com.fondova.finance.workspace.service.GoogleDriveData

class LegacyNewsListService(val legacyNewsDao: NewsDao, val appExecutors: AppExecutors = AppExecutors()): NewsListService {

    override fun fetchNewsList(listener: OnNewsListResponseListener) {
        appExecutors.syncThread().execute {

            var dataString = getLegacyDataString()

            val data: GoogleDriveData = Gson().fromJson(dataString, GoogleDriveData::class.java)
            val newsList = data.news
            if (newsList == null || newsList.isEmpty()) {
                appExecutors.mainThread().execute {
                    listener.onNewsListResponse(null)
                }
                return@execute
            }

            val output = newsList.map { convertSyncItemToNewsCategory(it) }
            appExecutors.mainThread().execute {
                listener.onNewsListResponse(output)
            }
        }
    }

    fun convertSyncItemToNewsCategory(syncItem: NewsCategorySyncItem): NewsCategory {
        val newsCategory = NewsCategory()
        newsCategory.name = syncItem.name
        newsCategory.query = syncItem.query
        newsCategory.keywords = syncItem.keywords
        return newsCategory
    }

    override fun newsListLiveData(): LiveData<List<NewsCategory>> {
        return MutableLiveData() // Never used
    }

    override fun currentNewsList(): List<NewsCategory>? {
        return null // should not be using this live
    }

    override fun saveNewsList(news: List<NewsCategory>) {
        // No longer saving to Google cloud
    }

    private fun getLegacyDataString(): String? {
        val legacyList = legacyNewsDao.loadNewsCategories()
        val googleDriveData = GoogleDriveData()
        googleDriveData.news = legacyList.map { newsCategoryToSyncItem(it) }
        val json = Gson().toJson(googleDriveData)
        return json

    }

    private fun newsCategoryToSyncItem(news: NewsCategory): NewsCategorySyncItem {
        var item = NewsCategorySyncItem()
        item.name = news.name
        item.isQuoteRelated = news.isQuoteRelated
        item.keywords = news.keywords
        item.query = news.query
        item.order = news.order
        return item
    }

    override fun clearCache() {
        // Nothing cached
    }
}