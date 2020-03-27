package com.fondova.finance.api.model.category;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Category {

    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("more")
    @Expose
    public boolean more;


    // Keep it. It's used while composing API request for deep diving into sub categories.
    @Override
    public String toString() {
        return name;
    }
}
