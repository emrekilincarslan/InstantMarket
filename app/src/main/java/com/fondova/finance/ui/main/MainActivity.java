package com.fondova.finance.ui.main;

import android.arch.lifecycle.LifecycleObserver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;

import com.fondova.finance.R;
import com.fondova.finance.config.AppConfig;
import com.fondova.finance.db.KeyValueDao;
import com.fondova.finance.di.components.AppComponent;
import com.fondova.finance.ui.PxActivity;
import com.fondova.finance.ui.news.NewsFragment;
import com.fondova.finance.ui.symbol.QuoteFragment;
import com.fondova.finance.ui.user.settings.SettingsFragment;

import javax.inject.Inject;

import butterknife.BindView;

public class MainActivity extends PxActivity implements MainView {

    // ---------------------------------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------------------------------
    @BindView(R.id.bottom_navigation) BottomNavigationView bottomNavigationItemView;
    @BindView(R.id.app_bar_layout) AppBarLayout appBarLayout;

    @Inject MainUseCase mainUseCase;
    @Inject AppConfig appConfig;
    @Inject
    KeyValueDao keyValueDao;


    // ---------------------------------------------------------------------------------------------
    // Private
    // ---------------------------------------------------------------------------------------------
    private void setupBottomNavigation() {
        if (!appConfig.showNewsTab()) {
            Menu menu = bottomNavigationItemView.getMenu();
            menu.removeItem(R.id.action_news);
        }
        bottomNavigationItemView.setOnNavigationItemSelectedListener(
                item -> {
                    switch (item.getItemId()) {
                        case R.id.action_symbols:
                            mainUseCase.onChartsTabClick();
                            break;
                        case R.id.action_news:
                            mainUseCase.onNewsTabClick();
                            break;
                        case R.id.action_settings:
                            mainUseCase.onSettingsTabClick();
                            break;
                    }
                    return true;
                });
    }



    private void openScreen(Fragment fragment, @Nullable final  String tag) {
        appBarLayout.setExpanded(true, false);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_content, fragment, tag);
        transaction.commit();
    }

    private void writeState(@NonNull final Bundle outPersistentState,  @NonNull final MainState mainState) {
        outPersistentState.putInt(MainState.KEY_SELECTED_POSITION, mainState.getSelectedPosition());
    }

    private MainState restoreState(@Nullable final Bundle outPersistentState) {
        return new MainState(outPersistentState == null ? 0 : outPersistentState.getInt(MainState.KEY_SELECTED_POSITION));
    }

    // ---------------------------------------------------------------------------------------------
    // Override
    // ---------------------------------------------------------------------------------------------
    @Override
    protected int layoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected LifecycleObserver getLifecycleObserver() {
        return mainUseCase;
    }

    @Override
    protected void doInject(AppComponent component) {
        component.inject(this);
    }


    @Override
    public void setupMainScreen() {
        setupBottomNavigation();
    }

    @Override
    public void openSymbolsScreen() {
        openScreen(QuoteFragment.newInstance(), QuoteFragment.TAG);
    }

    @Override
    public void openNewsScreen() {
        openScreen(NewsFragment.newInstance(), NewsFragment.TAG);
    }

    @Override
    public void openSettingsScreen() {
        openScreen(SettingsFragment.newInstance(), SettingsFragment.TAG);
    }

    @Override
    public void updateToolbarTitle(@StringRes int titleId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setTitle(getString(titleId));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mainUseCase != null) {
            writeState(outState, mainUseCase.getState());
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if(mainUseCase != null) {
            mainUseCase.restoreState(restoreState(savedInstanceState));
        }

    }

    public void hideToolbar() {
        appBarLayout.setExpanded(false);
    }

    public void showToolbar() {
        appBarLayout.setExpanded(true);
    }

    // ---------------------------------------------------------------------------------------------
    // Start
    // ---------------------------------------------------------------------------------------------
    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }
}
