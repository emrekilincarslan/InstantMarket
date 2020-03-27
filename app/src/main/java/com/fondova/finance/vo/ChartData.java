package com.fondova.finance.vo;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import static android.R.attr.id;
import static android.arch.persistence.room.ForeignKey.CASCADE;


@Entity(tableName = "chart_data")
@ForeignKey(entity = Quote.class,
            parentColumns = "_id",
            childColumns = "quote_id",
            onDelete = CASCADE)
public class ChartData {

    @ColumnInfo(name = "quote_id")
    public long quoteId;

    @ColumnInfo(name = "interval")
    public int interval;

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "date_time")
    @SerializedName("DateTime")
    @Expose
    public String dateTime;

    @ColumnInfo(name = "open")
    @SerializedName("Open")
    @Expose
    public DualFormatValue open;

    @ColumnInfo(name = "high")
    @SerializedName("High")
    @Expose
    public DualFormatValue high;

    @ColumnInfo(name = "low")
    @SerializedName("Low")
    @Expose
    public DualFormatValue low;

    @ColumnInfo(name = "close")
    @SerializedName("Close")
    @Expose
    public DualFormatValue close;

    @ColumnInfo(name = "volume")
    @SerializedName("Volume")
    @Expose
    public DualFormatValue volume;

    @ColumnInfo(name = "open_int")
    @SerializedName("OpenInt")
    @Expose
    public DualFormatValue openInt;

    @ColumnInfo(name = "is_null")
    @SerializedName("IsNull")
    @Expose
    public boolean isNull = false;

    @Override
    public String toString() {
        return "ChartData{" +
                "id=" + id +
                ", quoteId=" + quoteId +
                ", interval=" + interval +
                ", dateTime='" + dateTime + '\'' +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                ", volume=" + volume +
                ", openInt=" + openInt +
                ", isNull= " + isNull +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChartData) return dateTime.equalsIgnoreCase(((ChartData) obj).dateTime);
        return super.equals(obj);
    }


    public boolean hasEqualValues(ChartData other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        ChartData chartData = other;

        if (quoteId != chartData.quoteId) return false;
        if (interval != chartData.interval) return false;
        if (isNull != chartData.isNull) return false;
        if (dateTime != null ? !dateTime.equals(chartData.dateTime) : chartData.dateTime != null)
            return false;
        if (open != null ? !open.equals(chartData.open) : chartData.open != null) return false;
        if (high != null ? !high.equals(chartData.high) : chartData.high != null) return false;
        if (low != null ? !low.equals(chartData.low) : chartData.low != null) return false;
        if (close != null ? !close.equals(chartData.close) : chartData.close != null) return false;
        if (volume != null ? !volume.equals(chartData.volume) : chartData.volume != null)
            return false;
        return openInt != null ? openInt.equals(chartData.openInt) : chartData.openInt == null;
    }

    public static class DualFormatValue {

        @ColumnInfo(name = "text")
        @SerializedName("text")
        public String text;

        @ColumnInfo(name = "number")
        @SerializedName("number")
        public Double number;


        @Override
        public String toString() {
            return text;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DualFormatValue that = (DualFormatValue) o;

            if (text != null ? !text.equals(that.text) : that.text != null) return false;
            return number != null ? number.equals(that.number) : that.number == null;

        }
    }

}
