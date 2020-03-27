package com.fondova.finance.ui.about;

import android.arch.lifecycle.LifecycleObserver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.StringRes;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import com.fondova.finance.R;
import com.fondova.finance.di.components.AppComponent;
import com.fondova.finance.ui.PxActivity;
import com.fondova.finance.ui.main.MainActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

public class HtmlActivity extends PxActivity implements HtmlView {

    // ---------------------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------------------
    public final static int ABOUT = 0;
    public final static int PRIVACY = 1;
    public final static int EULA = 2;

    private static final String FILE = "file";
    private static final String SHOW_ACCEPT_EULA_BTN = "show_accept_eula_btn";
    private static final String GO_TO_MAIN_SCREEN = "go_to_main_screen";

    @BindView(R.id.wv_view) WebView wvView;
    @BindView(R.id.btn_generic) Button btnGeneric;

    @Inject HtmlUseCase useCase;

    // ---------------------------------------------------------------------------------------------
    // New instance
    // ---------------------------------------------------------------------------------------------
    public static Intent newInstance(Context context, int file) {
        Intent in = new Intent(context, HtmlActivity.class);
        in.putExtra(FILE, file);

        return in;
    }

    public static Intent acceptEulaScreen(Context context) {
        Intent in = new Intent(context, HtmlActivity.class);
        in.putExtra(FILE, EULA);
        in.putExtra(SHOW_ACCEPT_EULA_BTN, true);

        return in;
    }

    // ---------------------------------------------------------------------------------------------
    // Overrides
    // ---------------------------------------------------------------------------------------------


    @Override
    public int getFileType() {
        return getIntent().getIntExtra(FILE, ABOUT);
    }

    @Override
    public void addBackArrow() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected int layoutId() {
        return R.layout.activity_about;
    }

    @Override
    protected void doInject(AppComponent component) {
        component.inject(this);
    }

    @Override
    protected LifecycleObserver getLifecycleObserver() {
        return useCase;
    }

    @Override
    public void setHtmlFile(String filename) {
        wvView.loadUrl("file:///android_asset/html/" + filename);
    }

    @Override
    public void loadUrl(String url) {
        wvView.loadUrl(url);
    }

    @Override
    public void updateToolbarTitle(@StringRes int titleId) {
        toolbar.setTitle(titleId); //FIXME call requires API 21
    }

    @Override
    public void showButton(boolean show) {
        if(show) {
            btnGeneric.setVisibility(View.VISIBLE);
        } else {
            btnGeneric.setVisibility(View.GONE);
        }
    }

    @Override
    public void setButtonText(@StringRes int text) {
        btnGeneric.setText(text);
    }

    @Override
    public boolean shouldGoToMainScreen() {
        return getIntent().hasExtra(GO_TO_MAIN_SCREEN);
    }

    @Override
    public boolean shouldShowAcceptEulaBtn() {
        return getIntent().hasExtra(SHOW_ACCEPT_EULA_BTN);
    }

    @Override
    public void goToMainScreen() {
        MainActivity.start(this);
        finish();
    }

    @OnClick(R.id.btn_generic)
    public void onBtnGenericClick() {
        useCase.onBtnGenericClick();
        finish();
    }
}
