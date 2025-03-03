package com.example.appmonitor.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SharedPreferencesManager {
    private static final String SP_FILE = "SP_FILE";
    private static volatile SharedPreferencesManager instance;
    private final SharedPreferences sharedPref;

    private SharedPreferencesManager(Context context) {
        sharedPref = context.getSharedPreferences(SP_FILE, Context.MODE_PRIVATE);
    }

    public static SharedPreferencesManager init(Context context) {
        if (instance == null) {
            synchronized (SharedPreferencesManager.class) {
                if (instance == null) {
                    instance = new SharedPreferencesManager(context);
                }
            }
        }
        return instance;
    }

    public static SharedPreferencesManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SharedPreferencesManager must be initialized by calling init(context) before use.");
        }
        return instance;
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key, String defaultValue) {
        return sharedPref.getString(key, defaultValue);
    }

    public void deleteString(String key) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(key);
        editor.apply();
    }

    public void putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void editString(String key, String newValue) {
        if (sharedPref.contains(key)) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(key, newValue);
            editor.apply();
        } else {
            throw new IllegalArgumentException("Key '" + key + "' does not exist in SharedPreferences.");
        }
    }

    public String getValue(String key) {
        if (sharedPref.contains(key)) {
            return String.valueOf(sharedPref.getAll().get(key));
        } else {
            throw new IllegalArgumentException("Key '" + key + "' does not exist in SharedPreferences.");
        }
    }

    public boolean getBooleanOrLog(String key, boolean defaultValue) {
        if (sharedPref.contains(key)) {
            return sharedPref.getBoolean(key, defaultValue);
        } else {
            Log.d("SharedPreferencesManager", "Key not found");
            return defaultValue;
        }
    }
}
