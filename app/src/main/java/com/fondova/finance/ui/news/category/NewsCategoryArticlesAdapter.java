package com.fondova.finance.ui.news.category;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fondova.finance.R;
import com.fondova.finance.api.model.news.CategoryArticle;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewsCategoryArticlesAdapter extends RecyclerView.Adapter<NewsCategoryArticlesAdapter.ViewHolder> {


    // ---------------------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------------------
    private List<CategoryArticle> newsCategoryArticles = new ArrayList<>();
    private OnArticleClickListener onArticleClickListener;

    @Inject
    public NewsCategoryArticlesAdapter() {

    }

    // ---------------------------------------------------------------------------------------------
    // Override
    // ---------------------------------------------------------------------------------------------

    @Override
    public NewsCategoryArticlesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.adapter_news_article, parent, false));
    }

    @Override
    public void onBindViewHolder(NewsCategoryArticlesAdapter.ViewHolder holder, int position) {
        final CategoryArticle article = newsCategoryArticles.get(position);
        holder.tvTitle.setText(article.title);
        holder.tvDate.setText(article.datetime);
        holder.itemView.setOnClickListener(view -> {
            if (onArticleClickListener != null) {
                onArticleClickListener.onRowClick(article.storyId, article.title);
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsCategoryArticles.size();
    }


    // ---------------------------------------------------------------------------------------------
    // Public
    // ---------------------------------------------------------------------------------------------

    void setArticles(@Nullable final List<CategoryArticle> categoryArticles) {
        newsCategoryArticles = categoryArticles;
        notifyDataSetChanged();
    }

    List<CategoryArticle> getArticles() {
        return newsCategoryArticles;
    }

    void setOnQuoteClickListener(OnArticleClickListener onArticleClickListener) {
        this.onArticleClickListener = onArticleClickListener;
    }

    // ---------------------------------------------------------------------------------------------
    // Article ViewHolder
    // ---------------------------------------------------------------------------------------------
    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_title) TextView tvTitle;
        @BindView(R.id.tv_date) TextView tvDate;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    // ---------------------------------------------------------------------------------------------
    // On RowItem Click callback
    // ---------------------------------------------------------------------------------------------
    interface OnArticleClickListener {
        void onRowClick(@NonNull final String storyId, @NonNull final String title);
    }

}
