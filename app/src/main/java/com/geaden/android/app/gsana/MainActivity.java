package com.geaden.android.app.gsana;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;



/**
 * Gsana main activity class
 */
public class MainActivity extends ActionBarActivity {
    private final String LOG_TAG = getClass().getSimpleName();

    private String mAccessToken;

    public static final String ACCESS_TOKEN_KEY = "accessToken";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // check whether access token is already saved
        mAccessToken = getSharedPreferences(Constants.SHARED_PREF_KEY, Context.MODE_PRIVATE)
                .getString(ACCESS_TOKEN_KEY, null);
        Log.d(LOG_TAG, "Access Token: " + mAccessToken);
        if (mAccessToken == null) {
            // No token found
            // Initialize login activity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return;
        } else {
            // Fetch tasks
            if (savedInstanceState == null) {
                TasksFragment tasksFragment = TasksFragment.newInstance(mAccessToken);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, tasksFragment)
                        .commit();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_logout) {
            // Remove access token. If logout chosen.
            Editor editor = getSharedPreferences(Constants.SHARED_PREF_KEY,
                    Context.MODE_PRIVATE).edit();
            editor.remove(ACCESS_TOKEN_KEY);
            editor.commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
