package com.geaden.android.app.gsana;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by geaden on 14/08/14.
 */
public class LoginActivity extends Activity {
    private final String LOG_TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        Uri data = intent.getData();
        Log.d(LOG_TAG, "Data: " + data);
        if (data != null) {
            String token = data.getQueryParameter("access_token");
            FetchTask task = new FetchTask();
            task.execute(token);
        }
    }

    /**
     * Fetches tasks from Asana
     */
    private static class FetchTask extends AsyncTask<String, Void, Void> {
        private final String LOG_TAG = getClass().getSimpleName();

        @Override
        protected Void doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String accessToken = params[0];

            Log.d(LOG_TAG, "Access token: " + accessToken);

            // Will contain the raw JSON response as a string.
            String tasksJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String ASANA_BASE_URL = "https://app.asana.com/api/1.0/";
                final String TASKS_API = "tasks";
                final String WORKSPACE_QUERY_PARAM = "workspace";
                final String ASSIGNEE_QUERY_PARAM = "assignee";

                final String ASSIGNEE = "me";
                final String WORKSPACE = "498346170860";

                Uri builtUri = Uri.parse(ASANA_BASE_URL).buildUpon()
                        .appendPath(TASKS_API)
                        .appendQueryParameter(WORKSPACE_QUERY_PARAM, WORKSPACE)
                        .appendQueryParameter(ASSIGNEE_QUERY_PARAM, ASSIGNEE)
                        .build();

                Log.v(LOG_TAG, "URL: " + builtUri.toString());

                URL url = new URL(builtUri.toString());


                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                Log.v(LOG_TAG, String.format("Bearer %s", accessToken));
                urlConnection.setRequestProperty("Authorization", String.format("Bearer %s", accessToken));
//                byte[] authBytes = Base64.encodeBase64((API_KEY + ":").getBytes());
//                String authString = new String(authBytes);
//                Log.v(LOG_TAG, "Base auth: " + authString);
//                urlConnection.setRequestProperty("Authorization", "Basic "+ authString);
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
                tasksJsonStr = buffer.toString();
                Log.v(LOG_TAG, "Tasks: " + tasksJsonStr);
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
            return null;
        }
    }
}
