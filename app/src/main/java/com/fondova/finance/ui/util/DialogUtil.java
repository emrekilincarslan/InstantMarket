package com.fondova.finance.ui.util;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fondova.finance.R;
import com.fondova.finance.util.NewsCategoryQueryBuilder;
import com.fondova.finance.vo.NewsCategory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;

@Singleton
public class DialogUtil {

    private boolean nameFocused;
    private boolean selectedAND;
    private NewsCategoryQueryBuilder newsCategoryQueryBuilder;


    @Inject
    public DialogUtil(NewsCategoryQueryBuilder newsCategoryQueryBuilder) {
        this.newsCategoryQueryBuilder = newsCategoryQueryBuilder;
    }

    public void showErrorDialog(Context context, String title, String message) {
        showMessage(context, title, message);
    }

    public void showErrorDialog(Context context, @StringRes int message) {
        showMessage(context, R.string.error, context.getString(message));
    }

    public void showMessage(Context context, @StringRes int message) {
        showMessage(context, null, context.getString(message));
    }

    public AlertDialog showMessage(Context context, @StringRes int title, @StringRes int message) {
        return showMessage(context, title, context.getString(message));
    }

    public AlertDialog showMessage(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok, null);
        builder.setCancelable(true);

        return builder.show();
    }

    public void showMessage(Context context, @StringRes int title, @StringRes int message,
            @StringRes int positiveBtn, @StringRes int negativeBtn,
            ButtonClickListener buttonClickListener) {

        DialogInterface.OnClickListener onClickListener = (dialogInterface, which) -> {
            switch (which) {
                case BUTTON_NEGATIVE:
                    if (buttonClickListener != null) {
                        buttonClickListener.negative();
                    }
                    break;
                case BUTTON_POSITIVE:
                    if (buttonClickListener != null) {
                        buttonClickListener.positive();
                    }
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveBtn, onClickListener);
        builder.setNegativeButton(negativeBtn, onClickListener);
        builder.setCancelable(false);

        builder.show();
    }

    public void genericDialog(Context context, @StringRes int title, @StringRes int message,
            @StringRes int positiveBtn, @StringRes int negativeBtn, boolean cancelable,
            ButtonClickListener buttonClickListener) {

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_generic_layout, null);

        TextView btnPositive = view.findViewById(R.id.tv_positive_btn);
        TextView btnNegative = view.findViewById(R.id.tv_negative_btn);
        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvMsg = view.findViewById(R.id.tv_msg);

        btnPositive.setText(positiveBtn);
        btnNegative.setText(negativeBtn);
        tvTitle.setText(title);
        tvMsg.setText(message);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        builder.setCancelable(cancelable);

        AlertDialog dialog = builder.show();

        if (buttonClickListener != null) {
            btnPositive.setOnClickListener(view1 -> {
                buttonClickListener.positive();
                dialog.dismiss();
            });
            btnNegative.setOnClickListener(view1 -> {
                buttonClickListener.negative();
                dialog.dismiss();
            });
        }
    }

    public ProgressDialog showLoading(Context context) {
        return ProgressDialog.show(context, null, context.getString(R.string.loading___), true);
    }

    public void editNewsCategory(Context context, NewsCategory category,
            UpdateNewsCategoryListener listener) {
        int blueColor = ContextCompat.getColor(context, R.color.blue);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_news_category, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        builder.setPositiveButton(R.string.update, null);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();

        showKeyboard(dialog);

        Button negative = dialog.getButton(BUTTON_NEGATIVE);
        Button positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button btnAdd = view.findViewById(R.id.btn_and);
        Button btnOr = view.findViewById(R.id.btn_or);
        EditText etKeyword = view.findViewById(R.id.et_keyword);
        EditText etName = view.findViewById(R.id.et_name);

        if (negative != null) {
            negative.setTextColor(blueColor);
        }

        btnAdd.setOnClickListener(view1 -> {
            btnAdd.setAlpha(1f);
            btnOr.setAlpha(.2f);
            selectedAND = true;
        });

        btnOr.setOnClickListener(view2 -> {
            btnAdd.setAlpha(.2f);
            btnOr.setAlpha(1f);
            selectedAND = false;
        });

        if (positive != null) {
            positive.setTextColor(blueColor);

            if (listener != null) {
                positive.setOnClickListener(v -> {
                    String keywords = etKeyword.getText().toString();
                    String query = newsCategoryQueryBuilder.makeQuery(keywords, selectedAND);

                    category.keywords = keywords;
                    category.name = etName.getText().toString();
                    category.query = query;

                    listener.onUpdatedCategory(category);
                    dialog.dismiss();
                });
            }
        }

        etName.setText(category.name);
        if (category.query == null || category.query.isEmpty()) return;
        if (category.query.matches(".*\\bAND\\b.*")) {
            btnAdd.performClick();
        } else if (category.query.matches(".*\\bOR\\b.*")) {
            btnOr.performClick();
        }

        etKeyword.setText(category.keywords);
    }

    public void addNewsCategoryDialog(Context context, AddNewsCategoryListener listener) {
        nameFocused = false;
        selectedAND = true;

        float disabledAlpha = .3f;
        int blueColor = ContextCompat.getColor(context, R.color.blue);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_news_category, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        builder.setPositiveButton(R.string.add, null);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();

        showKeyboard(dialog);

        Button negative = dialog.getButton(BUTTON_NEGATIVE);
        Button positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button btnAdd = view.findViewById(R.id.btn_and);
        Button btnOr = view.findViewById(R.id.btn_or);
        EditText etKeyword = view.findViewById(R.id.et_keyword);
        EditText etName = view.findViewById(R.id.et_name);

        if (positive != null) {
            positive.setTextColor(blueColor);
            positive.setAlpha(disabledAlpha);
            positive.setEnabled(false);

            if (listener != null) {
                positive.setOnClickListener(v -> {
                    String name = etName.getText().toString();
                    String keywords = etKeyword.getText().toString();

                    listener.onAddedCategory(name, keywords, selectedAND);
                    dialog.dismiss();
                });
            }
        }

        if (negative != null) {
            negative.setTextColor(blueColor);
        }

        btnAdd.setOnClickListener(view1 -> {
            btnAdd.setAlpha(1f);
            btnOr.setAlpha(.2f);
            selectedAND = true;
        });

        btnOr.setOnClickListener(view2 -> {
            btnAdd.setAlpha(.2f);
            btnOr.setAlpha(1f);
            selectedAND = false;
        });

        etKeyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (positive != null) {
                    positive.setEnabled(s.length() > 0);
                    positive.setAlpha(s.length() > 0 ? 1f : disabledAlpha);
                }

                if (!nameFocused) etName.setText(s);
            }
        });

        etName.setOnFocusChangeListener((view12, hasFocus) -> {
            if (hasFocus) {
                nameFocused = true;
            }
        });

    }

    private AlertDialog showMessage(Context context, @StringRes int title, String message) {
        return showMessage(context, context.getString(title), message);
    }

    public interface AddNewsCategoryListener {
        void onAddedCategory(String name, String keywords, boolean selectedAND);
    }

    public interface UpdateNewsCategoryListener {
        void onUpdatedCategory(NewsCategory category);
    }

    public interface ButtonClickListener {
        void positive();

        void negative();
    }

    private void showKeyboard(AlertDialog dialog) {
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }
}
