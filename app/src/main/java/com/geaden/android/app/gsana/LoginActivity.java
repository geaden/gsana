package com.geaden.android.app.gsana;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


/**
 * Asana login activity
 */
public class LoginActivity extends ActionBarActivity {
    private final String LOG_TAG = getClass().getSimpleName();


    private final String CLIENT_SECRET = "881d9feaa4ef31bb4ecde1c924def191";

    public static final String OAUTH_ACCESS_TOKEN_URL = "https://app.asana.com/-/oauth_token";

    public static final String ACCESS_TOKEN = "access_token";

    /** Asana OAuth connect data */
    public static final String OAUTH_URL = "https://app.asana.com/-/oauth_authorize";
    static private final String CLIENT_ID = "15434230851041";
    public static final String RESPONSE_TYPE = "token";
    public static final String CALLBACK_URL = "http://gsana-android.appspot.com/oauth_callback";

    private final String API_KEY = "211sQUKy.yLnpQsl7IrRD90vp3UF6BTV";
    private final String WORKSPACE_ID = "498346170860";
    private final String ASSIGNEE = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth);
        Intent intent = getIntent();
        Uri data = intent.getData();
        // OAuth flow
        if (data != null) {
            Log.d(LOG_TAG, "Data: " + data);
            // Data not null get parameters
            String accessToken = data.getQueryParameter(ACCESS_TOKEN);
            Log.d(LOG_TAG, "Access tokent from LoginActivity is " + accessToken);
            // store auth token in default SharedPreferences
            Log.v(LOG_TAG, "Saving access token...");
            Editor e = getSharedPreferences(Constants.SHARED_PREF_KEY, Context.MODE_PRIVATE).edit();
            e.putString(MainActivity.ACCESS_TOKEN_KEY, accessToken);
            e.commit();
            // Start Main activity
            Intent mainIntent = new Intent(this, MainActivity.class);
            startActivity(mainIntent);
            return;
        }
        // Show login button
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.oauth_container, new LoginFragment())
                    .commit();
        }
    }

    /**
    * A placeholder fragment containing a simple view.
    */
    public static class LoginFragment extends Fragment {
        private final String LOG_TAG = getClass().getSimpleName();

        public LoginFragment() { }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_oauth, container, false);
            ImageButton asanaLogin = (ImageButton) rootView.findViewById(R.id.imageButton);
            asanaLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri asanUri = Uri.parse(OAUTH_URL).buildUpon()
                            .appendQueryParameter("client_id", CLIENT_ID)
                            .appendQueryParameter("redirect_uri", CALLBACK_URL)
                            .appendQueryParameter("response_type", RESPONSE_TYPE).build();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(asanUri);
                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        Log.d(LOG_TAG, "Couldn't call " + asanUri + ", no receiving apps installed!");
                    }
                }
            });
            return rootView;
        }
    }
}
