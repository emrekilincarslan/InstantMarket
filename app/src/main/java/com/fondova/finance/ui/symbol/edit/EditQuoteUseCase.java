package com.fondova.finance.ui.symbol.edit;


import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.util.Log;

import com.fondova.finance.R;
import com.fondova.finance.api.session.SessionService;
import com.fondova.finance.api.session.SessionStatus;
import com.fondova.finance.workspace.Workspace;
import com.fondova.finance.persistance.AppStorage;
import com.fondova.finance.persistance.QuoteListConverter;
import com.fondova.finance.sync.QuoteSyncItem;
import com.fondova.finance.ui.BaseUseCase;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class EditQuoteUseCase extends BaseUseCase<EditQuoteView> {

    private final AppStorage appStorage;
    private List<QuoteSyncItem> quoteList;
    private String workspaceId;
    private SessionService sessionService;

    @Inject EditQuoteUseCase(AppStorage appStorage,
                             SessionService sessionService) {
        this.appStorage = appStorage;
        this.sessionService = sessionService;
        Workspace workspace = appStorage.getWorkspace();
        this.workspaceId = workspace.getWorkspaceId();
        this.quoteList = QuoteListConverter.Companion.convertWorkspaceIntoQuoteList(workspace);
    }

    // ---------------------------------------------------------------------------------------------
    // LifeCycle
    // ---------------------------------------------------------------------------------------------
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    void onCreate() {
        view.setupViews();

        loadQuotes();

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
        view.updateToolbarSubTitle("");

        showConnected(sessionService.getSessionStatusLiveData().getValue() == SessionStatus.connected);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    void onStop() {
        if (view == null) return;
        view.showLoading(false);
    }

    // ---------------------------------------------------------------------------------------------
    // Public
    // ---------------------------------------------------------------------------------------------
    void onDoneClicked() {

        view.finishActivity();
    }

    void onDeleteClick(QuoteSyncItem quote, int position) {
        if (position >= quoteList.size()) {
            Log.e("" + this.getClass(), String.format("Attempting to delete invalid index: %d", position));
            return;
        }
        quoteList.remove(position);
        saveQuoteList(quoteList);
        view.removeItemFromAdapter(position);
        view.showSnackbarAfterDeletion(quote, position);
    }

    void undoQuoteDelete(QuoteSyncItem quote, int position) {
        quoteList.add(position, quote);
        saveQuoteList(quoteList);
        view.addItemToAdapter(quote, position);
    }

    private void saveQuoteList(List<QuoteSyncItem> quoteList) {
        Workspace workspace = QuoteListConverter.Companion.convertQuoteSyncItemListToWorkspace(quoteList);
        workspace.setWorkspaceId(workspaceId);
        appStorage.updateAndSaveWorkspace(workspace);
    }


    // ---------------------------------------------------------------------------------------------
    // Private
    // ---------------------------------------------------------------------------------------------
    private void showConnected(boolean isConnected) {
        view.showConnectivityIcon(isConnected ? R.drawable.circle_green : R.drawable.circle_red);
    }

    private void loadQuotes() {
        view.showLoadingContent(true);
        if (view == null) {
            return;
        }
        view.showLoadingContent(false);
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
