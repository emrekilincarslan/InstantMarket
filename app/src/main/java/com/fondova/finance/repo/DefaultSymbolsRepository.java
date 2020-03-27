package com.fondova.finance.repo;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.fondova.finance.enums.QuoteType;
import com.fondova.finance.workspace.Workspace;
import com.fondova.finance.persistance.AppStorage;
import com.fondova.finance.persistance.QuoteListConverter;
import com.fondova.finance.sync.QuoteSyncItem;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DefaultSymbolsRepository {

    private static final String LABEL = "label";

    private final AppStorage appStorage;
    private final Gson gson;


    @Inject
    public DefaultSymbolsRepository(AppStorage appStorage, Gson gson) {
        this.appStorage = appStorage;
        this.gson = gson;
    }

    public void insertDefaultSymbols() {

        Workspace workspace = createDefaultWorkspace();
        appStorage.setWorkspace(workspace);
    }

    @NonNull
    public Workspace createDefaultWorkspace() {
        List<QuoteSyncItem> quotes = new ArrayList<>();
        Data data = gson.fromJson(DEFAULT_QUOTES_JSON, Data.class);
        for (int i = 0; i < data.defaultQuotes.size(); i++) {
            DefaultQuote defaultQuote = data.defaultQuotes.get(i);
            QuoteSyncItem q = new QuoteSyncItem();
            q.requestName = defaultQuote.name;
            q.displayName = defaultQuote.name;
            q.order = i;
            switch (defaultQuote.type) {
                case LABEL:
                    q.type = QuoteType.LABEL;
                    break;
                default:
                    q.type = QuoteType.SYMBOL;
                    break;
            }
            quotes.add(q);
        }

        return QuoteListConverter.Companion.convertQuoteSyncItemListToWorkspace(quotes);
    }

    private static class Data {
        @SerializedName("data")
        @Expose
        List<DefaultQuote> defaultQuotes = null;
    }

    private static class DefaultQuote {
        @SerializedName("name")
        @Expose
        String name;
        @SerializedName("type")
        @Expose
        String type;
    }

    private static final String DEFAULT_QUOTES_JSON = "{\n" +
            "\"data\":[\n" +
            "{ \"name\":\"US AG\", \"type\":\"label\" }\n" +
            ",\n" +
            "{ \"name\":\"@C@1\", \"market\":3, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"@W@1\", \"market\":3, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"@KW@1\", \"market\":6, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"@MW@1\", \"market\":9, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"@S@1\", \"market\":3, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"@SM@1\", \"market\":3, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"@BO@1\", \"market\":3, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"@RR@1\", \"market\":3, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"@KC@1\", \"market\":25, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"@SB@1\", \"market\":25, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"@CC@1\", \"market\":25, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"@CT@1\", \"market\":25, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"@OJ@1\", \"market\":25, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"@LE@1\", \"market\":4, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"@GF@1\", \"market\":4, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"@HE@1\", \"market\":4, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"US ENERGY\", \"type\":\"label\" }\n" +
            ",\n" +
            "{ \"name\":\"QCL@1\", \"market\":247, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"QRB@1\", \"market\":247, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"QHO@1\", \"market\":247, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"QNG@1\", \"market\":247, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"@AC@1\", \"market\":3, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"US STOCK MARKET\", \"type\":\"label\" }\n" +
            ",\n" +
            "{ \"name\":\"INDU.X\", \"market\":236, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"COMPX.X\", \"market\":236, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"INX.X\", \"market\":2, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"CURRENCIES\", \"type\":\"label\" }\n" +
            ",\n" +
            "{ \"name\":\"@EU@1\", \"market\":4, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"@BP@1\", \"market\":4, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"@JY@1\", \"market\":4, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"@CD@1\", \"market\":4, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"@SF@1\", \"market\":4, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"@AD@1\", \"market\":4, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"US METALS\", \"type\":\"label\" }\n" +
            ",\n" +
            "{ \"name\":\"QGC@1\", \"market\":262, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"QSI@1\", \"market\":262, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"QHG@1\", \"market\":262, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"QPL@1\", \"market\":247, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"QPA@1\", \"market\":247, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"TREASURY\", \"type\":\"label\" }\n" +
            ",\n" +
            "{ \"name\":\"@TU@1\", \"market\":3, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"@FV@1\", \"market\":3, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"@TY@1\", \"market\":3, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"@US@1\", \"market\":3, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"WORLD\", \"type\":\"label\" }\n" +
            ",\n" +
            "{ \"name\":\"CA@1\", \"market\":383, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"PM@1\", \"market\":383, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"QK@1\", \"market\":415, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"AWM@1\", \"market\":43, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"WAW@1\", \"market\":43, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"PG@1\", \"market\":383, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"LRC@1\", \"market\":415, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"QW@1\", \"market\":415, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"QC@1\", \"market\":415, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"KPO@1\", \"market\":345, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"EB@1\", \"market\":64, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"GAS@1\", \"market\":64, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"GX@1\", \"market\":43, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"XG@1\", \"market\":295, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"MT@1\", \"market\":385, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"LF@1\", \"market\":384, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"SPI@1\", \"market\":43, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"BD@1\", \"market\":295, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"BL@1\", \"market\":295, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"EZ@1\", \"market\":295, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            ",\n" +
            "{ \"name\":\"JG@1\", \"market\":42, \"vendor\":\"DTN\", \"type\":\"symbol\" }\n" +
            "]\n" +
            "}";
}
