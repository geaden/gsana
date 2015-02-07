package com.geaden.android.gsana.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Calendar;

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

    /**
     * Gets time of the day
     * @return {String} time of the day
     */
    public static String getTimeOfTheDay() {
        // Greeting text
        String[] timesOfDay = {"Morning", "Day", "Evening", "Night"};

        // Correspondent greeting index
        final int MORNING = 0;
        final int DAY = 1;
        final int EVENING = 2;
        final int NIGHT = 3;

        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        // Greeting template string
        String timeOfDay = "";
        Log.d(Utility.class.getSimpleName(), "Hour: " + hour);
        if (hour >= 5 && hour < 12) {
            timeOfDay = timesOfDay[MORNING];
        } else if (hour >= 12 && hour < 16) {
            timeOfDay = timesOfDay[DAY];
        } else if (hour >= 16 && hour < 21) {
            timeOfDay = timesOfDay[EVENING];
        } else {
            timeOfDay = timesOfDay[NIGHT];
        }
        Log.d(Utility.class.getSimpleName(), "Time of day: " + timeOfDay);
        return timeOfDay;
    }
}
