package com.geaden.android.gsana.app;

import android.content.Context;

/**
 * Common utils.
 */
public class Utils {
    public static final String ACCESS_TOKEN_KEY = "access_token";

    /**
     * Gets access token from preferences
     * @param context context to get preferences for
     * @return access token for Asana API
     */
    public static String getAccessToken(Context context) {
        String accessToken = context.getSharedPreferences(Constants.SHARED_PREF_KEY, Context.MODE_PRIVATE)
                .getString(ACCESS_TOKEN_KEY, null);
        return accessToken;
    }
}
