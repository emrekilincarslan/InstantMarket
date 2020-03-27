package com.fondova.finance.ui.symbol.add;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fondova.finance.R;
import com.fondova.finance.api.model.category.Category;
import com.fondova.finance.vo.Quote;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

class SearchSymbolsAdapter extends RecyclerView.Adapter<SearchSymbolsAdapter.SymbolViewHolder> {

    private final OnSymbolClickListener listener;
    private List<Category> categoryList;
    private List<Quote> symbolList;

    SearchSymbolsAdapter(OnSymbolClickListener listener) {
        this.listener = listener;
    }

    @Override
    public SearchSymbolsAdapter.SymbolViewHolder onCreateViewHolder(ViewGroup parent,
                                                                    int viewType) {
        return new SymbolViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_category, parent,
                        false));
    }

    @Override
    public void onBindViewHolder(SearchSymbolsAdapter.SymbolViewHolder h, int position) {
        if (shouldShowCategories()) {
            Category category = categoryList.get(position);
            h.tvCategory.setText(category.name);
            h.tvCategory.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_right_arrow,
                    0);
            h.tvCategory.setOnClickListener(view -> listener.onCategoryClicked(category));
        } else {
            Context context = h.tvCategory.getContext();
            Quote symbol = symbolList.get(position);

            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_add);
            drawable.mutate().setTint(ContextCompat.getColor(context, R.color.symbol_add_plus_icon));

            SpannableString spannableString;

            if (TextUtils.isEmpty(symbol.description)) {
                spannableString = new SpannableString(symbol.displayName);
            } else {
                spannableString = new SpannableString(symbol.displayName + "\n" + symbol.description);
            }

            spannableString.setSpan(new RelativeSizeSpan(0.7f), symbol.displayName.length(),
                    spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            h.tvCategory.setText(spannableString);
            h.tvCategory.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
            h.tvCategory.setOnClickListener(view -> listener.onSymbolClicked(symbol.requestName, symbol.displayName));
        }
    }

    @Override
    public int getItemCount() {
        if (shouldShowCategories()) {
            return categoryList == null ? 0 : categoryList.size();
        } else {
            return symbolList == null ? 0 : symbolList.size();
        }
    }

    void refreshCategoryData(List<Category> categories) {
        this.categoryList = categories;
        notifyDataSetChanged();
    }

    private boolean shouldShowCategories() {
        return categoryList != null;
    }

    @Deprecated
    void refreshData(List<Quote> symbols) {
        this.symbolList = symbols;
        this.categoryList = null;
        notifyDataSetChanged();
    }

    static class SymbolViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_category) TextView tvCategory;

        SymbolViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    interface OnSymbolClickListener {
        void onCategoryClicked(Category category);

        void onSymbolClicked(String value, String displayName);
    }
}
