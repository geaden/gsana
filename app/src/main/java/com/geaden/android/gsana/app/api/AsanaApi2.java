package com.geaden.android.gsana.app.api;


import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.geaden.android.gsana.app.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Asana API 2 singleton realization.
 */
public class AsanaApi2 {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private String mAccessToken;

    public String baseApiUrl() {
        Uri.Builder builder = Uri.parse("https://" + Options.ASANA_HOST + ":" + Options.ASANA_PORT).buildUpon();
        String baseApiUrl = builder
                .appendPath("api")
                .appendPath(Options.API_VERSION).build().toString();
        return baseApiUrl;
    }

    /**
     * Make request to the Asana API
     *
     * @param httpMethod HTTP request method to use (e.g. "POST")
     * @param path Path to call.
     * @return API call result as a string
     */
    public void request(String httpMethod, String path,
                          AsanaCallback callback) {
        httpMethod = httpMethod.toUpperCase();
        Log.d(LOG_TAG, String.format("Client API request %s %s", httpMethod, path));

        String responseData = null;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            Log.d(LOG_TAG, "Url: " + baseApiUrl() + path);
            URL url = new URL(baseApiUrl() + path);
            // Create the request to the Asana API, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(httpMethod);

            if (httpMethod == "PUT" || httpMethod == "POST") {
                // TODO: Be polite with Asana Api
            }

            Log.d(LOG_TAG, String.format("Bearer %s", mAccessToken));
            urlConnection.setRequestProperty("Authorization", String.format("Bearer %s", mAccessToken));
            urlConnection.connect();
            int serverCode = urlConnection.getResponseCode();
            // successful query
            if (serverCode == 200) {
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
            } else if (serverCode == 401) {
                Log.d(LOG_TAG, "serverCode 401");
                callback.onError();
            } else {
                Log.e(LOG_TAG, "Server returned the following error code: " + serverCode, null);
                callback.onError();
            }
            try {
                callback.onResult(new JSONObject(responseData));
            } catch (JSONException e) {
                e.printStackTrace();
                callback.onError();
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the data, there's no point in attemping
            // to parse it.
            callback.onError();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                    callback.onError();
                }
            }
        }
    }

    private static AsanaApi2 instance = null;

    /**
     * Singlton of Asana API implementation
     *
     * @param context
     * @param acesssToken
     */
    protected AsanaApi2(Context context, String acesssToken) { 
        mAccessToken = acesssToken;
    }

    /**
     * Singleton implementation for Asana API realization
     *
     * @param context the context of application
     * @param accessToken access token
     * @return Asana API instance
     */
    public static AsanaApi2 getInstance(Context context, String accessToken) {
        if (instance == null) {
            instance = new AsanaApi2(context, accessToken);
        }
        return instance;
    }

    /**
     * Requests the user record for the logged-in user
     */
    public void me(AsanaCallback callback) {
        Log.i(LOG_TAG, "Requesting user info");
        request("GET", "/users/me", callback);
    }

    public void tasks(String workspaceId, AsanaCallback callback) {
        Log.i(LOG_TAG, "Requesting tasks");
        request("GET", "/tasks?workspace=" + workspaceId + "&assignee=me", callback);

    }

    public void workspaces(AsanaCallback callback) {
        // retrieve workspaces
        Log.i(LOG_TAG, "Requesting workspaces");
        request("GET", "/workspaces", callback);
    }

    public void projects(AsanaCallback callback) {
        // retrive projects
        Log.i(LOG_TAG, "Requesting projects");
        request("GET", "/projects", callback);
    }
}
