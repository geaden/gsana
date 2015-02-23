package com.geaden.android.gsana.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Common utils.
 */
public class Utility {
    public static final String ACCESS_TOKEN_KEY = "access_token";
    public static final String REFRESH_TOKEN_KEY = "refresh_token";
    public static final String DEFAULT_WORKSPACE_KEY = "workspace_id";
    public static final String CURRENT_USER_KEY = "user_id";

    public static String getSettingsStringValue(Context context, String key) {
        String value = context.getSharedPreferences(Constants.SHARED_PREF_KEY, Context.MODE_PRIVATE)
                .getString(key, null);
        return value;
    }

    /**
     * Gets stored preference
     * @param context activity context
     * @param key the key to get preference for
     * @return preference value
     */
    public static String getPreference(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(key, "");
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

    /**
     * Formats sqlDate (yyyy-MM-dd) to something like Feb 2, 2015
     * @param sqlDate the date from SQL
     * @return formatted date
     */
    public static String sqlStringDateFormat(String sqlDate) {
        String formattedDate = sqlDate;
        final String CURR_DATE_FORMAT = "yyyy-MM-dd";
        final String DATE_FORMAT = "MMM d, yyyy";
        SimpleDateFormat currSdf = new SimpleDateFormat(CURR_DATE_FORMAT);
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        try {
            Date date = currSdf.parse(sqlDate);
            formattedDate = sdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formattedDate;
    }
}
