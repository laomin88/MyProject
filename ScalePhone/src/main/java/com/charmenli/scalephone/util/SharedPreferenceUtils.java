package com.charmenli.scalephone.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by charmenli on 2014/3/27.
 */
public class SharedPreferenceUtils {
    private static final String TAG = SharedPreferenceUtils.class.getSimpleName();

    private static boolean isInit = false;
    private static SharedPreferences mSharedPreferences;
    private static SharedPreferences mSettingSharedPreferences;
    private static SharedPreferences mAppSensorSettingSharedPreferences;

    private static void checkInit() {
        if (!isInit) {
            throw new IllegalStateException("call init first");
        }
    }

    public static void init(Context context, String prefName) {
        if (context != null) {
            isInit = true;
            mSharedPreferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
            mSettingSharedPreferences = context.getSharedPreferences("com.charmenli.scalephone_preferences", Context.MODE_PRIVATE);
        }
    }

    public static void init(Context context) {
        init(context, "pref_" + context.getPackageName());
    }

    public static SharedPreferences getSharedPreference() {
        checkInit();
        return mSharedPreferences;
    }

    private static SharedPreferences getSettingsSharedPreference() {
        checkInit();
        return mSettingSharedPreferences;
    }

    public static boolean isLockIndicatorPosition() {
        return getSettingsSharedPreference().getBoolean("pref_fixed_position", false);
    }

    public static float getIndicatorScale() {
        return Float.parseFloat(getSettingsSharedPreference().getString(PrefConf.PREF_INDICATOR_SIZE, "1.0"));
    }

    public static int getSingleClickFunction() {
        return parseInt(getSettingsSharedPreference().getString(PrefConf.PREF_FUNCTION_SINGLE_CLICK, "0"));
    }

    public static int getDoubleClickFunction() {
        return parseInt(getSettingsSharedPreference().getString(PrefConf.PREF_FUNCTION_DOUBLE_CLICK, "1"));
    }

    public static int getLongPressFunction() {
        return parseInt(getSettingsSharedPreference().getString(PrefConf.PREF_FUNCTION_LONG_PRESS, "2"));
    }

    public static int getTurnPageType(String pkgName) {
        return parseInt(getSettingsSharedPreference().getString(pkgName, "0"));
    }

    public static boolean isAutoBoot() {
        return getSettingsSharedPreference().getBoolean(PrefConf.PREF_AUTO_BOOT, false);
    }

    private static int parseInt(String val, int defVal) {
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return defVal;
    }

    private static int parseInt(String val) {
        return parseInt(val, -1);
    }

}
