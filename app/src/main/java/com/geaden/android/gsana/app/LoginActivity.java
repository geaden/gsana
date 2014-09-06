package com.geaden.android.gsana.app;

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
import android.widget.Toast;

import io.oauth.OAuth;
import io.oauth.OAuthCallback;
import io.oauth.OAuthData;


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

    private static final String OAUTH_GSANA_PUBLIC_KEY = "_3P90BSqgLGbIIXYz5olOtGUMCE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth);
        Intent intent = getIntent();
        Uri data = intent.getData();
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
    public class LoginFragment extends Fragment implements OAuthCallback {
        private final String LOG_TAG = getClass().getSimpleName();

        public LoginFragment() { }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_oauth, container, false);
            final OAuth oAuth = new OAuth(getActivity());
            oAuth.initialize(OAUTH_GSANA_PUBLIC_KEY);
            ImageButton asanaLogin = (ImageButton) rootView.findViewById(R.id.imageButton);
            asanaLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.v(LOG_TAG, "Authenticating...");
                    oAuth.popup("asana", LoginFragment.this);
                }
            });
            return rootView;
        }

        @Override
        public void onFinished(OAuthData data) {
            Log.v(LOG_TAG, "On finished...");
            if (!data.status.equals("success")) {
                Log.e(LOG_TAG, data.error);
                Toast.makeText(getActivity(), data.error, Toast.LENGTH_SHORT);
                return;
            }
            // OAuth flow
            Log.d(LOG_TAG, "Access token: " + data.token);
            // Data not null get parameters
            String accessToken = data.token;
            Log.d(LOG_TAG, "Access token from LoginActivity is " + accessToken);
            // store auth token in default SharedPreferences
            Log.v(LOG_TAG, "Saving access token...");
            Editor e = getSharedPreferences(
                    Constants.SHARED_PREF_KEY, Context.MODE_PRIVATE).edit();
            e.putString(MainActivity.ACCESS_TOKEN_KEY, accessToken);
            e.commit();
            // Start Main activity
            Intent mainIntent = new Intent(getActivity(), MainActivity.class);
            startActivity(mainIntent);
            return;
        }
    }
}
