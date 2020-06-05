package com.sty.ne.dynamicskinpeeler.base;

import android.app.Application;

import com.sty.ne.skin_library.SkinManager;

public class SkinApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SkinManager.init(this);
    }
}
