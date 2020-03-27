package com.fondova.finance.news.service

import android.arch.lifecycle.LiveData
import com.fondova.finance.vo.NewsCategory

interface OnNewsListResponseListener {
    fun onNewsListResponse(news: List<NewsCategory>?)
}

interface NewsListService {

    fun fetchNewsList(listener: OnNewsListResponseListener)
    fun saveNewsList(news: List<NewsCategory>)
    fun currentNewsList(): List<NewsCategory>?
    fun newsListLiveData(): LiveData<List<NewsCategory>>
    fun clearCache()

}