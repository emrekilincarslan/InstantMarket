package com.fondova.finance.db;

import android.content.pm.PackageManager;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.fondova.finance.App;
import com.fondova.finance.R;
import com.fondova.finance.api.model.quote.QuoteWatchResponse;
import com.fondova.finance.api.model.quote.QuoteWatchResponseFieldExtensionsKt;
import com.fondova.finance.workspace.WorkspaceQuote;
import com.fondova.finance.repo.TextsRepository;

import javax.inject.Inject;

public class TextsDao implements TextsRepository {

    private static final int MAX_QUOTE_NAME_LENGTH = 6;

    private App app;

    @Inject
    public TextsDao(App app) {
        this.app = app;
    }

    @Override
    public String aboutHtmlFile() {
        return "about.html";
    }

    @Override
    public String privacyHtmlUrl() {
        return "https://www.stock.com/agriculture/legal/";
    }

    @Override
    public String eulaHtmlFile() {
        return "eula.html";
    }

    @Override
    public String supportEmail() {

        return getString(R.string.email_address);
    }

    @Override
    public String supportSubject(String username) {
        String appName = app.getString(R.string.app_name);
        return String.format("%s Android Developer Support - %s", appName, username);
    }

    @Override
    public String supportBody(String uuid) {
        String appName = app.getString(R.string.app_name);
        return String.format("%s for Android Support Email\nSupport Identifier: %s", appName, uuid);
    }

    @Override
    public String appNameForApiCalls() {
        return app.getString(R.string.octane_app_name);
    }

    @Override
    public String appVersion() {
        String appVersionDisplayName;
        Integer appVersionCode;
        String packageName = app.getPackageName();
        try {
            String appVersionName = app.getPackageManager().getPackageInfo(packageName,
                    PackageManager.GET_META_DATA).versionName;
            appVersionCode = app.getPackageManager().getPackageInfo(packageName,
                    PackageManager.GET_META_DATA).versionCode;
            String appVersionCodeString = Integer.toString(appVersionCode);
            appVersionDisplayName = String.format("%s.%s", appVersionName, appVersionCodeString);
        } catch (PackageManager.NameNotFoundException e) {
            appVersionDisplayName = null;
            e.printStackTrace();
        }
        return appVersionDisplayName;
    }

    @Override
    public String refreshRate5SecondsString() {
        return app.getString(R.string._5_seconds);
    }

    @Override
    public String refreshRate30SecondsString() {
        return app.getString(R.string._30_seconds);
    }

    @Override
    public String refreshRate60SecondsString() {
        return app.getString(R.string._60_seconds);
    }

    @Override
    public String refreshRate5MinutesString() {
        return app.getString(R.string._5_minutes);
    }

    @Override
    public String refreshRateOffString() {
        return app.getString(R.string.off_manual_refresh);
    }

    @Override
    public String autoRefreshTitle(String refreshRate) {
        return app.getString(R.string.auto_refresh_, refreshRate);
    }

    @Override
    public String symbolsCount(int count, int limit) {
        return app.getString(R.string.symbols_count, count, limit);
    }

    @Override
    public String categories() {
        return app.getString(R.string.categories);
    }

    @Override
    public String addItemError() {
        return app.getString(R.string.add_item_error);
    }

    @Override
    public String cannotAddMoreItemsThen(int symbolsLimit) {
        return app.getString(R.string.cannot_add_item, symbolsLimit);
    }

    @Override
    public String searchByName() {
        return app.getString(R.string.search_by_name);
    }

    @Override
    public String searchIntoCategory(String name) {
        return app.getString(R.string.search_into_category, name);
    }

    @Override
    public String notPermissionedServerStringResponse() {
        return "Not permissioned for data. Failed request";
    }

    @Override
    public String quoteTitleLand(WorkspaceQuote quote, QuoteWatchResponse value, @Nullable final String error) {

        return app.getString(R.string.quote_title_land,
                QuoteWatchResponseFieldExtensionsKt.getSymbolDescription(value),
                TextUtils.isEmpty(QuoteWatchResponseFieldExtensionsKt.getActualSymbol(value)) ? quote.getDisplayName() : QuoteWatchResponseFieldExtensionsKt.getActualSymbol(value),
                checkQuoteFieldValue(QuoteWatchResponseFieldExtensionsKt.getLast(value), error));
    }

    @Override
    public String quoteDataLand(QuoteWatchResponse value, @Nullable final String error) {
        return app.getString(R.string.quote_data_land,
                checkQuoteFieldValue(QuoteWatchResponseFieldExtensionsKt.getChange(value), error),
                checkQuoteFieldValue(QuoteWatchResponseFieldExtensionsKt.getChangePercentage(value), error),
                checkQuoteFieldValue(QuoteWatchResponseFieldExtensionsKt.getOpen(value), error),
                checkQuoteFieldValue(QuoteWatchResponseFieldExtensionsKt.getHigh(value), error),
                checkQuoteFieldValue(QuoteWatchResponseFieldExtensionsKt.getLow(value), error),
                QuoteWatchResponseFieldExtensionsKt.getVolume(value) == null ? error : String.valueOf(QuoteWatchResponseFieldExtensionsKt.getVolume(value)));
    }

    @Override
    public String quoteDataPort(WorkspaceQuote quote, QuoteWatchResponse value, @Nullable final String error) {

        String expression = TextUtils.isEmpty(QuoteWatchResponseFieldExtensionsKt.getActualSymbol(value)) ? quote.getDisplayName() : QuoteWatchResponseFieldExtensionsKt.getActualSymbol(value);
        if (expression == null) {
            return "";
        }

        @StringRes int formatRes;
        if (expression.length() > MAX_QUOTE_NAME_LENGTH) {
            expression = expression.substring(0, MAX_QUOTE_NAME_LENGTH).concat("...");
            formatRes = R.string.quote_data_port_short;
        } else {
            formatRes = R.string.quote_data_port;
        }
        return app.getString(formatRes,
                expression,
                checkQuoteFieldValue(QuoteWatchResponseFieldExtensionsKt.getLast(value), error),
                checkQuoteFieldValue(QuoteWatchResponseFieldExtensionsKt.getChange(value), error),
                checkQuoteFieldValue(QuoteWatchResponseFieldExtensionsKt.getChangePercentage(value), error));
    }

    private String checkQuoteFieldValue(final String toBeChecked, @Nullable final String error) {
        return TextUtils.isEmpty(toBeChecked) ? error : toBeChecked;
    }

    public String invalidSymbolStringResponse() {
        return "Symbol not found. Failed request";
    }

    @Override
    public String getString(int stringId) {
        return app.getString(stringId);
    }

    @Override
    public int getColor(@ColorRes int colorRes) {
        return ContextCompat.getColor(app, colorRes);
    }

    @Override
    public String getString(int stringId, Object... args) {
        return app.getString(stringId, args);
    }
}

