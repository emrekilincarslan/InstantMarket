package com.fondova.finance.ui.news.edit;


import android.arch.lifecycle.LifecycleObserver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.fondova.finance.R;
import com.fondova.finance.di.components.AppComponent;
import com.fondova.finance.ui.PxActivity;
import com.fondova.finance.vo.NewsCategory;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;

public class EditNewsActivity extends PxActivity implements EditNewsView, OnAdapterActionListener {

    // ---------------------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------------------
    @BindView(R.id.cl_loading) ContentLoadingProgressBar clLoading;
    @BindView(R.id.rv_list) RecyclerView rvList;
    @BindView(R.id.rl_root) RelativeLayout rlRoot;

    @Inject EditNewsUseCase useCase;
    @Inject EditNewsAdapter adapter;

    private ItemTouchHelper itemTouchHelper;

    // ---------------------------------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_news, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.done:
                useCase.onDoneClicked();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // ---------------------------------------------------------------------------------------------
    // View
    // ---------------------------------------------------------------------------------------------
    @Override
    public void setupViews() {
        getActionBar().setDisplayHomeAsUpEnabled(true);

        rvList.setHasFixedSize(true);
        rvList.setLayoutManager(new LinearLayoutManager(this));
        rvList.setAdapter(adapter);

        itemTouchHelper = new ItemTouchHelper(new DragItemTouchHelperCallback(adapter));
        itemTouchHelper.attachToRecyclerView(rvList);

        adapter.setOnAdapterActionListener(this);
    }

    @Override
    public void updateToolbarTitle(@StringRes int titleId) {
        toolbar.setTitle(titleId);
    }

    @Override
    public void showLoading(boolean show) {
        if (show) {
            clLoading.show();
        } else {
            clLoading.hide();
        }
    }

    @Override
    public void setCategories(List<NewsCategory> categories) {
        adapter.setCategories(categories);
    }

    @Override
    public List<NewsCategory> getCategoriesFromAdapter() {
        return adapter.getCategories();
    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void hideItem(int position) {
        adapter.hideItem(position);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onDeleteClicked(int position, NewsCategory category) {
        useCase.onDeleteClick(position, category);
    }

    @Override
    public void onEditClicked(int position, NewsCategory category) {
        useCase.onEditClicked(this, position, category);
    }

    @Override
    public void showDeletedCategorySnackbar(@StringRes int message, NewsCategory newsCategory,
            int position) {
        Snackbar snackbar = Snackbar.make(rlRoot, message, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.undo, v -> useCase.undoDeleteCategory(newsCategory, position));
        snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.blue));
        snackbar.show();
    }

    @Override
    public void addItem(NewsCategory category, int position) {
        adapter.addItem(category, position);
    }

    @Override
    public void updateItem(NewsCategory category, int position) {
        adapter.updateItem(category, position);
    }

    // ---------------------------------------------------------------------------------------------
    // Override
    // ---------------------------------------------------------------------------------------------
    @Override
    protected int layoutId() {
        return R.layout.activity_edit_news;
    }

    @Override
    protected LifecycleObserver getLifecycleObserver() {
        return useCase;
    }

    @Override
    protected void doInject(AppComponent component) {
        component.inject(this);
    }

    // ---------------------------------------------------------------------------------------------
    // New
    // ---------------------------------------------------------------------------------------------
    public static void start(Context context) {
        Intent intent = new Intent(context, EditNewsActivity.class);
        context.startActivity(intent);
    }
}
