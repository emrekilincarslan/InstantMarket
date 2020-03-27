package com.fondova.finance.repo;

import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;

import com.fondova.finance.api.model.quote.QuoteWatchResponse;
import com.fondova.finance.workspace.WorkspaceQuote;

public interface TextsRepository {
    String aboutHtmlFile();
    String privacyHtmlUrl();
    String eulaHtmlFile();
    String supportEmail();
    String supportSubject(String username);
    String supportBody(String uuid);
    String appNameForApiCalls();
    String appVersion();

    String refreshRate5SecondsString();
    String refreshRate30SecondsString();
    String refreshRate60SecondsString();
    String refreshRate5MinutesString();
    String refreshRateOffString();
    String autoRefreshTitle(String refreshRate);
    String symbolsCount(int count, int limit);
    String categories();
    String cannotAddMoreItemsThen(int symbolsLimit);
    String addItemError();
    String searchByName();
    String searchIntoCategory(String name);
    String notPermissionedServerStringResponse();
    String quoteTitleLand(WorkspaceQuote quote, QuoteWatchResponse value, @Nullable final String error);
    String quoteDataLand(QuoteWatchResponse value, @Nullable final String error);
    String quoteDataPort(WorkspaceQuote quote, QuoteWatchResponse value, @Nullable final String error);
    String invalidSymbolStringResponse();
    String getString(int stringId);
    String getString(int stringId, Object... args);
    int getColor(@ColorRes int colorRes);
}
