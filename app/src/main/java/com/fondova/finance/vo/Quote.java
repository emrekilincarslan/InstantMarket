package com.fondova.finance.vo;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.fondova.finance.enums.QuoteType;
import com.fondova.finance.workspace.WorkspaceQuote;
import com.fondova.finance.workspace.WorkspaceQuoteType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Entity

public class Quote implements Parcelable, WorkspaceQuote {

    @ColumnInfo(name = "_id")
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "request_name")
    @SerializedName("symbol")
    public String requestName;

    @ColumnInfo(name = "display_name")
    @SerializedName("displayName")
    public String displayName;

    @ColumnInfo(name = "market")
    @SerializedName("market")
    public String market;

    @ColumnInfo(name = "symbolsFirst")
    @SerializedName("symbolsFirst")
    public String symbolsFirst;

    @ColumnInfo(name = "description")
    @SerializedName("description")
    public String description;

    @ColumnInfo(name = "expirationDate")
    @SerializedName("expirationDate")
    public String expirationDate;

    @ColumnInfo(name = "order")
    @SerializedName("order")
    public int order;

    @ColumnInfo(name = "type")
    @SerializedName("type")
    public @QuoteType int type;

    @Ignore
    public boolean isSelected = false;

    @ColumnInfo(name = "is_not_permissioned")
    public boolean isNotPermissioned = false;

    @ColumnInfo(name = "is_invalid")
    public boolean isInvalid = false;

    @ColumnInfo(name = "user_id")
    public long userId;

    public String newsRequestSymbol() {
        return TextUtils.isEmpty(symbolsFirst) ? requestName : symbolsFirst;
    }

    @Override
    public String toString() {
        return "Quote{" +
                "id=" + id +
                ", name='" + requestName + '\'' +
                ", description='" + description + '\'' +
                ", expirationDate='" + expirationDate + '\'' +
                ", order=" + order +
                ", type=" + type +
                ", isSelected=" + isSelected +
                ", isNotPermissioned=" + isNotPermissioned +
                ", isInvalid=" + isInvalid +
                ", userId=" + userId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Quote)) return false;

        Quote quote = (Quote) o;

        if (isNotPermissioned != quote.isNotPermissioned) return false;
        return isInvalid == quote.isInvalid;

    }

    @Override
    public int hashCode() {
        int result = (isNotPermissioned ? 1 : 0);
        result = 31 * result + (isInvalid ? 1 : 0);
        return result;
    }


    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.requestName);
        dest.writeString(this.market);
        dest.writeString(this.symbolsFirst);
        dest.writeString(this.description);
        dest.writeString(this.expirationDate);
        dest.writeInt(this.order);
        dest.writeInt(this.type);
        dest.writeByte(this.isSelected ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isNotPermissioned ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isInvalid ? (byte) 1 : (byte) 0);
        dest.writeLong(this.userId);
    }

    public Quote() {
    }

    protected Quote(Parcel in) {
        this.id = in.readLong();
        this.requestName = in.readString();
        this.market = in.readString();
        this.symbolsFirst = in.readString();
        this.description = in.readString();
        this.expirationDate = in.readString();
        this.order = in.readInt();
        this.type = in.readInt();
        this.isSelected = in.readByte() != 0;
        this.isNotPermissioned = in.readByte() != 0;
        this.isInvalid = in.readByte() != 0;
        this.userId = in.readLong();
    }

    public static final Creator<Quote> CREATOR = new Creator<Quote>() {
        @Override public Quote createFromParcel(Parcel source) {
            return new Quote(source);
        }

        @Override public Quote[] newArray(int size) {
            return new Quote[size];
        }
    };

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
}
