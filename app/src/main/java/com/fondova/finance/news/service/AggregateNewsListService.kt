package com.fondova.finance.news.service

import android.arch.lifecycle.LiveData
import com.fondova.finance.persistance.AppStorageInterface
import com.fondova.finance.repo.DefaultNewsRepository
import com.fondova.finance.vo.NewsCategory

class AggregateNewsListService(val appStorage: AppStorageInterface, val googleService: LegacyNewsListService, val service: NewsListService): NewsListService {


    override fun fetchNewsList(listener: OnNewsListResponseListener) {
        service.fetchNewsList(object : OnNewsListResponseListener {
            override fun onNewsListResponse(news: List<NewsCategory>?) {
                if (news == null) {
                    fetchGoogleNewsList(listener)
                } else {
                    listener.onNewsListResponse(news)
                }
            }
        })
    }

    private fun fetchGoogleNewsList(listener: OnNewsListResponseListener) {
        googleService.fetchNewsList(object : OnNewsListResponseListener {
            override fun onNewsListResponse(news: List<NewsCategory>?) {
                var newsList = news
                if (newsList == null) {
                    newsList = appStorage.getNewsCategories()
                }
                if (newsList.isEmpty()) {
                    newsList = DefaultNewsRepository().createDefaultNewsCategories()
                }
                saveNewsList(newsList)
                listener.onNewsListResponse(newsList)
            }
        })
    }

    override fun saveNewsList(news: List<NewsCategory>) {
        service.saveNewsList(news)
    }

    override fun currentNewsList(): List<NewsCategory>? {
        return service.currentNewsList()
    }

    override fun newsListLiveData(): LiveData<List<NewsCategory>> {
        return service.newsListLiveData()
    }

    override fun clearCache() {
        service.clearCache()
        googleService.clearCache()
    }

}