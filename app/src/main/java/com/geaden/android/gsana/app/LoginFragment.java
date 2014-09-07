package com.geaden.android.gsana.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONException;

import io.oauth.OAuth;
import io.oauth.OAuthCallback;
import io.oauth.OAuthData;

/**
 * Created by geaden on 06/09/14.
 */
public class LoginFragment extends Fragment implements OAuthCallback {
    private final String LOG_TAG = getClass().getSimpleName();

    private static final String OAUTH_GSANA_PUBLIC_KEY = "_3P90BSqgLGbIIXYz5olOtGUMCE";

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
            Toast.makeText(getActivity(), data.error, Toast.LENGTH_LONG);
            return;
        }
        // OAuth flow
        try {
            Log.d(LOG_TAG, "Data request" + data.request.toString(4));
        } catch (JSONException e) {

        }
        Log.d(LOG_TAG, "Access token: " + data.token);
        // Data not null get parameters
        String accessToken = data.token;
        Log.d(LOG_TAG, "Access token from LoginActivity is " + accessToken);
        // store auth token in default SharedPreferences
        Log.v(LOG_TAG, "Saving access token...");
        SharedPreferences.Editor e = getActivity().getSharedPreferences(
                Constants.SHARED_PREF_KEY, Context.MODE_PRIVATE).edit();
        e.putString(MainActivity.ACCESS_TOKEN_KEY, accessToken);
        e.commit();
        // Start Main activity
        Intent mainIntent = new Intent(getActivity(), MainActivity.class);
        startActivity(mainIntent);
        return;
    }
}
