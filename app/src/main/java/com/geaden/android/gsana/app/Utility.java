package com.geaden.android.gsana.app;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Common utils.
 */
public class Utility {
    public static final String ACCESS_TOKEN_KEY = "access_token";
    public static final String REFRESH_TOKEN_KEY = "refresh_token";

    public static String getSettingsStringValue(Context context, String key) {
        String value = context.getSharedPreferences(Constants.SHARED_PREF_KEY, Context.MODE_PRIVATE)
                .getString(key, null);
        return value;
    }

    /**
     * Gets access token from preferences
     * @param context context to get preferences for
     * @return access token for Asana API
     */
    public static String getAccessToken(Context context) {
        return getSettingsStringValue(context, ACCESS_TOKEN_KEY);
    }

    /**
     * Gets refresh token from preferences
     * @param context context to get preferences for
     * @return refreshed access token for Asana API
     */
    public static String getRefreshToken(Context context) {
        return getSettingsStringValue(context, REFRESH_TOKEN_KEY);
    }

    /**
     * Puts values into settings
     *
     * @param context application context
     * @param key settings key
     * @param value settings value
     */
    public static void putSettingsStringValue(Context context, String key, String value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(Constants.SHARED_PREF_KEY,
                Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * Puts access token to shared settings
     *
     * @param context application context
     * @param accessToken access token value
     */
    public static void putAccessToken(Context context, String accessToken) {
        putSettingsStringValue(context, ACCESS_TOKEN_KEY, accessToken);
    }

    /**
     * Puts refresh token to use for refreshing access token
     * @param context the application context
     * @param refreshToken the refresh token
     */
    public static void putRefreshToken(Context context, String refreshToken) {
        if (refreshToken != null) {
            putSettingsStringValue(context, REFRESH_TOKEN_KEY, refreshToken);
        }
    }

    /**
     * Invalidates access token
     * @param context the activity to invalidate token from
     */
    public static void invalidateAccessToken(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(Constants.SHARED_PREF_KEY,
                Context.MODE_PRIVATE).edit();
        editor.remove(ACCESS_TOKEN_KEY);
        editor.commit();
    }
}
