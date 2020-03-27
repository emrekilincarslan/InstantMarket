package com.fondova.finance.ui.news.edit;

import android.support.v7.widget.RecyclerView;

import com.fondova.finance.vo.NewsCategory;

interface OnAdapterActionListener {

    /**
     * Called when a view is requesting a start of a drag.
     *
     * @param viewHolder The holder of the view to drag.
     */
    void onStartDrag(RecyclerView.ViewHolder viewHolder);
    void onDeleteClicked(int position, NewsCategory category);
    void onEditClicked(int position, NewsCategory category);
}