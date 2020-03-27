package com.fondova.finance.api.restful;

import android.text.TextUtils;

import com.fondova.finance.api.model.category.CategoriesResponse;
import com.fondova.finance.api.model.category.Category;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class StockRetrofit {

    public static final String EMPTY_SUBCATEGORY_PATH = "";

    private final StockService service;
    private Call<CategoriesResponse> call;
    private boolean ignoreOnFailure = false;

    public StockRetrofit(StockService service) {
        this.service = service;
    }

    public void getCategories(List<Category> parentCategories, DataListener listener) {

        String path = constructPath(parentCategories);
        call = service.requestCategoriesAndSymbols(path);
        call.enqueue(new Callback<CategoriesResponse>() {
            @Override
            public void onResponse(Call<CategoriesResponse> call,
                    Response<CategoriesResponse> response) {
                if (response.isSuccessful()) {
                    listener.onSuccess(response.body());
                } else {
                    listener.onFailure(response.message());
                }
            }

            @Override
            public void onFailure(Call<CategoriesResponse> call, Throwable t) {
                if (ignoreOnFailure) {
                    ignoreOnFailure = false;
                    return;
                }

                listener.onFailure(t.getMessage());

            }
        });
    }

    public void getSymbols(String query, List<Category> parentCategories, DataListener listener) {

        String path = constructPath(parentCategories);
        call = service.requestSymbolsIntoSubCategory(query, path);

        try {
            Response<CategoriesResponse> response = call.execute();
            if (response.isSuccessful()) {
                listener.onSuccess(response.body());
            } else {
                listener.onFailure(response.message());
            }
        } catch (IOException e) {
            if (ignoreOnFailure) {
                ignoreOnFailure = false;
                return;
            }

            listener.onFailure(e.getMessage());
        }
    }

    public static String constructPath(List<Category> parentCategories) {
        if (parentCategories != null && parentCategories.size() > 0) {
            return TextUtils.join("||", parentCategories);
        }
        return EMPTY_SUBCATEGORY_PATH;
    }

    public void cancelRequests() {
        if (call != null && !call.isCanceled()) {
            call.cancel();
            ignoreOnFailure = true;
        }
    }

    public interface StockService {
        @GET("FIMINetDD/GetDDJSON?symbolLimit=501")
        Call<CategoriesResponse> requestCategoriesAndSymbols(@Query("path") String path);

        @GET("FIMINetDD/GetDDJSON?symbolLimit=501")
        Call<CategoriesResponse> requestSymbolsIntoSubCategory(@Query(
                "searchText") String query, @Query("path") String path);
    }

    public interface DataListener {
        void onSuccess(CategoriesResponse response);

        void onFailure(String message);
    }

}
