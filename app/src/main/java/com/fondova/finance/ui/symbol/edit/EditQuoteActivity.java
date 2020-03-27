package com.fondova.finance.ui.symbol.edit;

import android.app.Dialog;
import android.arch.lifecycle.LifecycleObserver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.fondova.finance.R;
import com.fondova.finance.di.components.AppComponent;
import com.fondova.finance.enums.QuoteType;
import com.fondova.finance.workspace.Workspace;
import com.fondova.finance.persistance.AppStorage;
import com.fondova.finance.persistance.QuoteListConverter;
import com.fondova.finance.sync.QuoteSyncItem;
import com.fondova.finance.ui.PxActivity;
import com.fondova.finance.ui.user.login.LoginActivity;
import com.fondova.finance.ui.util.DialogUtil;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;

public class EditQuoteActivity extends PxActivity implements EditQuoteView,
        OnAdapterActionListener {

    // ---------------------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------------------
    @Inject EditQuoteUseCase useCase;
    EditQuoteAdapter editQuoteListAdapter;
    EditWorkspaceGroupsAdapter editWorkspaceGroupsAdapter;
    @Inject
    AppStorage appStorage;
    @BindView(R.id.rv_list) RecyclerView rvList;
    @BindView(R.id.ll_root) LinearLayout llRoot;
    @BindView(R.id.cl_progress_bar) ContentLoadingProgressBar clProgressBar;

    private ItemTouchHelper itemTouchHelper;
    @Inject DialogUtil dialogUtil;
    private Dialog dialog;
    private Boolean editingGroups = false;


    // ---------------------------------------------------------------------------------------------
    // New instance
    // ---------------------------------------------------------------------------------------------
    public static void start(Context context) {
        Intent intent = new Intent(context, EditQuoteActivity.class);
        context.startActivity(intent);

    }

    private List<QuoteSyncItem> getQuoteListFromStorage() {
        Workspace workspace = appStorage.getWorkspace();
        return QuoteListConverter.Companion.convertWorkspaceIntoQuoteList(workspace);
    }

    // ---------------------------------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------------------------------


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editQuoteListAdapter = new EditQuoteAdapter();
        editQuoteListAdapter.setQuoteList(getQuoteListFromStorage());

        editWorkspaceGroupsAdapter = new EditWorkspaceGroupsAdapter(this);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_quotes, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.done:
                if (editingGroups) {
                    doneEditingGroups();
                } else {
                    commit();
                    useCase.onDoneClicked();
                }
                break;
            case R.id.edit_groups:
                editGroups();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void doneEditingGroups() {
        editingGroups = false;
        Workspace workspace = editWorkspaceGroupsAdapter.getWorkspace();
        List<QuoteSyncItem> quoteList = QuoteListConverter.Companion.convertWorkspaceIntoQuoteList(workspace);
        editQuoteListAdapter.setQuoteList(quoteList);
        rvList.setAdapter(editQuoteListAdapter);
        getToolbar().getMenu().getItem(0).setVisible(true);

        itemTouchHelper = new ItemTouchHelper(new DragItemTouchHelperCallback(editQuoteListAdapter));
        itemTouchHelper.attachToRecyclerView(rvList);
    }

    private void editGroups() {
        editingGroups = true;
        getToolbar().getMenu().getItem(0).setVisible(false);
        Workspace workspace = QuoteListConverter.Companion.convertQuoteSyncItemListToWorkspace(editQuoteListAdapter.getQuoteList());
        editWorkspaceGroupsAdapter.setWorkspace(workspace);
        rvList.setAdapter(editWorkspaceGroupsAdapter);

        itemTouchHelper = new ItemTouchHelper(new DragItemTouchHelperCallback(editWorkspaceGroupsAdapter));
        itemTouchHelper.attachToRecyclerView(rvList);
    }

    // ---------------------------------------------------------------------------------------------
    // EditQuote View
    // ---------------------------------------------------------------------------------------------
    @Override
    public void updateToolbarTitle(@StringRes int titleId) {
        getToolbar().setTitle(titleId);
    }

    @Override
    public void updateToolbarSubTitle(String subtitleText) {
        getToolbar().setSubtitle(subtitleText);
    }

    public void showConnectivityIcon(@DrawableRes int iconRes) {
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setIcon(iconRes);
    }

    @Override
    public void hideConnectivityIcon() {
        getActionBar().setIcon(null);
        getActionBar().setDisplayShowHomeEnabled(false);
    }

    @Override
    public void setupViews() {

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        DividerItemDecoration divider = new DividerItemDecoration(this,
                layoutManager.getOrientation());
        divider.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider));

        rvList.setHasFixedSize(true);
        rvList.setLayoutManager(layoutManager);
        rvList.setAdapter(editQuoteListAdapter);
        rvList.addItemDecoration(divider);
        editQuoteListAdapter.setOnAdapterActionListener(this);
        editWorkspaceGroupsAdapter.setOnAdapterActionListener(this);

        itemTouchHelper = new ItemTouchHelper(new DragItemTouchHelperCallback(editQuoteListAdapter));
        itemTouchHelper.attachToRecyclerView(rvList);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void finishActivity() {
        finish();
    }

    public void onDeleteClicked(QuoteSyncItem quote, int position) {
        useCase.onDeleteClick(quote, position);
    }

    @Override
    public void removeItemFromAdapter(int position) {
        editQuoteListAdapter.removeItem(position);
    }

    @Override
    public void addItemToAdapter(QuoteSyncItem quote, int position) {
        editQuoteListAdapter.addItem(quote, position);
    }

    @Override
    public void showSnackbarAfterDeletion(QuoteSyncItem quote, int position) {
        String type = getString(R.string.label);
        if (quote.type != QuoteType.LABEL) {
            type = getString(R.string.symbol);
        }
        String msg = getString(R.string.quote_has_been_deleted, type);

        Snackbar snackbar = Snackbar.make(llRoot, msg, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.undo, v -> useCase.undoQuoteDelete(quote, position));
        snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.blue));
        snackbar.show();
    }

    @Override
    public void showLoading(boolean show) {
        if (show) {
            dialog = dialogUtil.showLoading(this);
            dialog.setCancelable(false);
        } else if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public void showLoadingContent(boolean show) {
        if (show) {
            clProgressBar.show();
        } else {
            clProgressBar.hide();
        }
    }

    @Override
    public void forceLogout() {
        LoginActivity.startAfterForcedLogout(this);
        finish();
    }

    // ---------------------------------------------------------------------------------------------
    // Private methods
    // ---------------------------------------------------------------------------------------------
    private void commit() {

        Workspace workspace = QuoteListConverter.Companion.convertQuoteSyncItemListToWorkspace(editQuoteListAdapter.getQuoteList());

        Workspace storedWorkspace = appStorage.getWorkspace();
        storedWorkspace.setGroups(workspace.getGroups());

        appStorage.updateAndSaveWorkspace(storedWorkspace);
    }

    // ---------------------------------------------------------------------------------------------
    // Override PxFragment
    // ---------------------------------------------------------------------------------------------
    @Override
    protected int layoutId() {
        return R.layout.activity_edit_quotes;
    }

    @Override
    protected LifecycleObserver getLifecycleObserver() {
        return useCase;
    }

    @Override
    protected void doInject(AppComponent component) {
        component.inject(this);
    }
}
