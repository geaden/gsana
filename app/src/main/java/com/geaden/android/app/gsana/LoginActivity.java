package com.geaden.android.app.gsana;

import android.content.Context;
import android.content.SharedPreferences;
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
 * Created by geaden on 14/08/14.
 */
public class LoginActivity extends ActionBarActivity {
    private final String LOG_TAG = getClass().getSimpleName();

    static private final String CLIENT_ID = "15434230851041";
    private final String CLIENT_SECRET = "881d9feaa4ef31bb4ecde1c924def191";
    public static String OAUTH_URL = "https://app.asana.com/-/oauth_authorize";
    public static String OAUTH_ACCESS_TOKEN_URL = "https://app.asana.com/-/oauth_token";

    public static String CALLBACK_URL = "http://gsana-android.appspot.com/oauth_callback";

    private final String API_KEY = "211sQUKy.yLnpQsl7IrRD90vp3UF6BTV";
    private final String WORKSPACE_ID = "498346170860";
    private final String ASSIGNEE = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.oauth_container, new PlaceholderFragment())
                    .commit();
        }
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
            SharedPreferences prefs = getActivity().getSharedPreferences(
                    "com.geaden.android.app.gsana", Context.MODE_PRIVATE);
            String token = prefs.getString(MainActivity.ACCESS_TOKEN_KEY, null);
            Log.d(LOG_TAG, "Token from prefs: " + token);
            if (token != null) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.putExtra(MainActivity.ACCESS_TOKEN_KEY, token);
                startActivity(intent);
                return null;
            }
            View rootView = inflater.inflate(R.layout.fragment_oauth, container, false);
            ImageButton asana = (ImageButton) rootView.findViewById(R.id.imageButton);
            asana.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri asanUri = Uri.parse(OAUTH_URL).buildUpon()
                            .appendQueryParameter("client_id", CLIENT_ID)
                            .appendQueryParameter("redirect_uri", CALLBACK_URL)
                            .appendQueryParameter("response_type", "token").build();
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
