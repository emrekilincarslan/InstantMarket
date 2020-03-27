package com.fondova.finance.ui.news;

import android.arch.lifecycle.LifecycleObserver;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.fondova.finance.R;
import com.fondova.finance.di.components.AppComponent;
import com.fondova.finance.workspace.WorkspaceQuote;
import com.fondova.finance.repo.NewsRepository;
import com.fondova.finance.sync.QuoteSyncItem;
import com.fondova.finance.ui.PxFragment;
import com.fondova.finance.ui.news.edit.EditNewsActivity;
import com.fondova.finance.ui.util.DialogUtil;
import com.fondova.finance.vo.NewsCategory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;

public class NewsFragment extends PxFragment implements NewsView {

    public static final String TAG = NewsFragment.class.getSimpleName();
    public static final String EXTRA_QUOTE = "extra_quote";
    private static final String EXTRA_QUOTE_SHORT_DESCRIPTION = "quote_short_description";

    // ---------------------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------------------
    @BindView(R.id.vp_news) ViewPager vpNews;
    @BindView(R.id.tl_categories) TabLayout tlCategories;
    @BindView(R.id.tv_empty) TextView tvEmpty;
    private NewsCategoriesAdapter adapter;
    @Inject NewsUseCase useCase;
    @Inject DialogUtil dialogUtil;
    @Inject NewsRepository newsRepository;

    // ---------------------------------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------------------------------
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        new MenuInflater(getContext()).inflate(R.menu.menu_news, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                useCase.onAddClicked();
                return true;
            case R.id.edit:
                useCase.onEditClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // News View
    // ---------------------------------------------------------------------------------------------
    @Override
    public void setupViews(boolean showHomeAsUpEnabled) {
        setHasOptionsMenu(true);
        getActivity().getActionBar().setDisplayShowHomeEnabled(showHomeAsUpEnabled);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(showHomeAsUpEnabled);

        adapter = new NewsCategoriesAdapter(getChildFragmentManager());

        vpNews.setAdapter(adapter);
        tlCategories.setupWithViewPager(vpNews);
    }


    @Override
    public void setNewsCategoriesAndTheirPosition(@NonNull List<NewsCategory> newsCategories) {
        if (adapter == null) {
            return;
        }
        adapter.setNewsCategories(newsCategories);
    }

    @Override
    public void goToEditNewsScreen() {
        EditNewsActivity.start(getContext());
    }

    @Override
    public void showEmptyView(boolean show) {
        tvEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void addNewsCategory() {
        dialogUtil.addNewsCategoryDialog(getContext(),
                (name, keywords, selectedAND) -> useCase.onAddedCategory(name, keywords,
                        selectedAND));
    }

    @Override
    public boolean hasQuoteAndValue() {
        return getArguments() != null && getArguments().containsKey(EXTRA_QUOTE);
    }

    @Override
    public WorkspaceQuote getQuote() {
        String json = getArguments().getString(EXTRA_QUOTE);
        return QuoteSyncItem.fromJson(json);
    }

    @Override
    public String getQuoteShortDescription() {
        return getArguments().getString(EXTRA_QUOTE_SHORT_DESCRIPTION);
    }

    @Override
    public void showConnectionError(@StringRes int errorRes) {
        dialogUtil.showErrorDialog(getContext(), errorRes);
    }


    // ---------------------------------------------------------------------------------------------
    // Override PxFragment
    // ---------------------------------------------------------------------------------------------
    @Override
    protected int layoutId() {
        return R.layout.fragment_news;
    }

    @Override
    protected void doInject(AppComponent component) {
        component.inject(NewsFragment.this);
        newsRepository.getNewsCategoriesLiveData().observe(getActivity(), (newsCategoryList) -> {
            List<NewsCategory> list = newsCategoryList;
            if (list == null) {
                list = new ArrayList<>();
            }
            setNewsCategoriesAndTheirPosition(list);
        });
    }

    @Override
    protected LifecycleObserver getLifecycleObserver() {
        return useCase;
    }

    // ---------------------------------------------------------------------------------------------
    // New instance
    // ---------------------------------------------------------------------------------------------
    public static NewsFragment newInstance() {
        return new NewsFragment();
    }

    public static Fragment newInstance(WorkspaceQuote quote, String quoteShortDescription) {
        Bundle args = new Bundle();
        args.putString(EXTRA_QUOTE, QuoteSyncItem.fromWorkspaceQuote(quote).toJson());
        args.putString(EXTRA_QUOTE_SHORT_DESCRIPTION, quoteShortDescription);
        NewsFragment fragment = newInstance();
        fragment.setArguments(args);
        return fragment;
    }
}
