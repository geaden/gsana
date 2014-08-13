package com.geaden.android.app.gsana;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import org.apache.commons.codec.binary.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.api.client.auth.oauth.OAuthAuthorizeTemporaryTokenUrl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Gsana main activity class
 */
public class MainActivity extends ActionBarActivity {
    private final String LOG_TAG = getClass().getSimpleName();
    static private final String CLIENT_ID = "15434230851041";
    private final String CLIENT_SECRET = "881d9feaa4ef31bb4ecde1c924def191";
    public static String OAUTH_URL = "https://app.asana.com/-/oauth_authorize";
    public static String OAUTH_ACCESS_TOKEN_URL = "https://app.asana.com/-/oauth_token";

    public static String CALLBACK_URL = "http://localhost";

    private final String API_KEY = "211sQUKy.yLnpQsl7IrRD90vp3UF6BTV";
    private final String WORKSPACE_ID = "498346170860";
    private final String ASSIGNEE = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Get the intent that started this activity
        Intent intent = getIntent();
        Uri data = intent.getData();
        // Figure out what to do based on the intent type
        Log.d(LOG_TAG, "Data: " + intent.getData());
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void doAuth() {
        String AUTH_URL = "https://app.asana.com/-/oauth_authorize";
        String QUERY_CLIENT_ID_PARAM = "client_id";
        String QUERY_REDIRECT_URI = "redirect_uri";
        String QUERY_RESPONSE_TYPE_PARAM = "response_type";
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        private final String LOG_TAG = getClass().getSimpleName();

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            ImageButton asana = (ImageButton) rootView.findViewById(R.id.imageButton);
            asana.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri asanUri = Uri.parse(OAUTH_URL).buildUpon()
                            .appendQueryParameter("client_id", CLIENT_ID)
                            .appendQueryParameter("redirect_uri", "http://6ccbd0cf.ngrok.com/oauth_callback")
                            .appendQueryParameter("response_type", "token").build();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(asanUri);
                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        Log.d(LOG_TAG, "Couldn't call " + asanUri + ", no receiving apps installed!");
                    }
//                    Intent intent = new Intent(getActivity(), OAuthActivity.class);
//                    startActivity(intent);
                }
            });
            return rootView;
        }
    }

    /**
     * Fetches tasks from Asana
     */
    private static class FetchTask extends AsyncTask<Void, Void, Void> {
        private final String LOG_TAG = getClass().getSimpleName();

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

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
