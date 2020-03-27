package com.fondova.finance.repo;

import android.arch.lifecycle.MutableLiveData;

import com.fondova.finance.AppExecutors;
import com.fondova.finance.api.Resource;
import com.fondova.finance.api.Status;
import com.fondova.finance.api.model.category.CategoriesResponse;
import com.fondova.finance.api.model.category.Category;
import com.fondova.finance.api.model.symbol.SymbolSearchResponse;
import com.fondova.finance.api.restful.StockRetrofit;
import com.fondova.finance.api.socket.ApiService;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CategoriesRepository {


    // ---------------------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------------------
    private final MutableLiveData<Resource<SymbolSearchResponse>> symbolsLiveData =
            new MutableLiveData<>();
    private final MutableLiveData<Resource<CategoriesResponse>> categoriesLiveData =
            new MutableLiveData<>();
    private final ApiService apiService;
    private final StockRetrofit stockRetrofit;
    private final AppExecutors appExecutors;


    @Inject
    public CategoriesRepository(ApiService apiService,
                                StockRetrofit stockRetrofit,
                                AppExecutors appExecutors) {
        this.apiService = apiService;
        this.stockRetrofit = stockRetrofit;
        this.appExecutors = appExecutors;
    }

    // ---------------------------------------------------------------------------------------------
    // Public
    // ---------------------------------------------------------------------------------------------
    public MutableLiveData<Resource<CategoriesResponse>> getAllCategories() {
        return getSubCategories(null);
    }

    public MutableLiveData<Resource<CategoriesResponse>> getSubCategories(
            List<Category> categories) {
        categoriesLiveData.setValue(Resource.loading());

        stockRetrofit.getCategories(categories, new StockRetrofit.DataListener() {

            @Override
            public void onSuccess(CategoriesResponse response) {
                appExecutors.mainThread().execute(() ->
                        categoriesLiveData.setValue(Resource.success(response))
                );
            }

            @Override
            public void onFailure(String message) {
                appExecutors.mainThread().execute(() ->
                        categoriesLiveData.setValue(Resource.error(Status.ERROR.name(), message))
                );
            }
        });

        return categoriesLiveData;
    }


    public MutableLiveData<Resource<CategoriesResponse>> getSymbolsIntoSubCategory(
            List<Category> pathCategories, String query) {
        categoriesLiveData.setValue(Resource.loading());


        appExecutors.networkIO().execute(() ->
                stockRetrofit.getSymbols(query, pathCategories, new StockRetrofit.DataListener() {

                    @Override
                    public void onSuccess(CategoriesResponse response) {
                        appExecutors.mainThread().execute(() ->
                                categoriesLiveData.setValue(Resource.success(response))
                        );
                    }

                    @Override
                    public void onFailure(String message) {
                        appExecutors.mainThread().execute(() ->
                                categoriesLiveData.setValue(Resource.error(Status.ERROR.name(), message))
                        );
                    }
                }));

        return categoriesLiveData;
    }

    public MutableLiveData<Resource<SymbolSearchResponse>> getSymbolsForQuery(String query) {
        symbolsLiveData.setValue(Resource.loading());

        appExecutors.networkIO().execute(() ->
                apiService.searchForSymbols(query,
                        symbolsResource -> appExecutors.mainThread().execute(
                                () -> symbolsLiveData.setValue(symbolsResource))
                )
        );

        return symbolsLiveData;
    }

    public void stopRestfulRequest() {
        stockRetrofit.cancelRequests();
    }
}
