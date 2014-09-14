package com.geaden.android.gsana.app;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;


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
