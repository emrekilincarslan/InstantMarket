package com.fondova.finance.news.service

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import com.google.gson.Gson
import com.fondova.finance.AppExecutors
import com.fondova.finance.sync.NewsCategorySyncItem
import com.fondova.finance.vo.NewsCategory
import com.fondova.finance.workspace.service.GoogleDriveData
import com.fondova.finance.workspace.service.GoogleDriveService

class GoogleNewsListService(val context: Context, val appExecutors: AppExecutors = AppExecutors()): NewsListService {

    private var googleService: GoogleDriveService = GoogleDriveService.shared

    override fun fetchNewsList(listener: OnNewsListResponseListener) {
        googleService.connectToGoogleClient(context) { apiClient, connected ->
            appExecutors.syncThread().execute {
                if (!connected) {
                    appExecutors.mainThread().execute {
                        listener.onNewsListResponse(null)
                    }
                    return@execute
                }
                if (apiClient == null) {
                    appExecutors.mainThread().execute {
                        listener.onNewsListResponse(null)
                    }
                    return@execute
                }
                var dataString = googleService.readFromDrive(apiClient)
                apiClient.disconnect()
                if (dataString == null) {
                    appExecutors.mainThread().execute {
                        listener.onNewsListResponse(null)
                    }
                }
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

    override fun clearCache() {
        // Nothing is cached
    }

}

