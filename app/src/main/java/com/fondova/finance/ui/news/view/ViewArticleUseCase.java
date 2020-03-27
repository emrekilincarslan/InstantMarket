package com.fondova.finance.ui.news.view;


import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ViewModelProviders;
import android.support.v4.app.FragmentActivity;

import com.fondova.finance.AppExecutors;
import com.fondova.finance.R;
import com.fondova.finance.api.OnLoadedListener;
import com.fondova.finance.ui.BaseUseCase;
import com.fondova.finance.util.NewsCategoryQueryBuilder;
import com.fondova.finance.vo.NewsCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class ViewArticleUseCase extends BaseUseCase<ViewArticleView> {
    private AppExecutors appExecutors;
    private NewsCategoryQueryBuilder newsCategoryQueryBuilder;
    private ViewArticleViewModel model;

    // ---------------------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------------------

    @Inject
    ViewArticleUseCase(AppExecutors appExecutors,
            NewsCategoryQueryBuilder newsCategoryQueryBuilder) {
        this.appExecutors = appExecutors;
        this.newsCategoryQueryBuilder = newsCategoryQueryBuilder;
    }

    // ---------------------------------------------------------------------------------------------
    // LifeCycle
    // ---------------------------------------------------------------------------------------------
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    void setup() {
        model = ViewModelProviders.of((FragmentActivity) source).get(ViewArticleViewModel.class);

        view.setupViews(view.getArticleTitle());

        loadArticle();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void onResume() {
        view.updateToolbarTitle(R.string.recent_news);
    }

    // ---------------------------------------------------------------------------------------------
    // Private
    // ---------------------------------------------------------------------------------------------
    private void loadArticle() {
        view.showLoading(true);
        String storyId = view.getStoryId();

        appExecutors.networkIO().execute(() -> {
            model.getArticle(storyId, new OnLoadedListener() {
                @Override
                public void onDone(final String response) {
                    appExecutors.mainThread().execute(() -> {
                        String finalResponse = markKeywords(response);

                        String html =
                                "<style>html, body {margin: 0;padding: 0;border: 0;font-size: "
                                        + "100%;font: inherit;vertical-align: baseline;"
                                        + "}</style><body>" + finalResponse + "</body>";
                        view.loadHtml(html);
                        view.showLoading(false);
                    });
                }

                @Override
                public void onError(String error) {
                    if (view != null) {
                        appExecutors.mainThread().execute(() -> view.showLoading(false));
                    }
                }
            });
        });
    }

    private String markKeywords(String response) {
        if (view.hasCategory()) {
            NewsCategory category = view.getCategory();

            if (category.query.equals("")) return response;

            List<String> keywords = newsCategoryQueryBuilder.decompileQuery(category.query);
            List<String> matches = new ArrayList<>();

            for (String keyword : keywords) {
                Pattern p = Pattern.compile("(?i)" + Pattern.quote(keyword));
                Matcher matcher = p.matcher(response);
                while (matcher.find()) {
                    matches.add(matcher.group());
                }
            }

            for (String match : matches) {
                response = response.replace(match, "<mark>" + match + "</mark>");
            }
        }
        return response;
    }
}
