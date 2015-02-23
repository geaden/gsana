package com.geaden.android.gsana.app.api;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.geaden.android.gsana.app.Utility;
import com.geaden.android.gsana.app.oauth.AsanaOAuthClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Class to organize communication with the Asana API.
 */
public class AsanaApiBridge {
    private static final String LOG_TAG = AsanaApiBridge.class.getSimpleName();

    private Context mContext;
    private AsanaOAuthClient mAsanaOAuthClient;

    // HTTP Methods
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";

    // HTTP Responses
    public static final int OK = 200;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int ACCESS_DENIED = 403;
    public static final int METHOD_NOT_ALLOWED = 405;

    private static AsanaApiBridge instance = null;

    private AsanaApiBridge(Context context) {
        mContext = context;
        mAsanaOAuthClient = AsanaOAuthClient.getInstance();
    };

    /**
     * Singleton of Asana API Bridge
     * @param context application context
     * @return {@link com.geaden.android.gsana.app.api.AsanaApiBridge} instance
     */
    public static AsanaApiBridge getInstance(Context context) {
        if (instance == null) {
            instance = new AsanaApiBridge(context);
        }
        return instance;
    }

    /**
     * Gets base API url
     * @return {@link String} The base URL to use for API requests
     */
    public static String baseApiUrl() {
        Uri.Builder builder = Uri.parse("https://" + AsanaOptions.ASANA_HOST
                + ":" + AsanaOptions.ASANA_PORT).buildUpon();
        String baseApiUrl = builder
                .appendPath("api")
                .appendPath(AsanaOptions.API_VERSION).build().toString();
        return baseApiUrl;
    }

    /**
     * Make request to the Asana API
     *
     * @param httpMethod HTTP request method to use (e.g. "POST")
     * @param path Path to call.
     */
    public void request(String httpMethod, String path, String params,
                        AsanaCallback<AsanaResponse> callback) {
        String accessToken = Utility.getAccessToken(mContext);
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
            urlConnection.setRequestProperty("Client", "Geaden Asana Android Client. Version: 0.1-b");
            Log.d(LOG_TAG, String.format("Bearer %s", accessToken));
            urlConnection.setRequestProperty("Authorization", String.format("Bearer %s", accessToken));
            if (params != null && (httpMethod.equals(HttpHelper.Method.POST)
                    || httpMethod.equals(HttpHelper.Method.PUT))) {
                urlConnection.setRequestProperty("Content-Type",
                        "application/json");
                urlConnection.setRequestProperty("Content-Length", "" +
                        Integer.toString(params.getBytes().length));
                urlConnection.setRequestProperty("Content-Language", "en-US");
                urlConnection.setUseCaches (false);
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                //Send request
                DataOutputStream wr = new DataOutputStream (
                        urlConnection.getOutputStream ());
                wr.writeBytes (params);
                wr.flush ();
                wr.close ();
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
                Log.d(LOG_TAG, "serverCode 401");
                Log.d(LOG_TAG, "Refreshing token and retrying...");
                String refreshToken = Utility.getRefreshToken(mContext);
                AsanaOAuthClient.AsanaTokenResponse tokenResponse = mAsanaOAuthClient.refreshToken(refreshToken);
                // Update values in shared preferences
                Utility.putSettingsStringValue(mContext, Utility.ACCESS_TOKEN_KEY, tokenResponse.getAccessToken());
                Utility.putSettingsStringValue(mContext, Utility.REFRESH_TOKEN_KEY, tokenResponse.getRefreshToken());
                Utility.putSettingsStringValue(mContext, Utility.CURRENT_USER_KEY, String.valueOf(tokenResponse.getUser().getId()));
                request(httpMethod, path, params, callback);
                return;
            } else {
                Log.e(LOG_TAG, "Server returned the following error code: " + serverCode, null);
                callback.onError(new Exception("Server returned the following error code: " + serverCode));
            }
            try {
                callback.onResult(new AsanaResponse(responseData));
            } catch (AsanaResponse.MalformedResponseException e) {
                e.printStackTrace();
                callback.onError(e);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the data, there's no point in attemping
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

}

