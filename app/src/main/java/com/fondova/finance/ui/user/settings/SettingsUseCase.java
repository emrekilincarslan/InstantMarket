package com.fondova.finance.ui.user.settings;


import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Intent;

import com.fondova.finance.R;
import com.fondova.finance.api.session.NetworkConnectivityService;
import com.fondova.finance.api.session.SessionService;
import com.fondova.finance.api.session.SessionStatus;
import com.fondova.finance.api.socket.ApiService;
import com.fondova.finance.persistance.AppStorage;
import com.fondova.finance.repo.QuotesRepository;
import com.fondova.finance.repo.TextsRepository;
import com.fondova.finance.repo.ValuesRepository;
import com.fondova.finance.ui.BaseUseCase;
import com.fondova.finance.util.EmailSupportHelper;

import javax.inject.Inject;


public class SettingsUseCase extends BaseUseCase<SettingsView> {

    private final TextsRepository textsRepository;
    private final QuotesRepository quotesRepository;
    private final ValuesRepository valuesRepository;
    private final TextsRepository textRepository;
    private EmailSupportHelper emailSupportHelper;
    private final AppStorage appStorage;
    private final SessionService sessionService;
    private final NetworkConnectivityService networkConnectivityService;
    private final ApiService apiService;

    @Inject
    public SettingsUseCase(TextsRepository textsRepository,
                           QuotesRepository quotesRepository,
                           ValuesRepository valuesRepository,
                           TextsRepository textRepository,
                           EmailSupportHelper emailSupportHelper,
                           SessionService sessionService,
                           ApiService apiService,
                           NetworkConnectivityService networkConnectivityService,
                           AppStorage appStorage) {
        this.textsRepository = textsRepository;
        this.quotesRepository = quotesRepository;
        this.valuesRepository = valuesRepository;
        this.textRepository = textRepository;
        this.emailSupportHelper = emailSupportHelper;
        this.appStorage = appStorage;
        this.sessionService = sessionService;
        this.networkConnectivityService = networkConnectivityService;
        this.apiService = apiService;
    }

    @Override
    protected void onBaseCreate(LifecycleOwner source) {
        super.onBaseCreate(source);
        sessionService.getSessionStatusLiveData().observe(source, this::sessionStatusChanged);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void onResume() {
        if (appStorage.isUserLogoutForced) {
            view.forceLogout();
            return;
        }

        view.updateToolbarTitle(R.string.settings);
        view.setAboutVersion(textsRepository.appVersion());


        markCheckbox(appStorage.getRefreshRate(), true, true);
    }

    private void sessionStatusChanged(SessionStatus sessionStatus) {
        switch (sessionStatus) {
            case connected:
                showConnected(true);
                view.populateConnectString(R.string.disconnect);
                break;

            case connecting:
                view.populateConnectString(R.string.loading___);
                break;

            default:
                showConnected(false);
                view.populateConnectString(R.string.connect);
                break;
        }

    }

    private void showConnected(boolean isConnected) {
        view.showConnectivityIcon(isConnected ? R.drawable.circle_green : R.drawable.circle_red);
    }

    void clickedCheckbox(String checkboxLabel) {
        if (sessionService.getSessionStatusLiveData().getValue() != SessionStatus.connected) {
            view.showError(R.string.error, R.string.not_connected_to_server_message);
            return;
        }

        boolean wasManualRefresh = appStorage.isManualRefreshEnabled();

        appStorage.setRefreshRate(checkboxLabel);
        markCheckbox(checkboxLabel, true, false);

        if (checkboxLabel.equals(textsRepository.refreshRateOffString())) {
            quotesRepository.unwatchAllQuotes();
        } else {
            if (wasManualRefresh) {
                apiService.refreshRate(appStorage.getRefreshRateAsInt(), item -> valuesRepository.listenForIncomingQuoteValues());
            } else {
                apiService.refreshRate(appStorage.getRefreshRateAsInt(), item -> { });
            }
        }

    }

    void clickedHelp() {
        view.openUrl("https://help.instantmarket.stock.com/");
    }

    void clickedAbout() {
        view.goToAbout();
    }

    void clickedEmailSupport() {
        if (emailSupportHelper.canSendEmail()) {
            view.showLoading();
            Intent intent = emailSupportHelper.createIntent();
            view.openEmailIntent(intent);
            view.hideLoading();
        } else {
            view.showError(R.string.mail_client_error_title, R.string.mail_client_error_message);
        }
    }

    void clickedEula() {
        view.goToEula();
    }

    void clickedWelcome() {
        view.goToWelcome();
    }

    void clickedPrivacyPolicy() {
        view.goToPrivacyPolicy(R.color.toolbar_color, R.drawable.ic_arrow_back_white_24dp,
                textRepository.privacyHtmlUrl());
    }

    void clickedLogout() {
        sessionService.logout(true);
        view.goToLoginScreen();
    }

    void clickedDisconnect() {
        if (sessionService.getSessionStatusLiveData().getValue() == SessionStatus.connected) {
            sessionService.logout(true);
        } else {
            if (!networkConnectivityService.isNetworkAvailable()) {
                view.showError(R.string.network_error, R.string.no_network_message);
                return;
            }
            sessionService.reconnect(null);
        }

    }

    private void markCheckbox(String checkboxLabel, boolean checked, boolean skipAnimation) {
        view.uncheckAll();

        if (checkboxLabel.equals(textsRepository.refreshRate5SecondsString())) {
            view.mark5SecondsCheckbox(checked, skipAnimation);
        } else if (checkboxLabel.equals(textsRepository.refreshRate30SecondsString())) {
            view.mark30SecondsCheckbox(checked, skipAnimation);
        } else if (checkboxLabel.equals(textsRepository.refreshRate60SecondsString())) {
            view.mark60SecondsCheckbox(checked, skipAnimation);
        } else if (checkboxLabel.equals(textsRepository.refreshRate5MinutesString())) {
            view.mark5MinutesCheckbox(checked, skipAnimation);
        } else {
            view.markOffCheckbox(checked, skipAnimation);
        }
    }


}
