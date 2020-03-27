package com.fondova.finance.ui.symbol.add;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ViewModelProviders;
import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.fondova.finance.AppExecutors;
import com.fondova.finance.R;
import com.fondova.finance.api.Resource;
import com.fondova.finance.api.model.category.CategoriesData;
import com.fondova.finance.api.model.category.CategoriesResponse;
import com.fondova.finance.api.model.category.Category;
import com.fondova.finance.api.model.symbol.SymbolSearchResponse;
import com.fondova.finance.api.restful.StockRetrofit;
import com.fondova.finance.api.session.SessionService;
import com.fondova.finance.api.session.SessionStatus;
import com.fondova.finance.persistance.AppStorage;
import com.fondova.finance.repo.SuggestionRepository;
import com.fondova.finance.repo.TextsRepository;
import com.fondova.finance.sync.SyncManager;
import com.fondova.finance.ui.BaseUseCase;
import com.fondova.finance.vo.Quote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AddSymbolsUseCase extends BaseUseCase<AddSymbolsView> {

    public static final int ADDED_QUOTES_LIMIT = 100;
    private static final int SHOW_SYMBOLS_LIMIT = 500;
    private static final String ASTERISK = "*";

    private final TextsRepository textsRepository;
    private String latestSearchQuery;
    private final SuggestionRepository suggestionRepository;
    private final AppExecutors appExecutors;
    private SyncManager syncManager;
    private final List<Category> categoriesHistory;
    private final SessionService sessionService;
    private final HashMap<String, CategoriesResponse> categoriesResponseHashMap;
    private AddSymbolsViewModel addSymbolsViewModel;
    private final AppStorage appStorage;

    @Inject
    AddSymbolsUseCase(TextsRepository textsRepository, SuggestionRepository suggestionRepository,
                      AppExecutors appExecutors, SyncManager syncManager, SessionService sessionService, AppStorage appStorage) {
        this.textsRepository = textsRepository;
        this.latestSearchQuery = textsRepository.categories();
        this.suggestionRepository = suggestionRepository;
        this.appExecutors = appExecutors;
        this.syncManager = syncManager;
        this.categoriesHistory = new ArrayList<>();
        this.categoriesResponseHashMap = new HashMap<>();
        this.sessionService = sessionService;
        this.appStorage = appStorage;
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    void onCreate() {
        addSymbolsViewModel = ViewModelProviders.of((FragmentActivity) source).get(
                AddSymbolsViewModel.class);

        view.setupViews();
        view.updateSearchHint(getSearchHint());
        appStorage.quoteListLiveData.observe(source, workspace -> updateAddedSymbolsCount());
        if (checkForNetworkOperationAllowed()) {
            fetchAllCategories();
        }
        sessionService.getSessionStatusLiveData().observe(source, sessionStatus -> {
            switch (sessionStatus) {
                case connected:
                    view.showLoading(false);
                    if (!isDeepDived()) {
                        fetchAllCategories();
                    }
                    break;
                case connecting:
                    view.showLoading(true);
                    break;
                default:
                    view.showLoading(false);
                    view.showNotConnectedError(R.string.error, R.string.not_connected_to_server_message);
                    break;
            }

        });
    }


    // -------------------------------------------------------------------------------------------------------------
    // Public
    // -------------------------------------------------------------------------------------------------------------
    void onSearchSymbolQueryEntered(String query) {
        if (TextUtils.isEmpty(query)) return;
        view.hideKeyboard();
        if (checkForNetworkOperationAllowed()) {
            searchSymbol(query);
        }
    }

    void onCategoryClicked(Category category) {
        if (checkForNetworkOperationAllowed()) {
            deepDive(category);
        }
    }

    void onCloseSearch() {
        if (isDeepDived()) {
            onCategoryClicked(categoriesHistory.get(categoriesHistory.size() - 1));
        } else {
            // we are on the top level navigation into Categories
            showCachedTopLevelCategories();
        }
    }

    void onUpPressed() {
        if (isDeepDived()) {
            // we are on the top level navigation into Categories
            categoriesHistory.clear();
            showCachedTopLevelCategories();
        } else {
            view.closeScreen();
        }
    }

    void onBackPressed() {
        if (!isDeepDived()) {
            view.closeScreen();
            return;
        }

        Category lastCategory = getLatestCategoryFromHistory();
        categoriesHistory.remove(lastCategory);
        latestSearchQuery = getCurrentCategoryName();

        CategoriesResponse response = categoriesResponseHashMap.get(
                StockRetrofit.constructPath(categoriesHistory));
        notifyForCategories(Resource.success(response));
    }


    void onSuggestionClicked(int position) {
        if (checkForNetworkOperationAllowed()) {
            searchSymbolBySuggestion(position);
        }
    }

    Cursor getSuggestions() {
        return suggestionRepository.getAllSuggestionsCursor();
    }


    // -------------------------------------------------------------------------------------------------------------
    // Private
    // -------------------------------------------------------------------------------------------------------------
    private boolean checkForNetworkOperationAllowed() {
        if (sessionService.getSessionStatusLiveData().getValue() != SessionStatus.connected) {
            view.showNotConnectedError(R.string.network_error, R.string.no_network_message);
            return false;
        }

        return true;

    }

    private void fetchAllCategories() {
        MutableLiveData<Resource<CategoriesResponse>> allCategories =
                addSymbolsViewModel.getAllCategories();
        allCategories.observe(source, categoriesResponseResource -> {
            switch (categoriesResponseResource.status) {
                case SUCCESS:
                    allCategories.removeObservers(source);
                    view.showLoading(false);
                    notifyForCategories(categoriesResponseResource);
                    break;

                case LOADING:
                    view.showLoading(true);
                    break;

                case ERROR:
                    allCategories.removeObservers(source);
                    view.showLoading(false);
                    view.showApiError(categoriesResponseResource.title,
                            categoriesResponseResource.message);
                    break;
            }
        });
    }

    private void showCachedTopLevelCategories() {
        latestSearchQuery = textsRepository.categories();
        view.updateSearchHint(getSearchHint());

        CategoriesResponse categoriesResponse = categoriesResponseHashMap.get(
                StockRetrofit.EMPTY_SUBCATEGORY_PATH);

        if (categoriesResponse != null) {
            view.showCategories(0, getCategoriesHistory(),
                    categoriesResponse.categoriesData.categoryList);
        }
    }

    private void searchSymbol(String query) {
        latestSearchQuery = query;
        saveSearchQuery();
        if (isDeepDived()) {
            searchSymbolsIntoRestfulApi(query);
        } else {
            searchSymbolsIntoWebSocket();
        }
    }

    void onCancelLoading() {
        if (isDeepDived()) {
            addSymbolsViewModel.stopRestfulRequest();
        }
    }

    private void searchSymbolsIntoWebSocket() {
        MutableLiveData<Resource<SymbolSearchResponse>> symbolsLiveData =
                addSymbolsViewModel.getSymbolsForQuery(latestSearchQuery + ASTERISK);
        symbolsLiveData.observe(source, symbolResource -> {

            switch (symbolResource.status) {
                case SUCCESS:
                    symbolsLiveData.removeObservers(source);
                    view.showLoading(false);
                    view.showSymbols(symbolResource.data.quotes, getCategoriesHistory());
                    break;

                case LOADING:
                    view.showLoading(true);
                    break;

                case ERROR:
                    symbolsLiveData.removeObservers(source);
                    view.showLoading(false);
                    view.showApiError(symbolResource.title, symbolResource.message);
                    break;
            }
        });
    }

    private void searchSymbolBySuggestion(int position) {
        String suggestion = suggestionRepository.getSuggestionAtPosition(position);
        view.updateSearchText(suggestion);
        onSearchSymbolQueryEntered(suggestion);
    }

    private void saveSearchQuery() {
        suggestionRepository.saveSearchQuery(latestSearchQuery);
        view.updateSuggestions();
    }

    private void deepDive(Category category) {
        if (!categoriesHistory.contains(category)) categoriesHistory.add(category);
        latestSearchQuery = category.name;

        String key = StockRetrofit.constructPath(categoriesHistory);
        if (categoriesResponseHashMap.containsKey(key)) {
            notifyForCategories(Resource.success(categoriesResponseHashMap.get(key)));
            return;
        }

        MutableLiveData<Resource<CategoriesResponse>> subCategories =
                addSymbolsViewModel.getSubCategories(categoriesHistory);
        subCategories.observe(source, categoriesResponseResource -> {
            switch (categoriesResponseResource.status) {
                case SUCCESS:
                    subCategories.removeObservers(source);
                    view.showLoading(false);
                    notifyForCategories(categoriesResponseResource);
                    break;

                case LOADING:
                    view.showLoading(true);
                    break;

                case ERROR:
                    subCategories.removeObservers(source);
                    view.showLoading(false);
                    view.showApiError(categoriesResponseResource.title,
                            categoriesResponseResource.message);
                    break;
            }
        });
    }

    private void searchSymbolsIntoRestfulApi(String query) {
        MutableLiveData<Resource<CategoriesResponse>> subCategories =
                addSymbolsViewModel.getSymbolsIntoSubCategory(categoriesHistory, query);
        subCategories.observe(source, categoriesResponseResource -> {
            switch (categoriesResponseResource.status) {
                case SUCCESS:
                    subCategories.removeObservers(source);
                    view.showLoading(false);

                    List<Quote> symbols = categoriesResponseResource.data.categoriesData.symbolList;
                    int symbolsResultCount = symbols.size();
                    if (symbolsResultCount > SHOW_SYMBOLS_LIMIT) {
                        view.showMessageInSnackbar(R.string.additional_results);
                        symbolsResultCount = SHOW_SYMBOLS_LIMIT;
                        view.showSymbols(symbols.subList(0, SHOW_SYMBOLS_LIMIT), getCategoriesHistory());
                    } else {
                        view.showSymbols(symbols, getCategoriesHistory());
                    }

                    break;

                case LOADING:
                    view.showLoading(true);
                    break;

                case ERROR:
                    subCategories.removeObservers(source);
                    view.showLoading(false);
                    view.showApiError(categoriesResponseResource.title,
                            categoriesResponseResource.message);
                    break;
            }
        });
    }

    private boolean isDeepDived() {
        return categoriesHistory != null && categoriesHistory.size() > 0;
    }


    private void notifyForCategories(Resource<CategoriesResponse> categoriesResponseResource) {
        CategoriesData categoriesData = categoriesResponseResource.data.categoriesData;

        List<Quote> currentSymbols = categoriesResponseResource.data.categoriesData.symbolList;
        int symbolsResultCount = currentSymbols.size();
        if (symbolsResultCount > SHOW_SYMBOLS_LIMIT) {
            view.showMessageInSnackbar(R.string.additional_results);
            symbolsResultCount = SHOW_SYMBOLS_LIMIT;
        }

        if (categoriesData.categoryList == null || categoriesData.categoryList.size() == 0) {
            // reached end subcategory
            view.showSymbols(categoriesData.symbolList, getCategoriesHistory());
        } else {
            view.updateSearchHint(getSearchHint());
            view.showCategories(symbolsResultCount, getCategoriesHistory(),
                    categoriesData.categoryList);
        }

        categoriesResponseHashMap.put(StockRetrofit.constructPath(categoriesHistory),
                categoriesResponseResource.data);
    }

    private String getCurrentCategoryName() {
        if (categoriesHistory != null && categoriesHistory.size() > 0) {
            return getLatestCategoryFromHistory().name;
        }
        return textsRepository.categories();
    }

    private Category getLatestCategoryFromHistory() {
        return categoriesHistory.get(categoriesHistory.size() - 1);
    }

    private void updateAddedSymbolsCount() {
        int count = addSymbolsViewModel.getQuotesCount();

        appExecutors.mainThread().execute(() -> {
            if (source == null) return;
            view.showAddedSymbolsCount(textsRepository.symbolsCount(count, ADDED_QUOTES_LIMIT));
        });
    }

    private String getSearchHint() {
        if (isDeepDived()) {
            return textsRepository.searchIntoCategory(
                    categoriesHistory.get(categoriesHistory.size() - 1).name);
        } else {
            return textsRepository.searchByName();
        }
    }

    private List<String> getCategoriesHistory() {
        List<String> history = new ArrayList<>();
        if (categoriesHistory.size() > 0) {
            for (Category category : categoriesHistory) {
                history.add(category.name);
            }
        } else {
            history.add(textsRepository.categories());
        }
        return history;
    }
}
