package com.fondova.finance.ui.symbol;


import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.fondova.finance.AppExecutors;
import com.fondova.finance.R;
import com.fondova.finance.api.quote.QuoteService;
import com.fondova.finance.api.session.SessionService;
import com.fondova.finance.api.session.SessionStatus;
import com.fondova.finance.workspace.Workspace;
import com.fondova.finance.workspace.WorkspaceQuote;
import com.fondova.finance.persistance.AppStorage;
import com.fondova.finance.persistance.QuoteListConverter;
import com.fondova.finance.repo.TextsRepository;
import com.fondova.finance.sync.QuoteSyncItem;
import com.fondova.finance.sync.SyncManager;
import com.fondova.finance.ui.BaseUseCase;
import com.fondova.finance.ui.util.DateFormatUtil;

import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class QuoteUseCase extends BaseUseCase<QuoteView> {

    // ---------------------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------------------
    private static final String TAG = "QuoteUseCase";
    private static final long MANUAL_REFRESH_WAIT = 5000; // 5 sec = 5 * 1000 ms

    private boolean isManualRefreshWaitDone;
    private final TextsRepository textsRepository;
    private final AppExecutors appExecutors;
    private SyncManager syncManager;
    private QuoteViewModel model;
    private AppStorage appStorage;
    private SessionService sessionService;
    private QuoteService quoteService;

    @Inject
    public QuoteUseCase(TextsRepository textsRepository,
                        AppExecutors appExecutors,
                        SyncManager syncManager,
                        SessionService sessionService,
                        QuoteService quoteService,
                        AppStorage appStorage) {
        this.textsRepository = textsRepository;
        this.appExecutors = appExecutors;
        this.syncManager = syncManager;
        this.appStorage = appStorage;
        this.sessionService = sessionService;
        this.quoteService = quoteService;
    }

    // ---------------------------------------------------------------------------------------------
    // LifeCycle
    // ---------------------------------------------------------------------------------------------
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    void setup() {
        view.setupViews();

        model = ViewModelProviders.of((Fragment) source).get(QuoteViewModel.class);

        isManualRefreshWaitDone = true;
    }

    @Override
    protected void onBaseCreate(LifecycleOwner source) {
        super.onBaseCreate(source);
        sessionService.getSessionStatusLiveData().observe(source, this::sessionStatusChanged);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void onResume() {
        if (appStorage.isUserLogoutForced) {
            view.forceLogout();
            return;
        }

        view.updateToolbarTitle(R.string.toolbar_title);
        view.updateToolbarSubTitle(
                textsRepository.autoRefreshTitle(appStorage.getRefreshRate()));

        view.updateLastUpdatedTime(DateFormatUtil.dateTimeToLastUpdatedString(new DateTime(quoteService.getLastQuoteUpdateLiveData().getValue())));
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    void onStop() {
        stopSpamRefreshTimer();
    }


    // ---------------------------------------------------------------------------------------------
    // Public
    // ---------------------------------------------------------------------------------------------
    void onAddSymbolClicked() {
        int order = view.getQuotes().size();

        view.openAddSymbolsScreen(order);
    }

    void onEditQuotesClicked() {
        view.goToEditQuotesScreen();
    }

    void onRowClicked(int groupIndex, int quoteIndex) {
        WorkspaceQuote workspaceQuote = getQuote(groupIndex, quoteIndex);

        if (checkForNetworkOperationAllowed()) {
            view.openChartScreen(workspaceQuote);
        }
    }

    void onManualRefreshClicked() {
        if (checkForNetworkOperationAllowed()) {
            appExecutors.networkIO().execute(this::onManualRefresh);
        }
    }

    void onDeleteRowClicked(int groupIndex, Integer quoteIndex) {
        WorkspaceQuote workspaceQuote = getQuote(groupIndex, quoteIndex);
        QuoteSyncItem quote = QuoteListConverter.Companion.convertWorkspaceQuoteToQuote(workspaceQuote);

        view.quoteDeletedSnackbar(groupIndex, quoteIndex, quote);

        model.deleteQuote(groupIndex, quoteIndex, quote);
        syncManager.uploadLocalData();
    }

    private WorkspaceQuote getQuote(int groupIndex, Integer quoteIndex) {
        Workspace workspace = appStorage.getWorkspace();
        WorkspaceQuote quote = workspace.getGroups().get(groupIndex).getListOfQuotes().get(quoteIndex);
        return quote;
    }


    private void showConnected(boolean isConnected) {
        view.showConnectivityIcon(isConnected ? R.drawable.circle_green : R.drawable.circle_red);
    }

    public boolean checkForNetworkOperationAllowed() {
        if (sessionService.getSessionStatusLiveData().getValue() != SessionStatus.connected) {
            view.showNotConnectedError(R.string.error, R.string.not_connected_to_server_message);
            return false;
        }
        return true;

    }

    private final Handler spamRefreshHandler = new Handler();
    private final Runnable spamRefreshCallback = () -> isManualRefreshWaitDone = true;

    private void stopSpamRefreshTimer() {
        isManualRefreshWaitDone = true;
        spamRefreshHandler.removeCallbacks(spamRefreshCallback);
    }

    private void resetSpamRefreshTimer() {
        isManualRefreshWaitDone = false;
        spamRefreshHandler.removeCallbacks(spamRefreshCallback);
        spamRefreshHandler.postDelayed(spamRefreshCallback, MANUAL_REFRESH_WAIT);
    }

    private void onManualRefresh() {
        if (isManualRefreshWaitDone) {
            Log.i(TAG, "onManualRefreshClicked: refreshing...");
            model.fetchLatestQuoteValues();
            resetSpamRefreshTimer();
        } else {
            Log.i(TAG, "onManualRefreshClicked: waiting...");
        }
    }

    private void sessionStatusChanged(SessionStatus sessionStatus) {
        switch (sessionStatus) {
            case connected:
                showConnected(true);
                break;
            case connecting:
                // do nothing
                break;
            default:
                showConnected(false);
                break;
        }
    }


}
