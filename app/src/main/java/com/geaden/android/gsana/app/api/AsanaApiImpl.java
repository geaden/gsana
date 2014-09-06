package com.geaden.android.gsana.app.api;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Asana Api Implementation
 */
public class AsanaApiImpl implements AsanaApi {
    private final String LOG_TAG = getClass().getSimpleName();

    /** Methods **/
    final private String GET = "GET";
    final private String POST = "POST";

    private String accessToken;

    /** Asan Api **/
    public static final String ASANA_BASE_URL = "https://app.asana.com/api/1.0/";
    public static final String TASKS_API = "tasks";


    private final String WORKSPACE_ID = "498346170860";

    public AsanaApiImpl(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject getUserInfo() {
        final String USER_API = "users/me";
        Uri buildUri = Uri.parse(ASANA_BASE_URL).buildUpon()
                .appendPath(USER_API).build();
        JSONObject userInfo = null;
        try {
            URL url = new URL(buildUri.toString());
            String response = asanaCall(url, GET);
            Log.v(LOG_TAG, "User info: " + response);
            userInfo = new JSONObject(response);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error retrieving user info", e);
        }
        return userInfo;
    }

    /**
     * Calls Asana API
     * @param url API endpoint
     * @param method method name to perform request
     * @return API call result as a string
     */
    private String asanaCall(URL url, String method) {
        String responseData = null;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            // Create the request to Asana, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(method);
            Log.v(LOG_TAG, String.format("Bearer %s", accessToken));
            urlConnection.setRequestProperty("Authorization", String.format("Bearer %s", accessToken));
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
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
                return null;
            }
            responseData = buffer.toString();
            Log.v(LOG_TAG, "Response: " + responseData);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return responseData;
    }

    @Override
    public String getTasks() {
        // Will contain the raw JSON response as a string.
        String tasksJsonStr = null;
        // Construct the URL for the Asana API query
        final String WORKSPACE_QUERY_PARAM = "workspace";
        final String ASSIGNEE_QUERY_PARAM = "assignee";

        final String ASSIGNEE = "me";
        final String WORKSPACE = WORKSPACE_ID;

        Uri builtUri = Uri.parse(ASANA_BASE_URL).buildUpon()
                .appendPath(TASKS_API)
                .appendQueryParameter(WORKSPACE_QUERY_PARAM, WORKSPACE)
                .appendQueryParameter(ASSIGNEE_QUERY_PARAM, ASSIGNEE)
                .build();

        try {
            URL url = new URL(builtUri.toString());
            tasksJsonStr = asanaCall(url, GET);
            Log.v(LOG_TAG, "Tasks: " + tasksJsonStr);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error retrieving tasks", e);
        }

        return tasksJsonStr;
    }

    @Override
    public JSONArray getWorkspaces() {
        return null;
    }

    @Override
    public JSONArray getProjects(String workspaceId) {
        return null;
    }
}

