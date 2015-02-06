package com.geaden.android.gsana.app.api;

import org.json.JSONObject;

/**
 * Callback for request to the Asana API.
 */
public interface AsanaCallback<T> {
    /**
     * On success
     * @param value result of successful call
     */
    public void onResult(T value);

    /**
     * On error
     * @param exception thrown exception
     */
    public void onError(Throwable exception);
}
