package com.fondova.finance.db.converter;

import android.arch.persistence.room.TypeConverter;

import com.fondova.finance.vo.ChartData;

import java.util.Locale;

public class ChartConverter {
    @TypeConverter
    public ChartData.DualFormatValue rowToDual(String rowValue) {
        if (rowValue == null) return null;
        String[] values = rowValue.split("/");
        ChartData.DualFormatValue dual = new ChartData.DualFormatValue();
        dual.text = values[0];
        dual.number = Double.valueOf(values[1]);
        return dual;
    }

    @TypeConverter
    public String dualToString(ChartData.DualFormatValue dual) {
        return dual == null ? null : String.format(Locale.US, "%s/%f", dual.text, dual.number);
    }
}
