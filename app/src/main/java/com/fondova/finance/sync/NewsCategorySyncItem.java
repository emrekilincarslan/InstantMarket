package com.fondova.finance.sync;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;


public class NewsCategorySyncItem implements Comparable<NewsCategorySyncItem> {

    @SerializedName("name")
    public String name;

    @SerializedName("query")
    public String query;

    @SerializedName("keywords")
    public String keywords;

    @SerializedName("order")
    public int order;

    @SerializedName("is_quote_related")
    public boolean isQuoteRelated = false;


    public NewsCategorySyncItem() {
    }

    public NewsCategorySyncItem(String name, String query, String keywords, int order, boolean isQuoteRelated) {
        this.name = name;
        this.query = query;
        this.keywords = keywords;
        this.order = order;
        this.isQuoteRelated = isQuoteRelated;
    }

    @Override
    public String toString() {
        return "NewsCategorySyncItem{" +
                "name='" + name + '\'' +
                ", query='" + query + '\'' +
                ", keywords='" + keywords + '\'' +
                ", order=" + order +
                '}';
    }

    @Override
    public int compareTo(@NonNull NewsCategorySyncItem category) {
        return this.order - category.order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NewsCategorySyncItem)) return false;

        NewsCategorySyncItem that = (NewsCategorySyncItem) o;

        if (order != that.order) return false;
        if (isQuoteRelated != that.isQuoteRelated) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (query != null ? !query.equals(that.query) : that.query != null) return false;
        return keywords != null ? keywords.equals(that.keywords) : that.keywords == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (query != null ? query.hashCode() : 0);
        result = 31 * result + (keywords != null ? keywords.hashCode() : 0);
        result = 31 * result + order;
        result = 31 * result + (isQuoteRelated ? 1 : 0);
        return result;
    }
}
