package com.fondova.finance.ui.news.edit;

import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fondova.finance.R;
import com.fondova.finance.vo.NewsCategory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

class EditNewsAdapter extends RecyclerView.Adapter<EditNewsAdapter.QuoteViewHolder>
        implements DragItemTouchHelperAdapter {

    private static final String SYMBOL_NAME_FORMAT = "%s[%s]";

    private List<NewsCategory> categories = new ArrayList<>();
    private OnAdapterActionListener onAdapterActionListener;

    @Inject
    EditNewsAdapter() {
    }

    void setOnAdapterActionListener(OnAdapterActionListener listener) {
        onAdapterActionListener = listener;
    }

    public void setCategories(List<NewsCategory> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    List<NewsCategory> getCategories() {
        return categories;
    }

    @Override
    public QuoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new QuoteViewHolder(inflater.inflate(R.layout.adapter_edit_news, parent, false));
    }

    @Override
    public void onBindViewHolder(QuoteViewHolder h, int position) {
        NewsCategory item = categories.get(position);
        h.tvName.setText(item.name);


        if (onAdapterActionListener != null) {
            h.ivReorder.setOnTouchListener((v, event) -> {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    onAdapterActionListener.onStartDrag(h);
                }
                return false;
            });
        }

        if (onAdapterActionListener != null) {
            h.ivDelete.setOnClickListener(
                    v -> onAdapterActionListener.onDeleteClicked(categories.indexOf(item), item));
        }

        if (onAdapterActionListener != null) {
            h.ivEdit.setOnClickListener(
                    v -> onAdapterActionListener.onEditClicked(categories.indexOf(item), item));
        }
    }

    void hideItem(int position) {
        if (position == -1) return;

        categories.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    void addItem(NewsCategory category, int position) {
        categories.add(position, category);
        notifyItemInserted(position);
        notifyItemRangeChanged(position, getItemCount() + position);
    }

    void updateItem(NewsCategory category, int position) {
        categories.set(position, category);
        notifyItemChanged(position);
        notifyItemRangeChanged(position, getItemCount() - position);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(categories, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    static class QuoteViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.rl_row) RelativeLayout rlRow;
        @BindView(R.id.tv_name) TextView tvName;
        @BindView(R.id.iv_delete) ImageView ivDelete;
        @BindView(R.id.iv_reorder) ImageView ivReorder;
        @BindView(R.id.iv_edit) ImageView ivEdit;

        QuoteViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
