package com.fondova.finance.ui.user.login;

import android.content.Intent;

import com.google.android.gms.common.ConnectionResult;
import com.fondova.finance.api.model.Credentials;
import com.fondova.finance.ui.BaseView;

interface LoginView extends BaseView {
    void enableLoginButton(boolean enable);

    void showApiError(String title, String message);

    void goToMainScreen();

    void goToEulaScreen();

    void showEmptyUsernameWarning();

    void showEmptyPasswordWarning();

    void hideUsernameError();

    void hidePasswordError();

    void showAppVersion(String appVersion);

    void populateCredentials(Credentials credentials);

    void checkRememberMe(boolean rememberCredentials);

    boolean isStartedAfterForcedLogout();

    void showForcedLogoutMessage();

    void showNoInternetError();

    void openEmailIntent(Intent intent);

    void showDownloadOrUploadDataToCloudDialog(boolean firstTimeLogin);

    void showUploadDataWarning();

    void showDriveApiError(ConnectionResult result);

    void startResolutionForResult(ConnectionResult result);

    void showStorageLocationQuestion();

    void showLoading();

    void hideLoading();
}
