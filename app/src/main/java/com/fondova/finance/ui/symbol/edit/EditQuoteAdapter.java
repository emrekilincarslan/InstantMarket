package com.fondova.finance.ui.symbol.edit;

import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fondova.finance.R;
import com.fondova.finance.enums.QuoteType;
import com.fondova.finance.sync.QuoteSyncItem;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditQuoteAdapter extends RecyclerView.Adapter<EditQuoteAdapter.QuoteViewHolder>
        implements DragItemTouchHelperAdapter {

    private OnAdapterActionListener onAdapterActionListener;
    private List<QuoteSyncItem> quoteList;

    EditQuoteAdapter() {

    }

    public void setQuoteList(List<QuoteSyncItem> newQuoteList) {
        this.quoteList = newQuoteList;
        notifyDataSetChanged();
    }

    public List<QuoteSyncItem> getQuoteList() {
        return this.quoteList;
    }

    void setOnAdapterActionListener(OnAdapterActionListener listener) {
        onAdapterActionListener = listener;
    }

    @Override
    public QuoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new QuoteViewHolder(inflater.inflate(R.layout.adapter_edit_quote, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return quoteList.get(position).type;
    }

    @Override
    public void onBindViewHolder(QuoteViewHolder h, int position) {
        QuoteSyncItem quote = quoteList.get(position);

        int type = getItemViewType(position);
        if (type == QuoteType.LABEL) {
            h.clRow.setBackgroundResource(R.color.grey_3f);
            h.ivReorder.setVisibility(View.GONE);
            h.ivDelete.setVisibility(View.GONE);
        } else {
            h.clRow.setBackgroundResource(R.color.black);

        }

        String name = quote.displayName;

        h.tvSymbol.setText(name);

        if (onAdapterActionListener != null) {
            h.ivReorder.setOnTouchListener((v, event) -> {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    onAdapterActionListener.onStartDrag(h);
                }
                return false;
            });
        }

        if (onAdapterActionListener != null) {
            h.ivDelete.setOnClickListener(v -> {
                if ((position == 0) && (quoteList.size() < 1 || quoteList.get(1).type != QuoteType.LABEL )) {
                    Snackbar snackbar = Snackbar.make(h.clRow, "Cannot delete the top group unless it is empty", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    return;
                }
                removeItem(position);
            });
        }
    }

    void removeItem(int position) {
        if (position == -1) return;
        quoteList.remove(position);

        notifyDataSetChanged();
    }

    void addItem(QuoteSyncItem quote, int position) {
        quoteList.add(position, quote);

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return quoteList.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(quoteList, fromPosition, toPosition);

        notifyItemMoved(fromPosition, toPosition);
    }

    private boolean isEmptyQuoteValues(int position) {
        return quoteList.get(position) == null;
    }



    public static class QuoteViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.cl_row) ConstraintLayout clRow;
        @BindView(R.id.tv_symbol) TextView tvSymbol;
        @BindView(R.id.tv_symbol_name) TextView tvSymbolName;
        @BindView(R.id.iv_delete) ImageView ivDelete;
        @BindView(R.id.iv_reorder) ImageView ivReorder;

        QuoteViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
