package com.sakurajima.galsearch;

import android.app.Application;
import android.content.SharedPreferences;

import com.sakurajima.galsearch.util.UiScaleUtil;

public class SakurajimaApp extends Application {
    private static Boolean sSavedPortrait = null;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences prefs = getSharedPreferences("yukihub_prefs", MODE_PRIVATE);
        sSavedPortrait = prefs.getBoolean("pref_portrait_mode", false);
    }

    public static boolean isSavedPortrait() {
        return sSavedPortrait != null && sSavedPortrait;
    }

    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(UiScaleUtil.wrap(base));
    }
}
