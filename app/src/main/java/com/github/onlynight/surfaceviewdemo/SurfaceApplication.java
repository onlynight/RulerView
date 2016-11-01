package com.github.onlynight.surfaceviewdemo;

import android.app.Application;
import android.content.Context;

/**
 * Created by lion on 2016/10/25.
 */

public class SurfaceApplication extends Application {

    private static Context context;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        context = this;
    }

    public static Context getContext() {
        return context;
    }
}
