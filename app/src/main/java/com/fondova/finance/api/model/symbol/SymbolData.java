package com.fondova.finance.api.model.symbol;

import com.google.gson.annotations.SerializedName;

class SymbolData {

    @SerializedName("sympat") private String sympat;
    @SerializedName("symbolFormat") private String symbolFormat;

    SymbolData(String sympat) {
        this.sympat = sympat;
    }

    public String getSymbolFormat() {
        return symbolFormat;
    }

    public void setSymbolFormat(String symbolFormat) {
        this.symbolFormat = symbolFormat;
    }
}
