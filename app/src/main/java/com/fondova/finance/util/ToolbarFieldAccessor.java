package com.fondova.finance.util;

import android.widget.TextView;
import android.widget.Toolbar;

import java.lang.reflect.Field;

/**
 * Created by krasimir.karamazov on 8/31/2017.
 */

public class ToolbarFieldAccessor {
    private static final Field sSubtitleTextViewField = getSubtitleTextViewField();

    private static Field getSubtitleTextViewField() {
        try {
            Field field = Toolbar.class.getDeclaredField("mSubtitleTextView");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException exception) {
            return null;
        }
    }

    public static TextView getSubtitleTextView(Toolbar toolbar) {
        if (sSubtitleTextViewField == null) {
            return null;
        }

        try {
            return (TextView) sSubtitleTextViewField.get(toolbar);
        } catch (IllegalAccessException exception) {
            return null;
        }
    }
}
