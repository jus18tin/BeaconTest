package com.example.junhuali.beaconfromrn;

import android.util.Log;

public class Logger {

    private static boolean isDebug = true;

    public static void v(String tag, String msg) {
        if (BuildConfig.DEBUG && isDebug) {
            Log.v(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (BuildConfig.DEBUG && isDebug) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (BuildConfig.DEBUG && isDebug) {
            Log.w(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (BuildConfig.DEBUG && isDebug) {
            Log.d(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (BuildConfig.DEBUG && isDebug) {
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable throwable) {
        if (BuildConfig.DEBUG && isDebug) {
            Log.e(tag, msg, throwable);
        }
    }

}