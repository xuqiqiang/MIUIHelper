package com.xuqiqiang.fuckmiui.utils;

import android.util.Log;

import java.util.Arrays;
import rikka.parcelablelist.BuildConfig;

@SuppressWarnings("unused")
public class L {
    public static final String TAG = "UIKit";
    public static boolean enabled = BuildConfig.DEBUG;

    public static void e(Object msg) {
        if (!enabled) return;
        Log.e(TAG, msg.toString());
    }

    public static void e(Object msg, Throwable e) {
        if (!enabled) return;
        Log.e(TAG, msg.toString(), e);
    }

    public static void d(Object msg) {
        if (!enabled) return;
        Log.d(TAG, msg.toString());
    }

    public static void d(Object... msg) {
        if (!enabled || ArrayUtils.isEmpty(msg)) return;
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < msg.length; i += 1) {
            str.append(msg[i] == null ? "null" : msg[i].toString());
            if (i < msg.length - 1)
                str.append(" ");
        }
        Log.d(TAG, str.toString());
    }

    public static void d(Object msg, Throwable e) {
        if (!enabled) return;
        Log.d(TAG, msg.toString(), e);
    }

    public static void d(Object msg, Object[] arr) {
        if (!enabled) return;
        Log.d(TAG, msg + ":" + Arrays.toString(arr));
    }

    public static void e(String tag, Object msg) {
        if (!enabled) return;
        Log.e(tag, msg.toString());
    }

    public static void e(String tag, Object msg, Throwable e) {
        if (!enabled) return;
        Log.e(tag, msg.toString(), e);
    }

    public static void d(String tag, Object msg, Throwable e) {
        if (!enabled) return;
        Log.d(tag, msg.toString(), e);
    }

    public static void d(String tag, Object msg, Object[] arr) {
        if (!enabled) return;
        Log.d(tag, msg + ":" + Arrays.toString(arr));
    }
}
