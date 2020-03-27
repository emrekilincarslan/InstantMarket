package com.fondova.finance.ui.symbol;

import android.app.Dialog;
import android.arch.lifecycle.LifecycleObserver;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import com.fondova.finance.R;
import com.fondova.finance.api.quote.QuoteService;
import com.fondova.finance.workspace.service.WorkspaceService;
import com.fondova.finance.charts.ChartActivity;
import com.fondova.finance.config.AppConfig;
import com.fondova.finance.di.components.AppComponent;
import com.fondova.finance.enums.QuoteType;
import com.fondova.finance.workspace.Workspace;
import com.fondova.finance.workspace.WorkspaceGroup;
import com.fondova.finance.workspace.WorkspaceQuote;
import com.fondova.finance.workspace.WorkspaceQuoteType;
import com.fondova.finance.persistance.AppStorage;
import com.fondova.finance.quotes.QuoteListView;
import com.fondova.finance.quotes.QuoteListViewListener;
import com.fondova.finance.quotes.edit.AddExpressionDialog;
import com.fondova.finance.quotes.edit.AddGroupDialog;
import com.fondova.finance.quotes.edit.GroupSelectionDialog;
import com.fondova.finance.repo.QuoteWatchRepository;
import com.fondova.finance.repo.QuotesRepository;
import com.fondova.finance.repo.TextsRepository;
import com.fondova.finance.sync.QuoteSyncItem;
import com.fondova.finance.ui.OnScrollObserver;
import com.fondova.finance.ui.PxFragment;
import com.fondova.finance.ui.main.MainActivity;
import com.fondova.finance.ui.symbol.add.AddSymbolsActivity;
import com.fondova.finance.ui.symbol.add.AddSymbolsUseCase;
import com.fondova.finance.ui.symbol.edit.EditQuoteActivity;
import com.fondova.finance.ui.user.login.LoginActivity;
import com.fondova.finance.ui.util.DateFormatUtil;
import com.fondova.finance.ui.util.DialogUtil;
import com.fondova.finance.util.ToolbarFieldAccessor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;

import static com.fondova.finance.ui.symbol.add.AddSymbolsUseCase.ADDED_QUOTES_LIMIT;

public class QuoteFragment extends PxFragment implements QuoteView, QuoteListViewListener {

    public static final String TAG = QuoteFragment.class.getSimpleName();

    // ---------------------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------------------
    @BindView(R.id.rv_list)
    QuoteListView quoteListView;
    @BindView(R.id.fl_root) FrameLayout flRoot;
    @Inject QuoteUseCase useCase;
    @Inject DialogUtil dialogUtil;
    private Dialog errorDialog;
    @Inject
    AppStorage appStorage;
    @Inject
    QuoteWatchRepository quoteWatchRepository;
    @Inject
    WorkspaceService workspaceService;
    @Inject
    QuotesRepository quotesRepository;
    @Inject
    AppConfig appConfig;
    @Inject
    QuoteService quoteService;
    @Inject
    TextsRepository textsRepository;

    // ---------------------------------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------------------------------
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        new MenuInflater(getContext()).inflate(R.menu.menu_symbols, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_symbol:
                useCase.onAddSymbolClicked();
                return true;
            case R.id.add_label:
                showAddGroupDialog();
                return true;
            case R.id.add_expression:
                showAddExpressionDialog();
                return true;
            case R.id.edit_quotes:
                useCase.onEditQuotesClicked();
                return true;
            case R.id.refresh:
                useCase.onManualRefreshClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        errorDialog = null;
    }

    // ---------------------------------------------------------------------------------------------
    // Quote View
    // ---------------------------------------------------------------------------------------------
    @Override
    public void setupViews() {
        setHasOptionsMenu(true);
        getActivity().getActionBar().setDisplayShowHomeEnabled(true);
        appStorage.quoteListLiveData.observe(getActivity(), (workspace) -> {
            if (workspace != null) {
                quoteListView.setWorkspace(workspace);
            }
        });

        quoteService.getLastQuoteUpdateLiveData().observe(getActivity(), (lastUpdateDate) -> {
            updateLastUpdatedTime(DateFormatUtil.dateTimeToLastUpdatedString(new DateTime(lastUpdateDate)));
        });

    }

    @Override
    public void onPause() {
        quoteListView.saveExpandedState();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        quoteListView.updateExpandedState();
    }

    @Override
    public void updateToolbarTitle(@StringRes int titleId) {
        ((MainActivity) getActivity()).getToolbar().setTitle(titleId);
    }

    @Override
    public void updateToolbarSubTitle(String subtitleText) {
        ((MainActivity) getActivity()).getToolbar().setSubtitle(subtitleText);
    }

    @Override
    public void updateLastUpdatedTime(String lastUpdated) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) {
            return;
        }
        TextView subtitleTextView = ToolbarFieldAccessor.getSubtitleTextView(activity.getToolbar());
        if (subtitleTextView == null) {
            return;
        }
        subtitleTextView.setMaxLines(3);
        subtitleTextView.setSingleLine(false);
        Toolbar toolbar = activity.getToolbar();
        if (toolbar == null) {
            return;
        }
        String subtitle = (toolbar.getSubtitle() != null) ? toolbar.getSubtitle().toString() : "";
        String lastUpdatedString = getString(R.string.last_updated);
        if (subtitle.contains(lastUpdatedString)) {
            subtitle = subtitle.substring(0, subtitle.indexOf(lastUpdatedString) - 1) + "\n" + lastUpdatedString + " " + lastUpdated;
        } else {
            subtitle = subtitle + "\n" + lastUpdatedString + " " + lastUpdated;
        }
        toolbar.setSubtitle(subtitle);
    }

    @Override
    public void openAddSymbolsScreen(int highlightedQuotePosition) {
        AddSymbolsActivity.start(getContext(), highlightedQuotePosition);
    }

    public void showConnectivityIcon(@DrawableRes int iconRes) {
        getActivity().getActionBar().setDisplayShowHomeEnabled(true);
        getActivity().getActionBar().setIcon(iconRes);
    }

    @Override
    public void showReachedQuotesLimitMessage(String title, String message) {
        dialogUtil.showMessage(getContext(), title, message);
    }

    @Override
    public void goToEditQuotesScreen() {
        EditQuoteActivity.start(getContext());
    }

    @Override
    public void quoteDeletedSnackbar(int groupIndex, Integer quoteIndex, QuoteSyncItem quote) {
        String type = getString(R.string.label);
        if (quote.type != QuoteType.LABEL) {
            type = getString(R.string.symbol);
        }
        String message = getString(R.string.quote_has_been_deleted, type);

        Snackbar snackbar = Snackbar.make(flRoot, message, Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(ContextCompat.getColor(getContext(), R.color.blue));
        snackbar.show();
    }

    @Override
    public void showNotConnectedError(@StringRes int title, @StringRes int message) {
        if (errorDialog == null || !errorDialog.isShowing()) {
            errorDialog = dialogUtil.showMessage(getContext(), title, message);
        }
    }

    @Override
    public List<QuoteSyncItem> getQuotes() {
        return new ArrayList<>();
    }

    @Override
    public void openChartScreen(WorkspaceQuote quote) {
        ChartActivity.Companion.start(getContext(), quote);
    }

    @Override
    public void showMessage(@StringRes int msgId) {
        dialogUtil.showMessage(getContext(), msgId);
    }

    @Override
    public void forceLogout() {
        LoginActivity.startAfterForcedLogout(getContext());
        getActivity().finish();
    }

    // ---------------------------------------------------------------------------------------------
    // Override PxFragment
    // ---------------------------------------------------------------------------------------------

    @Override
    protected int layoutId() {
        return R.layout.fragment_quotes;
    }

    @Override
    protected void doInject(AppComponent component) {
        component.inject(this);
        Workspace workspace = appStorage.getWorkspace();
        quoteListView.listen(this, this, quoteWatchRepository);
        quoteListView.setWorkspace(workspace);
        quoteListView.setWorkspaceList(appStorage.workspaceList);
        quoteListView.setOnScrollListener(new OnScrollObserver() {
            @Override
            public void onScrollDown() {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.hideToolbar();
            }

            @Override
            public void onScrollUp() {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.showToolbar();
            }
        });
    }

    @Override
    protected LifecycleObserver getLifecycleObserver() {
        return useCase;
    }


    // ---------------------------------------------------------------------------------------------
    // QuoteListViewListener
    // ---------------------------------------------------------------------------------------------
    @Override
    public void onQuoteClick(int groupIndex, int quoteIndex) {
        useCase.onRowClicked(groupIndex, quoteIndex);
    }

    @Override
    public void onDeleteClicked(int groupIndex, @Nullable Integer quoteIndex) {
        useCase.onDeleteRowClicked(groupIndex, quoteIndex);
    }

    @Override
    public void onWorkspaceSelected(@NotNull String workspaceId) {
        workspaceService.fetchWorkspaceDetails(workspaceId, (workspace, error) -> {
            String oldWorkspaceId = appStorage.getWorkspace().getWorkspaceId();
            String newWorkspaceId = workspace.getWorkspaceId();
            appStorage.setWorkspace(workspace);
            quotesRepository.unwatchAllQuotes();
            quotesRepository.listenForIncomingQuoteValues();
            workspaceService.setDefaultWorkspace(oldWorkspaceId, newWorkspaceId, () -> {
                Log.i(TAG, String.format("Changed Default Workspace from %s to %s", oldWorkspaceId, newWorkspaceId));
            });
        });
    }

    private boolean canAddItem() {
        if (!useCase.checkForNetworkOperationAllowed()) {
            return false;
        }

        if (quotesRepository.getQuotesCount() >= AddSymbolsUseCase.ADDED_QUOTES_LIMIT) {
            showReachedQuotesLimitMessage(textsRepository.addItemError(),
                    textsRepository.cannotAddMoreItemsThen(ADDED_QUOTES_LIMIT));
            return false;
        }
        return true;
    }

    private void showAddExpressionDialog() {

        if (!canAddItem()) {
            return;
        }



        AddExpressionDialog.Companion.showDialog(getContext(),
                quotesRepository.getQuotesCount(),
                AddSymbolsUseCase.ADDED_QUOTES_LIMIT,
                appConfig.useStockSettings(), (expression, name) -> {
                    showSelectGroupDialog(expression, name, WorkspaceQuoteType.Companion.getEXPRESSION());
                });
    }

    private void showSelectGroupDialog(String value, String displayName, String type) {
        List<WorkspaceGroup> groups = appStorage.getWorkspace().getGroups();

        GroupSelectionDialog.Companion.showDialog(getContext(), displayName, groups, (index) -> {
            quotesRepository.saveQuote(value, displayName, type, index, 0);
            quoteService.watchQuote(value, true);
            Snackbar.make(flRoot, R.string.symbol_has_been_added, Snackbar.LENGTH_LONG).show();
        });
    }

    private void showAddGroupDialog() {

        if (!canAddItem()) {
            return;
        }

        AddGroupDialog.Companion.showDialog(getContext(),
                quotesRepository.getQuotesCount(),
                AddSymbolsUseCase.ADDED_QUOTES_LIMIT,
                (name) -> {
                    quotesRepository.saveGroup(name, 0);
                });

    }

    // ---------------------------------------------------------------------------------------------
    // New instance
    // ---------------------------------------------------------------------------------------------
    public static QuoteFragment newInstance() {
        return new QuoteFragment();
    }

    public QuoteFragment() {
    }
}
