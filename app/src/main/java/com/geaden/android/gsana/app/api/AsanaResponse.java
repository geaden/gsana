package com.geaden.android.gsana.app.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Asana response.
 */
public class AsanaResponse {
    private final String KEY_DATA = "data";

    private Object data;

    public static class MalformedResponseException extends Exception {
        public MalformedResponseException(String message) {
            super(message);
        }
    }

    public AsanaResponse(Object data) throws MalformedResponseException {
        if (data instanceof String) {
            try {
                data = new JSONObject((String) data);
            } catch (JSONException e) {
                throw new MalformedResponseException(e.getMessage());
            }
        }
        if (((JSONObject) data).has(KEY_DATA)) {
            try {
                this.data = ((JSONObject) data).get(KEY_DATA);
            } catch (JSONException e) {
                throw new MalformedResponseException(e.getMessage());
            }
        } else {
            throw new MalformedResponseException("Response malformed");
        }
    }

    public Object getData() {
        return data;
    }

    public String toString() {
        return this.data.toString();
    }

    public String toString(int indent) {
        try {
            if (this.data instanceof JSONObject) {
                return ((JSONObject) this.data).toString(indent);
            }
            if (this.data instanceof JSONArray) {
                return ((JSONArray) this.data).toString(indent);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this.toString();
    }
}

