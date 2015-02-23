package com.geaden.android.gsana.app.api.toggl;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.geaden.android.gsana.app.R;
import com.geaden.android.gsana.app.Utility;
import com.geaden.android.gsana.app.api.HttpHelper;


import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Api Bridge for Toggl.
 */
public class GTogglApiBridge {
    public static final String TOGGL_API_URI = "https://www.toggl.com";
    public static final String TOGGL_API_VERSION = "v8";

    private final Context mContext;
    private String LOG_TAG = getClass().getSimpleName();
    private String mTogglApiKey;

    private GTogglApiBridge(Context context) {
        mContext = context;
    }

    public static GTogglApiBridge getInstance(Context context) {
        GTogglApiBridge apiBridge = new GTogglApiBridge(context);
        return apiBridge;
    }


    /**
     * Gets toggle base uri
     * @return
     */
    public static Uri baseApiUri() {
        Uri.Builder builder = Uri.parse(TOGGL_API_URI).buildUpon();
        Uri baseApiUrl = builder
                .appendPath("api")
                .appendPath(TOGGL_API_VERSION).build();
        return baseApiUrl;
    }

    /**
     * Gets encoded string for basic authentication
     * @param apiKey api key, as authorization over api token is supported only
     * @return encoded auth string
     */
    private String getBasicAuthString(String apiKey) {
        String creds = String.format("%s:%s", apiKey, "api_token");
        try {
            return Base64.encodeToString(creds.getBytes("UTF-8"), Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG, "getBasicAuthStringError", e);
        }
        return null;
    }

    /**
     * Basic request to Toggl API
     * @param httpMethod request method
     * @param path request path
     * @param callback callback to handle result or error
     */
    // TODO: make it generic
    public void request(String httpMethod, String path, String params, GTogglCallback<GTogglResponse> callback) {
        mTogglApiKey = Utility.getPreference(mContext, mContext.getResources().getString(R.string.pref_toggl_api_key));
        Log.i(LOG_TAG, "Toggle API Key: " + mTogglApiKey);
        if (mTogglApiKey == null || mTogglApiKey.length() < 32) {
            callback.onError(new Throwable("Toggl is not connected or api key is not correct"));
            return;
        }
        httpMethod = httpMethod.toUpperCase();
        Log.d(LOG_TAG, String.format("Client API request %s %s", httpMethod, path));
        String responseData = null;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            Log.i(LOG_TAG, "URL: " + baseApiUri().toString() + path);
            URL url = new URL(baseApiUri().toString() + path);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(httpMethod);
            urlConnection.setRequestProperty("Client", "Geaden Toggle Client. Version: 0.1-b");
            String encodedAuthString = getBasicAuthString(mTogglApiKey);
            if (encodedAuthString == null) return;
            Log.d(LOG_TAG, String.format("Basic %s", encodedAuthString));
            urlConnection.setRequestProperty("Authorization", String.format("Basic %s", encodedAuthString));
            if (null != params && (httpMethod.equals(HttpHelper.Method.POST)
                    || httpMethod.equals(HttpHelper.Method.PUT))) {
                Log.i(LOG_TAG, "Params: " + params);
                urlConnection.setRequestProperty("Content-Type",
                        "application/json");
                urlConnection.setRequestProperty("Content-Length", "" +
                        Integer.toString(params.getBytes().length));
                urlConnection.setRequestProperty("Content-Language", "en-US");
                urlConnection.setUseCaches(false);
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                //Send request
                DataOutputStream wr = new DataOutputStream(
                        urlConnection.getOutputStream());
                wr.writeBytes(params);
                wr.flush();
                wr.close();
            }
            urlConnection.connect();
            int serverCode = urlConnection.getResponseCode();
            // successful query
            if (serverCode == HttpHelper.ResponseCode.OK) {
                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    Log.d(LOG_TAG, "No data...");
                    return;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return;
                }
                responseData = buffer.toString();
                Log.v(LOG_TAG, "Response: " + responseData);
            } else if (serverCode == HttpHelper.ResponseCode.UNAUTHORIZED) {
                callback.onError(new Throwable("Unauthorized"));
            } else {
                Log.e(LOG_TAG, "Server returned the following error code: " + serverCode, null);
                callback.onError(new Exception("Server returned the following error code: " + serverCode));
            }
            callback.onResult(new GTogglResponse(responseData));
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the data, there's no point in attempting
            // to parse it.
            callback.onError(e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                    callback.onError(e);
                }
            }
        }
    }


    /**
     * Toggl response callback
     * @param <T>
     */
    public static interface GTogglCallback<T> {
        public void onResult(T value);
        public void onError(Throwable exception);
    }


    /**
     * Toggl response
     */
    public static class GTogglResponse {
        private final String DATA = "data";
        private final String LOG_TAG = getClass().getSimpleName();
        private Object data;

        public GTogglResponse(String result) {
            Object data = JSONValue.parse(result);
            if (data instanceof JSONObject) {
                this.data = ((JSONObject) data).get(DATA);
            } else {
                this.data = data;
            }
        }

        public Object getData() {
            return data;
        }
    }
}
