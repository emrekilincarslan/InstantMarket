package com.fondova.finance.ui.user.settings;

import android.app.ProgressDialog;
import android.arch.lifecycle.LifecycleObserver;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.fondova.finance.R;
import com.fondova.finance.di.components.AppComponent;
import com.fondova.finance.diagnostics.DiagnosticsActivity;
import com.fondova.finance.ui.PxFragment;
import com.fondova.finance.ui.about.HtmlActivity;
import com.fondova.finance.ui.main.MainActivity;
import com.fondova.finance.ui.user.login.LoginActivity;
import com.fondova.finance.ui.util.DialogUtil;
import com.fondova.finance.ui.util.MultiTapListener;
import com.fondova.finance.util.DelayedTaskRunner;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;


public class SettingsFragment extends PxFragment implements SettingsView {

    public static final String TAG = SettingsFragment.class.getSimpleName();

    // ---------------------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------------------
    @Inject SettingsUseCase useCase;
    @Inject DialogUtil dialogUtil;

    @BindView(R.id.btn_about) Button btnAbout;
    @BindView(R.id.cb_5_seconds) CheckBox cb5Seconds;
    @BindView(R.id.cb_30_seconds) CheckBox cb30Seconds;
    @BindView(R.id.cb_60_seconds) CheckBox cb60Seconds;
    @BindView(R.id.cb_5_minutes) CheckBox cb5Minutes;
    @BindView(R.id.cb_off_manual_refresh) CheckBox cbOffManualRefresh;
    @BindView(R.id.btn_disconnect) Button btnDisconnect;
    @BindView(R.id.tv_help) TextView supportView;

    private ProgressDialog progressDialog;
    private DelayedTaskRunner connectSpamBlockerTimer = new DelayedTaskRunner(2);
    private boolean connectSpamBlock = false;

    // ---------------------------------------------------------------------------------------------
    // New instance
    // ---------------------------------------------------------------------------------------------
    public static SettingsFragment newInstance() {
        Bundle args = new Bundle();
        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public SettingsFragment() {
    }

    // ---------------------------------------------------------------------------------------------
    // PxFragment
    // ---------------------------------------------------------------------------------------------

    @Override
    protected int layoutId() {
        return R.layout.fragment_settings;
    }

    @Override
    protected void doInject(AppComponent component) {
        component.inject(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        supportView.setOnTouchListener(new MultiTapListener(4, (view, motionEvent) -> {
            DiagnosticsActivity.Companion.start(getContext());
            return true;
        }));
    }

    @Override
    protected LifecycleObserver getLifecycleObserver() {
        return useCase;
    }


    // ---------------------------------------------------------------------------------------------
    // SettingsView
    // ---------------------------------------------------------------------------------------------
    @Override
    public void uncheckAll() {
        cb5Seconds.setChecked(false);
        cb30Seconds.setChecked(false);
        cb60Seconds.setChecked(false);
        cb5Minutes.setChecked(false);
        cbOffManualRefresh.setChecked(false);
    }

    @Override
    public void goToAbout() {
        startActivity(HtmlActivity.newInstance(getActivity(), HtmlActivity.ABOUT));
    }

    @Override
    public void goToEula() {
        startActivity(HtmlActivity.newInstance(getActivity(), HtmlActivity.EULA));
    }

    @Override
    public void goToWelcome() {
        Uri uri = Uri.parse("https://help.stockfinancex.com/");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public void goToPrivacyPolicy(@ColorRes int ctColor, @DrawableRes int backIcon, @NonNull final String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(getContext(), ctColor));

        builder.setCloseButtonIcon(BitmapFactory.decodeResource(
                getResources(), backIcon));
        builder.setShowTitle(true);
        CustomTabsIntent customTabsIntent = builder.build();
        try {
            customTabsIntent.launchUrl(getActivity(), Uri.parse(url));
        } catch (ActivityNotFoundException e) {
            startActivity(HtmlActivity.newInstance(getActivity(), HtmlActivity.PRIVACY));
        }
    }

    @Override
    public void openUrl(String url) {
        try {
            Uri webpage = Uri.parse(url);
            Intent myIntent = new Intent(Intent.ACTION_VIEW, webpage);
            startActivity(myIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(),
                    "No application can handle this request. Please install a web browser or check your URL.",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void openEmailIntent(Intent intent) {
        startActivity(intent);
    }

    @Override
    public void updateToolbarTitle(@StringRes int titleId) {
        ((MainActivity) getActivity()).getToolbar().setTitle(titleId); //FIXME Call requires API 21
    }

    @Override
    public void goToLoginScreen() {
        LoginActivity.start(getContext());
        getActivity().finish();
    }

    @Override
    public void setAboutVersion(String version) {
        btnAbout.setText(getString(R.string.about_stock_financex_version, version));
    }

    public void mark5SecondsCheckbox(boolean checked, boolean skipAnimation) {
        cb5Seconds.post(() -> {
            cb5Seconds.setChecked(checked);
            if (skipAnimation) {
                cb5Seconds.jumpDrawablesToCurrentState();
            }
        });
    }

    @Override
    public void mark30SecondsCheckbox(boolean checked, boolean skipAnimation) {
        cb30Seconds.post(() -> {
            cb30Seconds.setChecked(checked);
            if (skipAnimation) {
                cb30Seconds.jumpDrawablesToCurrentState();
            }
        });
    }

    @Override
    public void mark60SecondsCheckbox(boolean checked, boolean skipAnimation) {
        cb60Seconds.post(() -> {
            cb60Seconds.setChecked(checked);
            if (skipAnimation) {
                cb60Seconds.jumpDrawablesToCurrentState();
            }
        });
    }

    @Override
    public void mark5MinutesCheckbox(boolean checked, boolean skipAnimation) {
        cb5Minutes.post(() -> {
            cb5Minutes.setChecked(checked);
            if (skipAnimation) {
                cb5Minutes.jumpDrawablesToCurrentState();
            }
        });
    }

    @Override
    public void markOffCheckbox(boolean checked, boolean skipAnimation) {
        cbOffManualRefresh.post(() -> {
            cbOffManualRefresh.setChecked(checked);
            if (skipAnimation) {
                cbOffManualRefresh.jumpDrawablesToCurrentState();
            }
        });
    }

    @Override
    public void showError(String title, String msg) {
        dialogUtil.showErrorDialog(getContext(), title, msg);
    }

    @Override
    public void populateConnectString(@StringRes int stringRes) {
        btnDisconnect.setText(stringRes);
    }

    @Override
    public void setDisconnectButtonEnabled(boolean enabled) {
        btnDisconnect.setEnabled(enabled);
    }

    @Override
    public void showError(@StringRes int title, @StringRes int msg) {
        dialogUtil.showMessage(getContext(), title, msg);
    }

    @Override
    public void forceLogout() {
        LoginActivity.startAfterForcedLogout(getContext());
        getActivity().finish();
    }

    @Override
    public void showConnectivityIcon(@DrawableRes int iconRes) {
        getActivity().getActionBar().setDisplayShowHomeEnabled(true);
        getActivity().getActionBar().setIcon(iconRes);
    }

    @Override
    public void showLoading() {
        progressDialog = dialogUtil.showLoading(getContext());
    }

    @Override
    public void hideLoading() {
        progressDialog.dismiss();
    }

    // ---------------------------------------------------------------------------------------------
    // OnClick
    // ---------------------------------------------------------------------------------------------
    @OnClick(
            {R.id.tv_5_seconds, R.id.tv_30_seconds, R.id.tv_60_seconds, R.id.tv_5_minutes, R.id.tv_off_manual_refresh})
    void clickedCheckbox(TextView view) {
        useCase.clickedCheckbox(view.getText().toString());
    }

    @OnClick(R.id.btn_help)
    void onHelpClick() {
        useCase.clickedHelp();
    }

    @OnClick(R.id.btn_about)
    void onAboutClick() {
        useCase.clickedAbout();
    }

    @OnClick(R.id.btn_eula)
    void onEulaClick() {
        useCase.clickedEula();
    }

    @OnClick(R.id.btn_privacy_policy)
    void onPrivacyPolicyClick() {
        useCase.clickedPrivacyPolicy();
    }

    @OnClick(R.id.btn_email)
    void onEmailClick() {
        useCase.clickedEmailSupport();
    }

    @OnClick(R.id.btn_welcome)
    void onWelcomeClick() {
        useCase.clickedWelcome();
    }

    @OnClick(R.id.btn_logout)
    void onLogoutClick() {
        useCase.clickedLogout();
    }

    @OnClick(R.id.btn_disconnect)
    void onDisconnectClicked() {
        if (connectSpamBlock) {
            return;
        }
        connectSpamBlock = true;
        connectSpamBlockerTimer.run(() -> connectSpamBlock = false);
        useCase.clickedDisconnect();
    }


}
