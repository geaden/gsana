package com.geaden.android.gsana.app.api;

import org.json.JSONObject;

/**
 * Callback for request to the Asana API.
 */
public interface AsanaCallback {
    public void onResult(JSONObject data);
    public void onError();
}
