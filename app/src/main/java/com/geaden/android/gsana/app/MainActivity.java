package com.geaden.android.gsana.app;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import com.geaden.android.gsana.app.api.AsanaApi2;
import com.geaden.android.gsana.app.fragments.MainDrawerFragment;
import com.geaden.android.gsana.app.fragments.TaskListFragment;
import com.geaden.android.gsana.app.models.AsanaUser;
import com.geaden.android.gsana.app.sync.GsanaSyncAdapter;


/**
 * Gsana main activity class
 */
public class MainActivity extends ActionBarActivity implements TaskListFragment.Callback {
    private final String LOG_TAG = getClass().getSimpleName();

    public static final String ACCESS_TOKEN_KEY = "access_token";

    public AsanaApi2 mAsanaApi;

    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private String mAccessToken;

    // Current user first name to display
    public static String sCurrentUser = "";

    private ArrayAdapter<String> mWorkspaceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTitle = mDrawerTitle = getTitle();
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
            mAsanaApi = AsanaApi2.getInstance(this);
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            mDrawerToggle = new ActionBarDrawerToggle(
                    this,                  /* host Activity */
                    mDrawerLayout,         /* DrawerLayout object */
                    R.string.drawer_open,  /* "open drawer" description */
                    R.string.drawer_close  /* "close drawer" description */
            ) {

                /** Called when a drawer has settled in a completely closed state. */
                public void onDrawerClosed(View view) {
                    super.onDrawerClosed(view);
                    getSupportActionBar().setTitle(mTitle);
                    invalidateOptionsMenu();
                }

                /** Called when a drawer has settled in a completely open state. */
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    getSupportActionBar().setTitle(mDrawerTitle);
                    invalidateOptionsMenu();
                }
            };

            // Set the drawer toggle as the DrawerListener
            mDrawerLayout.setDrawerListener(mDrawerToggle);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);

            if (savedInstanceState == null) {
                TaskListFragment taskListFragment = TaskListFragment.newInstance(mAccessToken);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.drawer_container, MainDrawerFragment.newInstance())
                        .add(R.id.container, taskListFragment)
                        .commit();
            }

            /** Initialize sync adapter */
            GsanaSyncAdapter.initializeSyncAdapter(this);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
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
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_logout) {
            // Remove access token. If logout chosen.
            Utility.invalidateAccessToken(this);
            startActivity(getIntent());
            return true;
        } else if (id == R.id.action_sync) {
            // Sync immediately
            GsanaSyncAdapter.syncImmediately(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        this.getSupportActionBar().setTitle(mTitle);
    }

    @Override
    public void onItemSelected(String taskId) {
        // TODO: implement two panes mode
        Intent intent = new Intent(this, TaskDetailActivity.class)
                .putExtra(TaskDetailActivity.TASK_KEY, taskId);
        startActivity(intent);
    }
}
