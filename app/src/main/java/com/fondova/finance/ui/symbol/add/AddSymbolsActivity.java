package com.fondova.finance.ui.symbol.add;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.arch.lifecycle.LifecycleObserver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fondova.finance.R;
import com.fondova.finance.api.model.category.Category;
import com.fondova.finance.api.quote.QuoteService;
import com.fondova.finance.api.session.SessionService;
import com.fondova.finance.api.session.SessionStatus;
import com.fondova.finance.di.components.AppComponent;
import com.fondova.finance.workspace.WorkspaceGroup;
import com.fondova.finance.workspace.WorkspaceQuoteType;
import com.fondova.finance.persistance.AppStorage;
import com.fondova.finance.quotes.edit.GroupSelectionDialog;
import com.fondova.finance.repo.QuotesRepository;
import com.fondova.finance.repo.SuggestionRepository;
import com.fondova.finance.repo.TextsRepository;
import com.fondova.finance.ui.PxActivity;
import com.fondova.finance.ui.util.DialogUtil;
import com.fondova.finance.vo.Quote;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;

import static com.fondova.finance.ui.symbol.add.AddSymbolsUseCase.ADDED_QUOTES_LIMIT;

public class AddSymbolsActivity extends PxActivity implements AddSymbolsView,
        SearchSymbolsAdapter.OnSymbolClickListener {


    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------

    public static final String HIGHLIGHT_POSITION = "highlight_position";

    // ---------------------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------------------
    @BindView(R.id.root) ViewGroup vgRoot;
    @BindView(R.id.hsc_history) HorizontalScrollView hsvHistory;
    @BindView(R.id.crumb_text) TextView tvCrumbText;
    @BindView(R.id.tv_added_symbols_count) TextView tvAddedSymbolsCount;
    @BindView(R.id.tv_result_count) TextView tvResultCount;
    @BindView(R.id.rv_symbols) RecyclerView rvSymbols;

    @Inject AddSymbolsUseCase addSymbolsUseCase;
    @Inject DialogUtil dialogUtil;
    @Inject
    AppStorage appStorage;
    @Inject
    QuotesRepository quotesRepository;
    @Inject
    QuoteService quoteService;
    @Inject
    SessionService sessionService;
    @Inject
    TextsRepository textsRepository;

    private ProgressDialog dialog;

    private SearchSymbolsAdapter adapter;
    private SearchView searchView;
    private String searchHint;
    private int highlightedPosition = 0;

    // ---------------------------------------------------------------------------------------------
    // LifeCycle
    // ---------------------------------------------------------------------------------------------


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        highlightedPosition = getIntent().getIntExtra(HIGHLIGHT_POSITION, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_symbols, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();

        setupSearchViewUI();
        handleSearchViewActions();
        provideSearchHistory();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            addSymbolsUseCase.onUpPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        addSymbolsUseCase.onBackPressed();
    }


    // ---------------------------------------------------------------------------------------------
    // AddSymbolView
    // ---------------------------------------------------------------------------------------------


    @Override
    public int getHighlightedPosition() {
        return highlightedPosition;
    }

    @Override
    public void setupViews() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        adapter = new SearchSymbolsAdapter(this);

        rvSymbols.setLayoutManager(new LinearLayoutManager(this));
        rvSymbols.setAdapter(adapter);
    }

    @Override
    public void updateSearchHint(String hint) {
        getActionBar().setTitle(null);
        searchHint = hint;
        if (searchView != null) searchView.setQueryHint(hint);
    }

    @Override
    public void updateSearchText(String query) {
        searchView.setQuery(query, false);
    }

    @Override
    public void showLoading(boolean show) {
        if (show) {
            if (dialog == null) {
                dialog = dialogUtil.showLoading(this);
                dialog.setCancelable(true);
                dialog.setOnCancelListener(dialog -> {
                    AddSymbolsActivity.this.dialog = null;
                    addSymbolsUseCase.onCancelLoading();
                });
            }
        } else {
            if (dialog != null) {
                dialog.dismiss();
            }
            dialog = null;
        }
    }

    @Override
    public void showCategories(int symbolsResultCount, List<String> categoriesHistory,
                               List<Category> categories) {
        adapter.refreshCategoryData(categories);
        tvResultCount.setText(getString(R.string.results_, symbolsResultCount));

        tvCrumbText.setText(createBreadCrumbHistoryString(categoriesHistory));

        hsvHistory.postDelayed(() -> hsvHistory.smoothScrollTo(tvCrumbText.getRight(), 0), 100);
    }

    @Override
    public void showSymbols(List<Quote> quotes, List<String> categoriesHistory) {
        adapter.refreshData(quotes);
        tvCrumbText.setText(createBreadCrumbHistoryString(categoriesHistory));
        hsvHistory.postDelayed(() -> hsvHistory.smoothScrollTo(tvCrumbText.getRight(), 0), 100);
        tvResultCount.setText(getString(R.string.results_, quotes.size()));
    }

    @Override
    public void showApiError(String title, String message) {
        dialogUtil.showErrorDialog(this, title, message);
    }

    @Override
    public void showAddedSymbolsCount(String countString) {
        tvAddedSymbolsCount.setText(countString);
    }

    @Override
    public void showReachedSymbolsLimitMessage(String title, String message) {
        dialogUtil.showMessage(this, title, message);
    }

    @Override
    public void showMessageInSnackbar(@StringRes int message) {
        Snackbar.make(vgRoot, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void closeScreen() {
        finish();
    }

    @Override
    public void updateSuggestions() {
        provideSearchHistory();
    }

    @Override
    public void showNotConnectedError(@StringRes int title, @StringRes int message) {
        dialogUtil.showMessage(this, title, message);
    }

    @Override
    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // SearchSymbolsAdapter.OnSymbolClickListener
    // ---------------------------------------------------------------------------------------------
    @Override
    public void onSymbolClicked(String value, String displayName) {
        if (!canAddItem()) {
            return;
        }
        showSelectGroupDialog(value, displayName);
    }

    private boolean canAddItem() {
        if (!checkForNetworkOperationAllowed()) {
            return false;
        }

        if (quotesRepository.getQuotesCount() >= AddSymbolsUseCase.ADDED_QUOTES_LIMIT) {
            showReachedQuotesLimitMessage(textsRepository.addItemError(),
                    textsRepository.cannotAddMoreItemsThen(ADDED_QUOTES_LIMIT));
            return false;
        }
        return true;
    }

    public void showReachedQuotesLimitMessage(String title, String message) {
        dialogUtil.showMessage(this, title, message);
    }


    public boolean checkForNetworkOperationAllowed() {
        if (sessionService.getSessionStatusLiveData().getValue() != SessionStatus.connected) {
            showNotConnectedError(R.string.error, R.string.not_connected_to_server_message);
            return false;
        }
        return true;

    }


    private void showSelectGroupDialog(String value, String displayName) {
        List<WorkspaceGroup> groups = appStorage.getWorkspace().getGroups();

        GroupSelectionDialog.Companion.showDialog(this, displayName, groups, (index) -> {
            quotesRepository.saveQuote(value, displayName, WorkspaceQuoteType.Companion.getSYMBOL(), index, 0);
            quoteService.watchQuote(value, false);
            showMessageInSnackbar(R.string.symbol_has_been_added);
        });
    }

    @Override
    public void onCategoryClicked(Category category) {
        addSymbolsUseCase.onCategoryClicked(category);
    }


    // ---------------------------------------------------------------------------------------------
    // Private
    // ---------------------------------------------------------------------------------------------

    private Pair<Integer, Integer> getSelectedIndexPath() {
        return new Pair<>(0, 0);
    }

    private void setupSearchViewUI() {
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconified(false);
        searchView.setQueryHint(searchHint);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        LinearLayout editFrame = searchView.findViewById(
                android.support.v7.appcompat.R.id.search_edit_frame);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) editFrame.getLayoutParams();
        params.leftMargin = 0;
        editFrame.setLayoutParams(params);
    }

    private void handleSearchViewActions() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchForSymbol(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return true;
            }
        });

        searchView.setOnCloseListener(() -> {
            addSymbolsUseCase.onCloseSearch();
            return true;
        });


        EditText editText = searchView.findViewById(
                android.support.v7.appcompat.R.id.search_src_text);
        editText.setOnEditorActionListener((v, keyAction, keyEvent) -> {
            if (
                //Soft keyboard search
                    keyAction == EditorInfo.IME_ACTION_SEARCH ||
                            //Physical keyboard enter key
                            (keyEvent != null && KeyEvent.KEYCODE_ENTER == keyEvent.getKeyCode()
                                    && keyEvent.getAction() == KeyEvent.ACTION_DOWN)) {
                searchForSymbol(editText.getText().toString());
                return true;
            }
            return true;
        });

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                addSymbolsUseCase.onSuggestionClicked(position);
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                addSymbolsUseCase.onSuggestionClicked(position);
                return true;
            }
        });
    }

    private void provideSearchHistory() {
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.item_suggestion,
                addSymbolsUseCase.getSuggestions(),
                new String[]{SuggestionRepository.COLUMN_QUERY},
                new int[]{R.id.tv_suggestion},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        searchView.setSuggestionsAdapter(adapter);
    }

    private void searchForSymbol(String query) {
        addSymbolsUseCase.onSearchSymbolQueryEntered(query);
    }

    private String createBreadCrumbHistoryString(List<String> categoriesHistory) {
        if (categoriesHistory.size() > 1) return TextUtils.join(" > ", categoriesHistory);
        return categoriesHistory.get(0);
    }


    // ---------------------------------------------------------------------------------------------
    // Override PxActivity
    // ---------------------------------------------------------------------------------------------
    @Override
    protected int layoutId() {
        return R.layout.activity_add_symbols;
    }

    @Override
    protected LifecycleObserver getLifecycleObserver() {
        return addSymbolsUseCase;
    }

    @Override
    protected void doInject(AppComponent component) {
        component.inject(this);
    }


    // ---------------------------------------------------------------------------------------------
    // New
    // ---------------------------------------------------------------------------------------------
    public static void start(Context context, int highlightedQuotePosition) {
        Intent intent = new Intent(context, AddSymbolsActivity.class);
        intent.putExtra(HIGHLIGHT_POSITION, highlightedQuotePosition);
        context.startActivity(intent);
    }
}
