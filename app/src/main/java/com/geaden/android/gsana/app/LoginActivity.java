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
 * Gsana login activity
 */
public class LoginActivity extends ActionBarActivity {
    private final String LOG_TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth);
        // Show login button
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.oauth_container, new LoginFragment())
                    .commit();
        }
    }
}
