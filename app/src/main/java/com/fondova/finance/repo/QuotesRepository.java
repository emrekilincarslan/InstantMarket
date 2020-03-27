package com.fondova.finance.repo;

import com.fondova.finance.api.quote.QuoteService;
import com.fondova.finance.api.socket.ApiService;
import com.fondova.finance.workspace.Workspace;
import com.fondova.finance.workspace.WorkspaceQuote;
import com.fondova.finance.persistance.LocalQuoteListManager;
import com.fondova.finance.persistance.QuoteListConverter;
import com.fondova.finance.sync.QuoteSyncItem;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class QuotesRepository {


    // ---------------------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------------------
    private final ApiService apiService;
    private final LocalQuoteListManager quoteDao;
    private final ValuesRepository valuesRepository;
    private final QuoteService quoteService;

    @Inject
    public QuotesRepository(ApiService apiService,
                            LocalQuoteListManager quoteDao,
                            ValuesRepository valuesRepository,
                            QuoteService quoteService) {
        this.apiService = apiService;
        this.quoteDao = quoteDao;
        this.valuesRepository = valuesRepository;
        this.quoteService = quoteService;
    }

    // ---------------------------------------------------------------------------------------------
    // Public
    // ---------------------------------------------------------------------------------------------
    public void saveQuote(String value, String displayName, String type, int groupIndex, Integer quoteIndex) {
        quoteDao.insertQuote(value, displayName, type, groupIndex, quoteIndex);
    }
    public void saveGroup(String title, int index) {
        quoteDao.insertGroup(title, index);
    }

    public void setWorkspace(Workspace workspace) {
        quoteDao.setWorkspace(workspace);
    }

    public void deleteQuote(int groupIndex, int quoteIndex, WorkspaceQuote quote) {
        unwatchQuote(quote.getValue());
        quoteDao.deleteSymbol(groupIndex, quoteIndex);
    }

    public void deleteGroup(int groupIndex) {
        quoteDao.deleteGroup(groupIndex);
    }

    public void deleteAllQuotes() {
        for (QuoteSyncItem quote : getAllQuotes()) {
            unwatchQuote(quote.getValue());
        }
        quoteDao.deleteAllQuotes();
    }

    public void unwatchAllQuotes() {
        quoteService.unwatchAll();
    }

    public void listenForIncomingQuoteValues() {
        valuesRepository.listenForIncomingQuoteValues();
    }

    @Deprecated
    public List<QuoteSyncItem> getAllQuotes() {
        Workspace workspace = quoteDao.getWorkspace();
        return QuoteListConverter.Companion.convertWorkspaceIntoQuoteList(workspace);
    }

    public int getQuotesCount() {
        return quoteDao.getQuoteCount();
    }

    // ---------------------------------------------------------------------------------------------
    // Private
    // ---------------------------------------------------------------------------------------------

    private void unwatchQuote(String quote) {
        quoteService.unwatchQuote(quote);
    }

}
