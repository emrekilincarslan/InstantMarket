package com.fondova.finance.workspace.instantmarket

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class StockSettings {

    @Expose
    @SerializedName("data") var workspaces: MutableList<MutableMap<String, Any>>? = mutableListOf()



}