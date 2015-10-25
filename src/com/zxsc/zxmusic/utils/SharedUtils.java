package com.zxsc.zxmusic.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * * * * * * * * * * * * * * * * * * * * * * *
 * Created by zhaoyiding
 * Date: 15/10/16
 * * * * * * * * * * * * * * * * * * * * * * *
 **/
public class SharedUtils {

    public static String SHARE_NAME = "share";

    private static SharedPreferences getPreference(Context ctx) {
        return ctx.getSharedPreferences(SHARE_NAME, Context.MODE_PRIVATE);
    }

    public static int getInt(Context ctx, String key, int defValue) {
        return getPreference(ctx).getInt(key, defValue);
    }

    public static void saveInt(Context ctx, String key, int value) {
        getPreference(ctx).edit().putInt(key, value).commit();
    }

    public static boolean getBoolean(Context ctx, String key, boolean defValue) {
        return getPreference(ctx).getBoolean(key, defValue);
    }

    public static void saveBoolean(Context ctx, String key, boolean value) {
        getPreference(ctx).edit().putBoolean(key, value).commit();
    }
}
