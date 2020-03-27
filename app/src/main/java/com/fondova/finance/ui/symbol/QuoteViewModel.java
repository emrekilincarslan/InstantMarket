package com.fondova.finance.ui.symbol;


import android.arch.lifecycle.ViewModel;

import com.fondova.finance.App;
import com.fondova.finance.persistance.AppStorage;
import com.fondova.finance.repo.QuotesRepository;
import com.fondova.finance.repo.TextsRepository;
import com.fondova.finance.repo.ValuesRepository;
import com.fondova.finance.sync.QuoteSyncItem;

import java.util.List;

import javax.inject.Inject;

public class QuoteViewModel extends ViewModel {

    // ---------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------
    @Inject QuotesRepository quotesRepository;
    @Inject ValuesRepository valuesRepository;
    @Inject TextsRepository textsRepository;
    @Inject
    AppStorage appStorage;

    private List<QuoteSyncItem> quoteValues;


    // ---------------------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------------------
    public QuoteViewModel() {
        App.getAppComponent().inject(this);
    }


    // ---------------------------------------------------------------------------------
    // Public
    // ---------------------------------------------------------------------------------
    @Deprecated
    List<QuoteSyncItem> getQuotes() {
        if (quoteValues == null || quoteValues.isEmpty()) {
            loadQuoteValues();
        }
        return quoteValues;
    }

    void deleteQuote(int groupIndex, Integer quoteIndex, QuoteSyncItem quote) {
        quotesRepository.deleteQuote(groupIndex, quoteIndex, quote);
    }

    void fetchLatestQuoteValues() {
        valuesRepository.fetchLatestValuesForWatchedQuotes();
    }


    // ---------------------------------------------------------------------------------
    // Private
    // ---------------------------------------------------------------------------------
    private void loadQuoteValues() {
        quoteValues = quotesRepository.getAllQuotes();
    }

}