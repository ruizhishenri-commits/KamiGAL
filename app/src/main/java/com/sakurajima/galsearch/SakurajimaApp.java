package com.sakurajima.galsearch;

import android.app.Application;

import com.sakurajima.galsearch.util.UiScaleUtil;

public class SakurajimaApp extends Application {
    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(UiScaleUtil.wrap(base));
    }
}
