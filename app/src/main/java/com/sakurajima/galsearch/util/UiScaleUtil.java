package com.sakurajima.galsearch.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

public final class UiScaleUtil {
    public static final String PREFS_NAME = "yukihub_prefs";
    public static final String KEY_UI_FONT_SCALE = "ui_font_scale";
    public static final float DEFAULT_FONT_SCALE = 1.0f;
    public static final float MIN_FONT_SCALE = 0.85f;
    public static final float MAX_FONT_SCALE = 1.30f;

    private UiScaleUtil() { }

    public static float getFontScale(Context context) {
        if (context == null) return DEFAULT_FONT_SCALE;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return clamp(prefs.getFloat(KEY_UI_FONT_SCALE, DEFAULT_FONT_SCALE));
    }

    public static void setFontScale(Context context, float scale) {
        if (context == null) return;
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putFloat(KEY_UI_FONT_SCALE, clamp(scale))
                .apply();
    }

    public static void resetFontScale(Context context) {
        if (context == null) return;
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_UI_FONT_SCALE)
                .apply();
    }

    public static Context wrap(Context base) {
        if (base == null) return null;
        float scale = getFontScale(base);
        Configuration config = new Configuration(base.getResources().getConfiguration());
        config.fontScale = scale;
        return base.createConfigurationContext(config);
    }

    public static float clamp(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) return DEFAULT_FONT_SCALE;
        return Math.max(MIN_FONT_SCALE, Math.min(MAX_FONT_SCALE, value));
    }

    public static int percent(float scale) {
        return Math.round(clamp(scale) * 100f);
    }
}
