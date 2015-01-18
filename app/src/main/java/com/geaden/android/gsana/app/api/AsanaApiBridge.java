package com.geaden.android.gsana.app.api;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.geaden.android.gsana.app.LoginActivity;
import com.geaden.android.gsana.app.Utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Class to organize communication with the Asana API.
 */
public class AsanaApiBridge {
    private final String LOG_TAG = this.getClass().getSimpleName();

    // Version of the Asana API to use
    public static String API_VEISION = "1.0";

    public static String ASANA_HOST = "app.asana.com";

    public static int ASANA_PORT = 443;

    private final int TOKEN_IDX = 0;

    public String baseApiUrl(String asanaHost, int asanaPort) {
        Uri.Builder builder = new Uri.Builder();
        String baseApiUrl = builder.scheme("https://").path(asanaHost + ":" + asanaPort)
                .appendPath("/api/").appendPath(API_VEISION).build().toString();
        return baseApiUrl;
    }

    /**
     * Make request to the Asana API
     *
     * @param httpMethod HTTP request method to use (e.g. "POST")
     * @param path Path to call.
     * @param params Parameters for API method; depends on method.
     * @param options optional params
     * @return API call result as a string
     */
    public String request(String httpMethod, String path, String[] params, String[] options) {
        httpMethod = httpMethod.toUpperCase();
        Log.d(LOG_TAG, String.format("Client API request %s %s %s", httpMethod, path, params));

        String responseData = null;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(baseApiUrl(ASANA_HOST, ASANA_PORT) + path);
            // Create the request to the Asana API, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(httpMethod);
            Log.v(LOG_TAG, String.format("Bearer %s", params[TOKEN_IDX]));

            urlConnection.setRequestProperty("Authorization", String.format("Bearer %s", params[TOKEN_IDX]));
            urlConnection.connect();

            int serverCode = urlConnection.getResponseCode();
            // successful query
            if (serverCode == 200) {
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
            } else if (serverCode == 401) {
                return null;
            } else {
                Log.e(LOG_TAG, "Server returned the following error code: " + serverCode, null);
                return null;
            }
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

}

