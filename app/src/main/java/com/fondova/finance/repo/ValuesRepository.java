package com.fondova.finance.repo;

import com.fondova.finance.AppExecutors;
import com.fondova.finance.api.quote.QuoteService;
import com.fondova.finance.api.socket.ApiService;
import com.fondova.finance.workspace.WorkspaceQuote;
import com.fondova.finance.workspace.WorkspaceQuoteType;
import com.fondova.finance.persistance.AppStorage;
import com.fondova.finance.persistance.LocalQuoteListManager;
import com.fondova.finance.persistance.QuoteListConverter;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ValuesRepository {

    private static final String TAG = "ValuesRepository";

    @Inject ApiService apiService;
    @Inject
    LocalQuoteListManager quoteDao;
    @Inject TextsRepository textsRepository;
    @Inject AppExecutors appExecutors;
    @Inject
    AppStorage appStorage;
    @Inject
    QuoteService quoteService;

    @Inject
    public ValuesRepository() {
    }

    @Inject QuoteWatchRepository quoteWatchRepository;

    public void listenForIncomingQuoteValues() {

        List<WorkspaceQuote> quotes = quoteDao.getAllSymbols();

        appExecutors.networkIO().execute(() -> {

            for (WorkspaceQuote quote : quotes) {
                listenForIncomingQuoteValues(QuoteListConverter.Companion.convertWorkspaceQuoteToQuote(quote));
            }
        });
    }

    public void listenForIncomingQuoteValues(WorkspaceQuote quote) {
        String refreshRate = appStorage.getRefreshRate();
        if (refreshRate.equals(textsRepository.refreshRateOffString())) {
            return;
        }

        quoteService.watchQuote(quote.getValue(), quote.getType().toLowerCase().equals(WorkspaceQuoteType.Companion.getEXPRESSION()));
    }

    public void fetchLatestValuesForWatchedQuotes() {
        List<WorkspaceQuote> quotes = quoteDao.getAllSymbols();

        for (WorkspaceQuote quote : quotes) {
            quoteSnap(QuoteListConverter.Companion.convertWorkspaceQuoteToQuote(quote));
        }
    }

    public void quoteSnap(WorkspaceQuote quote) {
        quoteService.snapQuote(quote.getValue(), quote.getType().toLowerCase().equals(WorkspaceQuoteType.Companion.getEXPRESSION()));
    }

}