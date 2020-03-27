package com.fondova.finance.vo;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(tableName = "quote_value", foreignKeys = {
        @ForeignKey(entity = Quote.class,
                parentColumns = "_id",
                childColumns = "quote_id",
                onDelete = CASCADE)},
        indices = {@Index("quote_id")})

public class QuoteValue implements Parcelable {

    public static final String[] WATCH_FIELDS = new String[]{
            "Last", "LastTicknum", "UserDescription", "ActualSymbol",
            "Change", "IssueDescription", "PctChange", "High", "Low", "Open",
            "Bid", "Ask", "CumVolume", "TradeDateTime", "SettlementPrice", "Settledate",
            "QuoteDelay"
    };

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "quote_value_id")
    public int id;

    @ColumnInfo(name = "quote_id")
    public long quoteId;

    @ColumnInfo(name = "last")
    @SerializedName("Last")
    public String last;

    @ColumnInfo(name = "last_ticknum")
    @SerializedName("LastTicknum")
    public String lastTicknum;

    @ColumnInfo(name = "change")
    @SerializedName("Change")
    public String change;

    @ColumnInfo(name = "user_description")
    @SerializedName("UserDescription")
    public String userDescription;


    @ColumnInfo(name = "issue_description")
    @SerializedName("IssueDescription")
    public String issueDescription;

    @ColumnInfo(name = "pct_change")
    @SerializedName("PctChange")
    public String pctChange;


    @ColumnInfo(name = "quote_delay")
    @SerializedName("QuoteDelay")
    public String quoteDelay;

    @ColumnInfo(name = "actual_symbol")
    @SerializedName("ActualSymbol")
    public String actualSymbol;

    @ColumnInfo(name = "cum_volume")
    @SerializedName("CumVolume")
    public Integer volume;

    @ColumnInfo(name = "trade_date_time")
    @SerializedName("TradeDateTime")
    public String tradeDateTime;

    @ColumnInfo(name = "open_price")
    @SerializedName("Open")
    public String openPrice;

    @ColumnInfo(name = "high_price")
    @SerializedName("High")
    public String highPrice;

    @ColumnInfo(name = "low_price")
    @SerializedName("Low")
    public String lowPrice;

    @ColumnInfo(name = "best_bid")
    @SerializedName("Bid")
    public String currentBestBid;

    @ColumnInfo(name = "current_ask")
    @SerializedName("Ask")
    public String currentAsk;

    @ColumnInfo(name = "settle_date")
    @SerializedName("Settledate")
    public String settleDate;

    @ColumnInfo(name = "settlement_price")
    @SerializedName("SettlementPrice")
    public String settlementPrice;


    @Override
    public String toString() {
        return "QuoteValue{" +
                "id=" + id +
                ", quoteId=" + quoteId +
                ", last='" + last + '\'' +
                ", lastTicknum='" + lastTicknum + '\'' +
                ", change='" + change + '\'' +
                ", userDescription='" + userDescription + '\'' +
                ", issueDescription='" + issueDescription + '\'' +
                ", pctChange='" + pctChange + '\'' +
                ", quoteDelay='" + quoteDelay + '\'' +
                ", actualSymbol='" + actualSymbol + '\'' +
                ", volume=" + volume +
                ", tradeDateTime='" + tradeDateTime + '\'' +
                ", openPrice='" + openPrice + '\'' +
                ", highPrice='" + highPrice + '\'' +
                ", lowPrice='" + lowPrice + '\'' +
                ", currentBestBid='" + currentBestBid + '\'' +
                ", currentAsk='" + currentAsk + '\'' +
                ", settleDate='" + settleDate + '\'' +
                ", settlementPrice='" + settlementPrice + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QuoteValue that = (QuoteValue) o;

        if (last != null ? !last.equals(that.last) : that.last != null) return false;
        if (lastTicknum != null ? !lastTicknum.equals(that.lastTicknum) : that.lastTicknum != null)
            return false;
        if (change != null ? !change.equals(that.change) : that.change != null) return false;
        if (userDescription != null ? !userDescription.equals(that.userDescription) : that.userDescription != null)
            return false;
        if (issueDescription != null ? !issueDescription.equals(that.issueDescription) : that.issueDescription != null)
            return false;
        if (pctChange != null ? !pctChange.equals(that.pctChange) : that.pctChange != null)
            return false;
        if (quoteDelay != null ? !quoteDelay.equals(that.quoteDelay) : that.quoteDelay != null)
            return false;
        if (actualSymbol != null ? !actualSymbol.equals(that.actualSymbol) : that.actualSymbol != null)
            return false;
        if (volume != null ? !volume.equals(that.volume) : that.volume != null) return false;
        if (tradeDateTime != null ? !tradeDateTime.equals(that.tradeDateTime) : that.tradeDateTime != null)
            return false;
        if (openPrice != null ? !openPrice.equals(that.openPrice) : that.openPrice != null)
            return false;
        if (highPrice != null ? !highPrice.equals(that.highPrice) : that.highPrice != null)
            return false;
        if (lowPrice != null ? !lowPrice.equals(that.lowPrice) : that.lowPrice != null)
            return false;
        if (currentBestBid != null ? !currentBestBid.equals(that.currentBestBid) : that.currentBestBid != null)
            return false;
        if (currentAsk != null ? !currentAsk.equals(that.currentAsk) : that.currentAsk != null)
            return false;
        if (settleDate != null ? !settleDate.equals(that.settleDate) : that.settleDate != null)
            return false;
        return settlementPrice != null ? settlementPrice.equals(that.settlementPrice) : that.settlementPrice == null;

    }

    @Override
    public int hashCode() {
        int result = last != null ? last.hashCode() : 0;
        result = 31 * result + (lastTicknum != null ? lastTicknum.hashCode() : 0);
        result = 31 * result + (change != null ? change.hashCode() : 0);
        result = 31 * result + (userDescription != null ? userDescription.hashCode() : 0);
        result = 31 * result + (issueDescription != null ? issueDescription.hashCode() : 0);
        result = 31 * result + (pctChange != null ? pctChange.hashCode() : 0);
        result = 31 * result + (quoteDelay != null ? quoteDelay.hashCode() : 0);
        result = 31 * result + (actualSymbol != null ? actualSymbol.hashCode() : 0);
        result = 31 * result + (volume != null ? volume.hashCode() : 0);
        result = 31 * result + (tradeDateTime != null ? tradeDateTime.hashCode() : 0);
        result = 31 * result + (openPrice != null ? openPrice.hashCode() : 0);
        result = 31 * result + (highPrice != null ? highPrice.hashCode() : 0);
        result = 31 * result + (lowPrice != null ? lowPrice.hashCode() : 0);
        result = 31 * result + (currentBestBid != null ? currentBestBid.hashCode() : 0);
        result = 31 * result + (currentAsk != null ? currentAsk.hashCode() : 0);
        result = 31 * result + (settleDate != null ? settleDate.hashCode() : 0);
        result = 31 * result + (settlementPrice != null ? settlementPrice.hashCode() : 0);
        return result;
    }


    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeLong(this.quoteId);
        dest.writeString(this.last);
        dest.writeString(this.lastTicknum);
        dest.writeString(this.change);
        dest.writeString(this.userDescription);
        dest.writeString(this.issueDescription);
        dest.writeString(this.pctChange);
        dest.writeString(this.quoteDelay);
        dest.writeString(this.actualSymbol);
        dest.writeValue(this.volume);
        dest.writeString(this.tradeDateTime);
        dest.writeString(this.openPrice);
        dest.writeString(this.highPrice);
        dest.writeString(this.lowPrice);
        dest.writeString(this.currentBestBid);
        dest.writeString(this.currentAsk);
        dest.writeString(this.settleDate);
        dest.writeString(this.settlementPrice);
    }

    public QuoteValue() {
    }

    protected QuoteValue(Parcel in) {
        this.id = in.readInt();
        this.quoteId = in.readLong();
        this.last = in.readString();
        this.lastTicknum = in.readString();
        this.change = in.readString();
        this.userDescription = in.readString();
        this.issueDescription = in.readString();
        this.pctChange = in.readString();
        this.quoteDelay = in.readString();
        this.actualSymbol = in.readString();
        this.volume = (Integer) in.readValue(Integer.class.getClassLoader());
        this.tradeDateTime = in.readString();
        this.openPrice = in.readString();
        this.highPrice = in.readString();
        this.lowPrice = in.readString();
        this.currentBestBid = in.readString();
        this.currentAsk = in.readString();
        this.settleDate = in.readString();
        this.settlementPrice = in.readString();
    }

    public static final Parcelable.Creator<QuoteValue> CREATOR = new Parcelable.Creator<QuoteValue>() {
        @Override public QuoteValue createFromParcel(Parcel source) {
            return new QuoteValue(source);
        }

        @Override public QuoteValue[] newArray(int size) {
            return new QuoteValue[size];
        }
    };
}
