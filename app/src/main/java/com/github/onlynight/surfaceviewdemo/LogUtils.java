package com.github.onlynight.surfaceviewdemo;

import android.text.TextUtils;

import com.orhanobut.logger.Logger;

/**
 * Created by lion on 2016/6/15 0015.
 */
public class LogUtils {

    static {
        Logger.init();
    }

    public static void d(Object... args) {
        if (!Config.RELEASE) {
            String output = combineParams(args);
            if (!TextUtils.isEmpty(output)) {
                Logger.d(output);
            } else {
                Logger.e("params is null");
            }
        }
    }

    public static void i(Object... args) {
        if (!Config.RELEASE) {
            String output = combineParams(args);
            if (!TextUtils.isEmpty(output)) {
                Logger.i(output);
            } else {
                Logger.e("params is null");
            }
        }
    }

    public static void e(Object... args) {
        if (!Config.RELEASE) {
            String output = combineParams(args);
            if (!TextUtils.isEmpty(output)) {
                Logger.e(output);
            } else {
                Logger.e("params is null");
            }
        }
    }

    public static void exception(Throwable throwable) {
        if (!Config.RELEASE) {
            if (throwable != null) {
                Logger.e(throwable.getMessage());
            } else {
                Logger.e("params is null");
            }
        }
    }

    private static String combineParams(Object... args) {
        if (args == null) {
            return null;
        }

        String output = "";
        for (Object arg : args) {
            output += arg.toString();
        }

        return output;
    }
}

class Config {
    static final boolean RELEASE = false;
}
