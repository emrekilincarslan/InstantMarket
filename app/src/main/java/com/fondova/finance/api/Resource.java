package com.fondova.finance.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.fondova.finance.api.Status.ERROR;
import static com.fondova.finance.api.Status.LOADING;
import static com.fondova.finance.api.Status.SUCCESS;

public class Resource<T> {
    @NonNull public final Status status;
    @Nullable public final T data;
    @Nullable public final String title;
    @Nullable public final String message;

    private Resource(@NonNull Status status, @Nullable T data, @Nullable String title, @Nullable String message) {
        this.status = status;
        this.data = data;
        this.title = title;
        this.message = message;
    }

    public static <T> Resource<T> success(@NonNull T data) {
        return new Resource<>(SUCCESS, data, null, null);
    }

    public static <T> Resource<T> error(String title, String msg) {
        return new Resource<>(ERROR, null, title, msg);
    }

    public static <T> Resource<T> loading() {
        return new Resource<>(LOADING, null, null, null);
    }

    @Override public String toString() {
        return "Resource{" +
                "status=" + status +
                ", data=" + data +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
