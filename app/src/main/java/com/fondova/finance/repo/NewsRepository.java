package com.fondova.finance.repo;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.Handler;
import android.util.Log;

import com.fondova.finance.AppExecutors;
import com.fondova.finance.api.OnLoadedListener;
import com.fondova.finance.api.Resource;
import com.fondova.finance.api.Status;
import com.fondova.finance.api.model.news.NewsWatchResponse;
import com.fondova.finance.api.socket.ApiService;
import com.fondova.finance.news.service.NewsListService;
import com.fondova.finance.persistance.AppStorage;
import com.fondova.finance.vo.NewsCategory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.fondova.finance.api.model.news.NewsData.DEFAULT_LIMIT;

@Singleton
public class NewsRepository {
    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------
    private static final String TAG = NewsRepository.class.getSimpleName();
    private static final int TIMEOUT = 30 * 1000;
    // ---------------------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------------------
    private final ApiService apiService;
    private final AppExecutors appExecutors;
    private HashMap<String, String> requestIdMap = new HashMap<>();
    private HashMap<String, MutableLiveData<Resource<NewsWatchResponse>>> newsCategoryLiveDataMap = new HashMap<>();
    private MutableLiveData<Resource<NewsWatchResponse>> quoteRelatedArticlesLimited = new MutableLiveData<>();
    private MutableLiveData<Resource<NewsWatchResponse>> quoteRelatedArticlesFull = new MutableLiveData<>();
    private AppStorage appStorage;
    //Handle no response from the server
    private Handler handler = new Handler();
    private HashMap<String, Runnable> timeoutRunnables = new HashMap<>();
    private NewsListService newsListService;

    @Inject
    NewsRepository(ApiService apiService,
                   AppExecutors appExecutors,
                   AppStorage appStorage,
                   NewsListService newsListService) {
        this.apiService = apiService;
        this.appExecutors = appExecutors;
        this.appStorage = appStorage;
        this.newsListService = newsListService;
    }

    // ---------------------------------------------------------------------------------------------
    // Public
    // ---------------------------------------------------------------------------------------------
    public void getArticle(String storyId, OnLoadedListener listener) {
        String reqId = UUID.randomUUID().toString();

        apiService.newsPageSnap(reqId, storyId,
                item -> {
                    if (item.status == Status.SUCCESS) {
                        String html = item.data.data.get(0).body;

                        if (listener != null) {
                            listener.onDone(html);
                        }
                    } else {
                        if (listener != null) {
                            listener.onError("some error");
                        }
                    }
                });
    }

    public List<NewsCategory> getNewsCategories() {
        return newsListService.currentNewsList();
    }

    public LiveData<List<NewsCategory>> getNewsCategoriesLiveData() {
        return newsListService.newsListLiveData();
    }

    public LiveData<Resource<NewsWatchResponse>> getNewsCategoryArticlesLiveData(NewsCategory newsCategory) {
        String query = newsCategory.query;
        if (newsCategory.isQuoteRelated) {
            watchQuoteRelatedArticles(query, DEFAULT_LIMIT, quoteRelatedArticlesFull);
            return quoteRelatedArticlesFull;
        } else {
            final String requestId = newsCategoryWatch(query, newsCategory.id);
            MutableLiveData<Resource<NewsWatchResponse>> liveData = new MutableLiveData<>();
            liveData.setValue(Resource.loading());
            newsCategoryLiveDataMap.put(requestId, liveData);
            final Runnable timeoutRunnable = () -> appExecutors.mainThread().execute(
                    () -> newsCategoryLiveDataMap.get(requestId).setValue(Resource.success(NewsWatchResponse.createEmptyResponse())));
            timeoutRunnables.put(requestId, timeoutRunnable);
            handler.postDelayed(timeoutRunnable, TIMEOUT);
            return liveData;
        }
    }

    public LiveData<Resource<NewsWatchResponse>> getChartArticlesLiveData(String symbol, int limit) {
        watchQuoteRelatedArticles(symbol, limit, quoteRelatedArticlesLimited);
        return quoteRelatedArticlesLimited;
    }

    public void unwatch(String query) {
        //remove runnable if any
        removeTimeoutRunnable(query);
        //check whether there is a newsWatch for the query
        if (!requestIdMap.containsKey(query)) return;
        //remove request
        final String requestId = requestIdMap.remove(query);
        //remove LiveData
        newsCategoryLiveDataMap.remove(requestIdMap.get(query));
        appExecutors.networkIO().execute(() ->
                apiService.unwatch(requestId, item -> Log.i(TAG, "unwatch successful for " + query)));

    }

    public void unwatch(long categoryId) {
        unwatch(Long.toString(categoryId));
    }

    public void clearQuoteRelatedArticlesFull() {
        quoteRelatedArticlesFull.setValue(null);
    }

    public void clearQuoteRelatedArticlesLimited() {
        quoteRelatedArticlesLimited.setValue(null);
    }

    public void updateCategories(List<NewsCategory> categories) {
        newsListService.saveNewsList(categories);
    }

    public void insertCategory(NewsCategory category) {
        List<NewsCategory> newsCategories = newsListService.currentNewsList();
        if (newsCategories == null) {
            newsCategories = new ArrayList<>();
        }
        newsCategories.add(category);
        newsListService.saveNewsList(newsCategories);
    }

    public void removeCategories(List<NewsCategory> categories) {
        List<NewsCategory> newsCategories = newsListService.currentNewsList();
        if (newsCategories == null) {
            return;
        }
        List<NewsCategory> itemsToDelete = new ArrayList<>();
        for (NewsCategory category : categories) {
            for (NewsCategory existingCategory : newsCategories) {
                if (category.name.equals(existingCategory.name)) {
                    itemsToDelete.add(existingCategory);
                }
            }
        }

        newsCategories.removeAll(itemsToDelete);

    }

    public void clearRequest(long categoryId) {
        final String stringValue = Long.toString(categoryId);

        removeTimeoutRunnable(stringValue);

        if (requestIdMap.containsKey(stringValue)) {
            apiService.clearNewsWatch(requestIdMap.get(stringValue));
            newsCategoryLiveDataMap.remove(requestIdMap.get(stringValue));
            requestIdMap.remove(stringValue);
        }
    }


    // ---------------------------------------------------------------------------------------------
    // Private
    // ---------------------------------------------------------------------------------------------
    private String newsCategoryWatch(String query, long categoryId) {
        final String requestId = UUID.randomUUID().toString();
        appExecutors.networkIO().execute(() -> {
            requestIdMap.put(Long.toString(categoryId), requestId);
            apiService.newsWatch(requestId, query, item -> appExecutors.mainThread().execute(() -> {

                removeTimeoutRunnable(requestId);

                if (item.status == Status.SUCCESS && newsCategoryLiveDataMap.containsKey(requestId)) {
                    newsCategoryLiveDataMap.get(requestId).setValue(item);
                } else if (item.status == Status.ERROR) {
                    Log.e(TAG, "Watch News Category failed! " + item.message);
                    requestIdMap.remove(Long.toString(categoryId));
                    newsCategoryLiveDataMap.remove(requestId);
                }
            }));
        });
        return requestId;
    }

    private void removeTimeoutRunnable(String requestId) {
        if (handler != null && timeoutRunnables.containsKey(requestId))
            handler.removeCallbacks(timeoutRunnables.get(requestId));
    }

    private void watchQuoteRelatedArticles(String symbol, int limit,
                                           MutableLiveData<Resource<NewsWatchResponse>> liveData) {
        liveData.setValue(Resource.loading());
        appExecutors.networkIO().execute(() -> {
            String requestId = UUID.randomUUID().toString();
            requestIdMap.put(symbol, requestId);
            apiService.watchChartNews(requestId, symbol, limit,
                    item -> appExecutors.mainThread().execute(() -> {
                        if (item.status == Status.SUCCESS) {
                            appExecutors.mainThread().execute(() -> liveData.setValue(item));
                        } else if (item.status == Status.ERROR) {
                            Log.e(TAG, "Watch News failed! " + item.message);
                        }

                    }));
        });
    }

}
