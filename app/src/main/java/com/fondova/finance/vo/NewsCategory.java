package com.fondova.finance.vo;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "news_category")
public class NewsCategory implements Parcelable, Comparable<NewsCategory> {

    @SerializedName("id")
    @ColumnInfo(name = "_id")
    @PrimaryKey(autoGenerate = true)
    public long id;

    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("query")
    @Expose
    public String query;

    @SerializedName("keywords")
    @ColumnInfo(name = "keywords")
    public String keywords;

    @ColumnInfo(name = "order")
    @SerializedName("order")
    public int order;

    @ColumnInfo(name = "user_id")
    @SerializedName("user_id")
    public long userId;

    @ColumnInfo(name = "is_quote_related")
    @SerializedName("is_quote_related")
    public boolean isQuoteRelated = false;


    public NewsCategory() {
    }

    @Override
    public String toString() {
        return "NewsCategory{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", query='" + query + '\'' +
                ", keywords='" + keywords + '\'' +
                ", order=" + order +
                ", userId=" + userId +
                '}';
    }


    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeString(this.query);
        dest.writeString(this.keywords);
        dest.writeInt(this.order);
        dest.writeLong(this.userId);
        dest.writeByte(this.isQuoteRelated ? (byte) 1 : (byte) 0);
    }

    protected NewsCategory(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.query = in.readString();
        this.keywords = in.readString();
        this.order = in.readInt();
        this.userId = in.readLong();
        this.isQuoteRelated = in.readByte() != 0;
    }

    public static final Parcelable.Creator<NewsCategory> CREATOR = new Parcelable.Creator<NewsCategory>() {
        @Override public NewsCategory createFromParcel(Parcel source) {
            return new NewsCategory(source);
        }

        @Override public NewsCategory[] newArray(int size) {
            return new NewsCategory[size];
        }
    };

    @Override
    public int compareTo(@NonNull NewsCategory category) {
        return this.order - category.order;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NewsCategory)) return false;

        NewsCategory category = (NewsCategory) o;

        if (id != category.id) return false;
        if (order != category.order) return false;
        if (userId != category.userId) return false;
        if (isQuoteRelated != category.isQuoteRelated) return false;
        if (name != null ? !name.equals(category.name) : category.name != null) return false;
        if (query != null ? !query.equals(category.query) : category.query != null) return false;
        return keywords != null ? keywords.equals(category.keywords) : category.keywords == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (query != null ? query.hashCode() : 0);
        result = 31 * result + (keywords != null ? keywords.hashCode() : 0);
        result = 31 * result + order;
        result = 31 * result + (int) (userId ^ (userId >>> 32));
        result = 31 * result + (isQuoteRelated ? 1 : 0);
        return result;
    }
}
