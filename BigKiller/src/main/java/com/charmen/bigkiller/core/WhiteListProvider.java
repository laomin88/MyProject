package com.charmen.bigkiller.core;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by charmenli on 14-1-26.
 */
public class WhiteListProvider {

    private static final String PREF_NAME = "pref_white_list";
    private static final String KEY_WHITE_LIST = "key_white_list";
    private SharedPreferences preferences;

    private static WhiteListProvider provider;
    private Context context;

    private WhiteListProvider(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static WhiteListProvider getInstance() {
        return provider;
    }

    public static WhiteListProvider getInstance(Context context) {
        if (provider == null) {
           provider = new WhiteListProvider(context);
        }
        return provider;
    }

    public void save(Set<String> whiteList) {
        StringBuilder sb = new StringBuilder();
        for (String str : whiteList) {
            sb.append(str).append(';');
        }
        preferences.edit().putString(KEY_WHITE_LIST, sb.toString()).commit();
    }

    public Set<String> getWhiteList() {
        String str = preferences.getString(KEY_WHITE_LIST, "");
        String[] tmp = str.split(";");
        Set<String> set = new HashSet<String>();
        for (String s : tmp) {
            if (!s.isEmpty()) {
                set.add(s);
            }
        }
        return set;
    }
}
