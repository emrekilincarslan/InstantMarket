package com.fondova.finance.ui.news;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.fondova.finance.ui.news.category.NewsCategoryFragment;
import com.fondova.finance.vo.NewsCategory;

import java.util.ArrayList;
import java.util.List;

class NewsCategoriesAdapter extends FragmentStatePagerAdapter {

    private List<NewsCategory> newsCategories = new ArrayList<>();

    NewsCategoriesAdapter(FragmentManager fm) {
        super(fm);
    }

    void setNewsCategories(@NonNull final List<NewsCategory> newsCategories) {
        List<NewsCategory> copy = new ArrayList<>();
        for (NewsCategory item : newsCategories) {
            copy.add(item);
        }
        this.newsCategories = copy;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        return NewsCategoryFragment.newInstance(newsCategories.get(position));
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return newsCategories.get(position).name;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return newsCategories.size();
    }
}
