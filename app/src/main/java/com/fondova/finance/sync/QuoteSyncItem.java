package com.fondova.finance.sync;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.fondova.finance.workspace.WorkspaceQuote;
import com.fondova.finance.workspace.WorkspaceQuoteType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class QuoteSyncItem implements Comparable<QuoteSyncItem>, WorkspaceQuote {

    @SerializedName("request_name")
    public String requestName;

    @SerializedName("display_name")
    public String displayName;

    @SerializedName("order")
    public int order;

    @SerializedName("type")
    public int type;

    public QuoteSyncItem() {
    }

    public QuoteSyncItem(String requestName, String displayName, int order, int type) {
        this.requestName = requestName;
        this.displayName = displayName;
        this.order = order;
        this.type = type;
    }

    @Override
    public int compareTo(@NonNull QuoteSyncItem quoteSyncItem) {
        return this.order - quoteSyncItem.order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuoteSyncItem)) return false;

        QuoteSyncItem that = (QuoteSyncItem) o;

        if (order != that.order) return false;
        return requestName.equals(that.requestName);

    }

    @Override
    public int hashCode() {
        int result = requestName.hashCode();
        result = 31 * result + order;
        return result;
    }

    @Nullable
    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(@NotNull String name) {
        displayName = name;
    }

    @Nullable
    @Override
    public String getType() {
        return WorkspaceQuoteType.Companion.fromQuoteType(type);
    }

    @Override
    public void setType(@NotNull String type) {
        this.type = WorkspaceQuoteType.Companion.toQuoteType(type);
    }

    @Nullable
    @Override
    public String getValue() {
        return requestName;
    }

    @Override
    public void setValue(@NotNull String value) {
        requestName = value;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static QuoteSyncItem fromJson(String json) {
        return new Gson().fromJson(json, QuoteSyncItem.class);
    }

    public static QuoteSyncItem fromWorkspaceQuote(WorkspaceQuote quote) {
        QuoteSyncItem item = new QuoteSyncItem();
        item.requestName = quote.getValue();
        item.displayName = quote.getDisplayName();
        item.type = WorkspaceQuoteType.Companion.toQuoteType(quote.getType());
        return item;
    }
}
