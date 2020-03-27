package com.fondova.finance.ui.symbol.edit;

import android.support.v7.widget.RecyclerView;

import com.fondova.finance.sync.QuoteSyncItem;

interface OnAdapterActionListener {

    /**
     * Called when a view is requesting a start of a drag.
     *
     * @param viewHolder The holder of the view to drag.
     */
    void onStartDrag(RecyclerView.ViewHolder viewHolder);
    void onDeleteClicked(QuoteSyncItem quote, int position);
}