package com.fondova.finance.ui.about;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;

import com.fondova.finance.R;
import com.fondova.finance.config.AppConfig;
import com.fondova.finance.persistance.AppStorage;
import com.fondova.finance.repo.TextsRepository;

import javax.inject.Inject;

import static com.fondova.finance.ui.about.HtmlActivity.ABOUT;
import static com.fondova.finance.ui.about.HtmlActivity.EULA;
import static com.fondova.finance.ui.about.HtmlActivity.PRIVACY;

public class HtmlUseCase implements LifecycleObserver {

    private final TextsRepository repository;
    private final AppStorage appStorage;
    private HtmlView view;
    private final AppConfig appConfig;

    @Inject
    public HtmlUseCase(TextsRepository repository, AppStorage appStorage, AppConfig appConfig) {
        this.repository = repository;
        this.appStorage = appStorage;
        this.appConfig = appConfig;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    void onCreate(LifecycleOwner source) {
        this.view = (HtmlView) source;

        loadHtmlFile();

        switch (view.getFileType()) {
            case PRIVACY:
                view.showButton(false);
                view.addBackArrow();
                break;
            case EULA:
                view.addBackArrow();
                view.showButton(view.shouldShowAcceptEulaBtn());
                view.setButtonText(R.string.accept);
                break;
            case ABOUT:
                view.showButton(false);
                view.addBackArrow();
                break;
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void onResume() {
        loadTitle();
    }

    // ---------------------------------------------------------------------------------------------
    // Public
    // ---------------------------------------------------------------------------------------------
    void onBtnGenericClick() {
        if (view.getFileType() == EULA && view.shouldShowAcceptEulaBtn()) {
            appStorage.setAcceptedEula(true);
        }

    }

    // ---------------------------------------------------------------------------------------------
    // Private
    // ---------------------------------------------------------------------------------------------
    private void loadTitle() {
        int title = R.string.about_stock_financex;
        switch (view.getFileType()) {
            case PRIVACY:
                title = R.string.privacy_policy;
                break;
            case EULA:
                title = R.string.eula;
                break;
        }

        view.updateToolbarTitle(title);
    }

    private void loadHtmlFile() {
        String htmlFile = repository.aboutHtmlFile();
        switch (view.getFileType()) {
            case PRIVACY:
                htmlFile = repository.privacyHtmlUrl();
                view.loadUrl(htmlFile);
                return;
            case EULA:
                htmlFile = repository.eulaHtmlFile();
                break;
        }

        view.setHtmlFile(htmlFile);
    }
}
