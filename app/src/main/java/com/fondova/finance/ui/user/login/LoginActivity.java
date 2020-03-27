package com.fondova.finance.ui.user.login;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.arch.lifecycle.LifecycleActivity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.fondova.finance.App;
import com.fondova.finance.R;
import com.fondova.finance.api.Resource;
import com.fondova.finance.api.Status;
import com.fondova.finance.api.auth.AuthenticationResponseListener;
import com.fondova.finance.api.model.Credentials;
import com.fondova.finance.api.model.login.LoginResponse;
import com.fondova.finance.api.quote.QuoteService;
import com.fondova.finance.api.session.NetworkConnectivityService;
import com.fondova.finance.api.session.SessionService;
import com.fondova.finance.api.session.SessionStatus;
import com.fondova.finance.config.AppConfig;
import com.fondova.finance.diagnostics.FinanceXAnalytics;
import com.fondova.finance.persistance.AppStorageInterface;
import com.fondova.finance.persistance.QuoteListConverter;
import com.fondova.finance.repo.DefaultSymbolsRepository;
import com.fondova.finance.repo.TextsRepository;
import com.fondova.finance.sync.QuoteSyncItem;
import com.fondova.finance.ui.about.HtmlActivity;
import com.fondova.finance.ui.main.MainActivity;
import com.fondova.finance.ui.util.DialogUtil;
import com.fondova.finance.util.EmailSupportHelper;
import com.fondova.finance.workspace.Workspace;
import com.fondova.finance.workspace.service.WorkspaceService;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class LoginActivity extends LifecycleActivity implements LoginView {

    private static final String TAG = "LoginActivity";
    private static final String EXTRA_FORCED_LOGOUT = "extra_forced_logout";

    private static final int REQUEST_CODE_GOOGLE_SIGN_IN_RESOLUTION = 3;
    private final int EULA_REQUEST_CODE = 10001;

    // ---------------------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------------------
    @Inject
    DialogUtil dialogUtil;
    @Inject
    SessionService sessionService;
    @Inject
    AppStorageInterface appStorage;
    @Inject
    TextsRepository textsRepository;
    @Inject
    NetworkConnectivityService networkConnectivityService;
    @Inject
    EmailSupportHelper emailSupportHelper;
    @Inject
    DefaultSymbolsRepository defaultSymbolsRepository;
    @Inject
    WorkspaceService workspaceService;
    @Inject
    AppConfig appConfig;
    @Inject
    QuoteService quoteService;

    @BindView(R.id.et_username) EditText etUsername;
    @BindView(R.id.et_password) EditText etPassword;
    @BindView(R.id.btn_login) Button btnLogin;
    @BindView(R.id.tv_app_version) TextView tvAppVersion;
    @BindView(R.id.cb_remember_me) CheckBox cbRememberCredentials;
    @BindView(R.id.til_username) TextInputLayout tilUsername;
    @BindView(R.id.til_password) TextInputLayout tilPassword;
    @BindView(R.id.loading_indicator) View loadingIndicator;
    private ProgressDialog progressDialog;
    private boolean isForeground = false;
    private boolean showedGoogleLogin = false;
    private FinanceXAnalytics analytics;
    private boolean showedEula = false;

    // ---------------------------------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EULA_REQUEST_CODE) {
            showedEula = true;
        } else if (resultCode == RESULT_CANCELED) {
            Log.i(TAG, "onActivityResult: canceled");
            //loginUseCase.onCancelDriveLogin();
        } else if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN_RESOLUTION
                && resultCode == RESULT_OK) {
            Log.i(TAG, "onActivityResult: connect to google");
            //loginUseCase.connectToGoogle();.

        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.analytics = new FinanceXAnalytics(this);
        App.getAppComponent().inject(this);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        showAppVersion(textsRepository.appVersion());

        sessionService.setWorkspaceSelector(this, (legacy, current, listener) -> {
            Workspace autoselected = autoSelectWorkspace(legacy, current);
            if (autoselected != null) {
                listener.didSelectWorkspace(autoselected);
                return;
            }

            new AlertDialog.Builder(this)
                    .setMessage(R.string.migrate_google_cloud_quote_list_msg)
                    .setNegativeButton(R.string.use_legacy_quote_list, (dialogInterface, i) -> {
                        workspaceService.saveWorkspace(legacy, () -> appStorage.setDidMigrateGoogleQuoteList(true));
                        listener.didSelectWorkspace(workspaceOrDefault(legacy));
                    })
                    .setPositiveButton(R.string.use_stock_cloud_quote_list, (dialogInterface, i) -> {
                        appStorage.setDidMigrateGoogleQuoteList(true);
                        listener.didSelectWorkspace(workspaceOrDefault(current));
                    })
                    .show();
        });
        if (sessionService.getSessionStatusLiveData().getValue() == SessionStatus.seatbump) {
            showForcedLogoutMessage();
        }

    }

    private Workspace autoSelectWorkspace(Workspace legacy, Workspace current) {
        if (appStorage.getDidMigrateGoogleQuoteList()) {
            return current;
        }
        if (legacy == null && current == null) {
            return defaultSymbolsRepository.createDefaultWorkspace();
        }
        if (legacy == null) {
            return current;
        }
        if (current == null) {
            workspaceService.saveWorkspace(legacy, () -> appStorage.setDidMigrateGoogleQuoteList(true));
            return legacy;
        }
        if (areWorkspacesEqual(legacy, current)) {
            return current;
        }
        return null;
    }

    private boolean areWorkspacesEqual(Workspace legacy, Workspace current) {
        List<QuoteSyncItem> legacyList = QuoteListConverter.Companion.convertWorkspaceIntoQuoteList(legacy);
        List<QuoteSyncItem> currentList =  QuoteListConverter.Companion.convertWorkspaceIntoQuoteList(current);
        if (legacyList.size() != currentList.size()) {
            return false;
        }

        for (int i = 0; i < legacyList.size(); i++) {
            if (!legacyList.get(i).displayName.equals(currentList.get(i).displayName)
                    || !legacyList.get(i).requestName.equals(currentList.get(i).requestName)
                    || legacyList.get(i).type != currentList.get(i).type) {
                return false;
            }
        }

        return true;

    }

    private Workspace workspaceOrDefault(Workspace workspace) {
        if (workspace == null) {
            return defaultSymbolsRepository.createDefaultWorkspace();
        }
        return workspace;
    }

    @Override
    protected void onPause() {
        isForeground = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        analytics.setActivity(this);

        isForeground = true;
        if (appStorage.getShouldRememberCredentials()) {
            populateCredentials(appStorage.getCredentials());
        }
        checkRememberMe(appStorage.getShouldRememberCredentials());
        if (sessionService.getSessionStatusLiveData().getValue() == SessionStatus.seatbump) {
            return;
        }
        if(sessionService.getSessionStatusLiveData().getValue() == SessionStatus.connected) {
            goToMainScreen();
            return;
        }

        checkAutoLogin();

    }

    private void checkAutoLogin() {
        if (!appStorage.getAcceptedEula()) {
            return;
        }
        if ((appStorage.getShouldRememberCredentials() || showedEula)
                && networkConnectivityService.isNetworkAvailable()
                && sessionService.getSessionStatusLiveData().getValue() != SessionStatus.userLoggedOut
                && sessionService.getSessionStatusLiveData().getValue() != SessionStatus.connectionLost) {
            login(appStorage.getCredentials());
        }
    }

    // ---------------------------------------------------------------------------------------------
    // OnClick
    // ---------------------------------------------------------------------------------------------
    @OnClick(R.id.btn_login)
    public void onLoginClick() {
        sessionService.clearCache();
        quoteService.reset();
        if (!networkConnectivityService.isNetworkAvailable()) {
            showApiError(getResources().getString(R.string.network_error), getResources().getString(R.string.no_network_message));
            return;
        }
        if (areCredentialNotValid(etUsername.getText().toString(), etPassword.getText().toString())) {
            return;
        }
        Credentials credentials = Credentials.create(etUsername.getText().toString(), etPassword.getText().toString());
        appStorage.setShouldRememberCredentials(cbRememberCredentials.isChecked());
        appStorage.setCredentials(credentials);

        if (!appStorage.getAcceptedEula()) {
            goToEulaScreen();
            return;
        }

        login(credentials);
    }

    private void login(Credentials credentials) {
        showLoading();
        sessionService.authenticate(credentials, new AuthenticationResponseListener() {
            @Override
            public void onAuthenticationResponse(@NotNull Resource<LoginResponse> response) {
                if (response.status == Status.ERROR) {
                    showApiError(response.title, response.message);
                    hideLoading();
                    return;
                }
                if (cbRememberCredentials.isChecked()) {
                    appStorage.setCredentials(credentials);
                    Log.i(TAG, "Logged in");
                }

                analytics.setUserId(credentials.username);

                goToMainScreen();

            }
        });

    }

    @OnClick(R.id.tv_support)
    void onSupportClick() {
        showLoading();
        Intent intent = emailSupportHelper.createIntent();
        openEmailIntent(intent);
        hideLoading();
    }


    // ---------------------------------------------------------------------------------------------
    // View
    // ---------------------------------------------------------------------------------------------

    @Override
    public void openEmailIntent(Intent intent) {
        startActivity(intent);
    }

    @Override
    public void enableLoginButton(boolean enable) {
        btnLogin.setText(enable ? R.string.login : R.string.loading___);
        btnLogin.setEnabled(enable);
    }

    @Override
    public void showApiError(String title, String message) {
        if (!isForeground) {
            return;
        }
        dialogUtil.showErrorDialog(this, title, message);
    }

    @Override
    public void showEmptyUsernameWarning() {
        tilUsername.setError(getString(R.string.empty_username));
    }

    @Override
    public void showEmptyPasswordWarning() {
        tilPassword.setError(getString(R.string.empty_password));
    }

    @Override
    public void hideUsernameError() {
        tilUsername.setError(null);
    }

    @Override
    public void hidePasswordError() {
        tilPassword.setError(null);
    }

    @Override
    public void goToMainScreen() {
        MainActivity.start(this);
        hideLoading();
        finish();
    }

    @Override
    public void goToEulaScreen() {
        startActivityForResult(HtmlActivity.acceptEulaScreen(this), EULA_REQUEST_CODE);
    }

    @Override
    public void showAppVersion(String appVersion) {
        tvAppVersion.setText(getString(R.string.version_number, appVersion));
    }

    @Override
    public void populateCredentials(Credentials credentials) {
        etUsername.setText(credentials.username);
        etPassword.setText(credentials.password);
    }

    @Override
    public void checkRememberMe(boolean rememberCredentials) {
        cbRememberCredentials.setChecked(rememberCredentials);
    }

    @Override
    public void showNoInternetError() {
        dialogUtil.showMessage(this, R.string.network_error, R.string.no_network_message);
    }

    @Override
    public boolean isStartedAfterForcedLogout() {
        return getIntent().getBooleanExtra(EXTRA_FORCED_LOGOUT, false);
    }

    @Override
    public void showForcedLogoutMessage() {
        Log.i(TAG, "Showing forced logout message: $");

        dialogUtil.showMessage(this, R.string.disconnect, R.string.forced_logout_message);
    }

    @Override
    public void showDownloadOrUploadDataToCloudDialog(boolean firstTimeLogin) {
        dialogUtil.genericDialog(this, R.string.new_google_cloud_data_available_title,
                R.string.new_google_cloud_data_available_msg,
                R.string.new_google_cloud_data_available_positive,
                R.string.new_google_cloud_data_available_negative,
                false,
                new DialogUtil.ButtonClickListener() {
                    @Override
                    public void positive() {
                        if (firstTimeLogin) {
                            //loginUseCase.clickedUploadDataToGooleFirstLogin();
                        } else {
                            //loginUseCase.clickedUploadDataToGoogle();
                        }
                    }

                    @Override
                    public void negative() {
                        //loginUseCase.downloadDeviceDataFromGoogle();
                    }
                });
    }

    @Override
    public void showUploadDataWarning() {
        dialogUtil.showMessage(this, R.string.overwrite_data_title, R.string.overwrite_data_msg,
                R.string.overwrite_data_positive, R.string.cancel,
                new DialogUtil.ButtonClickListener() {
                    @Override
                    public void positive() {
                        //loginUseCase.uploadLocalDataToCloud();
                    }

                    @Override
                    public void negative() {
                        //loginUseCase.uploadCancelClicked();
                    }
                });
    }

    @Override
    public void showDriveApiError(ConnectionResult result) {
        GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
    }

    @Override
    public void startResolutionForResult(ConnectionResult result) {
        try {
            result.startResolutionForResult(this, REQUEST_CODE_GOOGLE_SIGN_IN_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override
    public void showStorageLocationQuestion() {
        dialogUtil.genericDialog(this, R.string.google_drive, R.string.google_drive_login_msg,
                R.string.allow_cloud_data, R.string.use_local_data, false,
                new DialogUtil.ButtonClickListener() {
                    @Override
                    public void positive() {
                        //loginUseCase.onStorageLocationSelected(StorageLocation.CLOUD);
                    }

                    @Override
                    public void negative() {
                        //loginUseCase.onStorageLocationSelected(StorageLocation.LOCAL);
                    }
                });
    }

    @Override
    public void showLoading() {
        loadingIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        loadingIndicator.setVisibility(View.GONE);
    }

    // ---------------------------------------------------------------------------------------------
    // Start
    // ---------------------------------------------------------------------------------------------
    public static void start(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public static void startAfterForcedLogout(Context context) {
        if (context instanceof LoginActivity) {
            return;
        }
        Log.i(TAG, "Starting Login activity after forced logout from " + context);
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(EXTRA_FORCED_LOGOUT, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private boolean areCredentialNotValid(String username, String password) {
        hidePasswordError();
        hideUsernameError();

        if (isCredentialEmpty(username) && isCredentialEmpty(password)) {
            showEmptyUsernameWarning();
            showEmptyPasswordWarning();
            return true;
        }

        if (isCredentialEmpty(username)) {
            showEmptyUsernameWarning();
            return true;
        }

        if (isCredentialEmpty(password)) {
            showEmptyPasswordWarning();
            return true;
        }

        return false;
    }

    private boolean isCredentialEmpty(String credential) {
        return TextUtils.isEmpty(credential.trim());
    }

}
