package com.geaden.android.gsana.app.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Abstract model class
 */
public abstract class Model {
    private final String LOG_TAG = getClass().getSimpleName();

    /**
     * Gets String value from data as {@link org.json.JSONObject} by value
     * @param data json representation of data
     * @param key the key to get value for
     * @return value
     */
    public String getStringValue(JSONObject data, String key) {
        if (data == null) {
            return null;
        }
        if (data.has(key)) {
            try {
                return data.getString(key);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error to get value for " + key);
            }
        }
        return null;
    }

    /**
     * Gets Long value from data as {@link JSONObject} by value. If returning value is 0, than
     * it's not fully retrieved from API.
     * @param data json representation of data
     * @param key the key to get value for
     * @return value
     */
    public long getLongValue(JSONObject data, String key) {
        if (data == null) {
            return 0;
        }
        if (data.has(key)) {
            try {
                return data.getLong(key);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error to get value for " + key);
            }
        }
        return 0;
    }

    /**
     * Gets JSONObject value from data as {@link JSONObject} by value
     * @param data json representation of data
     * @param key the key to get value for
     * @return value
     */
    public JSONObject getJSONObject(JSONObject data, String key) {
        if (data == null) {
            return null;
        }
        if (data.has(key)) {
            try {
                return data.getJSONObject(key);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error to get value for " + key);
            }
        }
        return null;
    }

    /**
     * Gets JSONArray value from data as {@link JSONObject} by value
     * @param data json representation of data
     * @param key the key to get value for
     * @return value as JSONArray
     */
    public JSONArray getJSONArray(JSONObject data, String key) {
        if (data == null) {
            return null;
        }
        if (data.has(key)) {
            try {
                return data.getJSONArray(key);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error to get value for " + key);
            }
        }
        return null;
    }

    /**
     * String representation of the model
     * @return String
     */
    abstract public String toString();

    /**
     * JSON representation of the model
     * @return JSONObject
     */
    abstract public JSONObject toJSONObject();

    abstract public void setId(long id);

    abstract public void setName(String name);
}
