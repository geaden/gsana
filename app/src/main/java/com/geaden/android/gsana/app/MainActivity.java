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
import com.geaden.android.gsana.app.fragments.TaskDetailFragment;
import com.geaden.android.gsana.app.fragments.TaskListFragment;
import com.geaden.android.gsana.app.sync.GsanaSyncAdapter;


/**
 * Gsana main activity class
 */
public class MainActivity extends ActionBarActivity implements TaskListFragment.Callback,
        UserInfoListener, MainDrawerFragment.OnGsanaDrawerItemSelected {
    private static final String SELECTED_WORKSPACE_TITLE = "selected_workspace_title";
    private static final String SELECTED_PROJECT_TITLE = "selected_project_title";
    private static final String SELECTED_WORKSPACE_ID = "selected_workspace_id";
    private static final String SELECTED_PROJECT_ID = "selected_project_id";
    private final String LOG_TAG = getClass().getSimpleName();

    // Indicates whether current view in two pane mode
    private boolean mTwoPane;

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
    private static final String TASK_LIST_FRAGMENT = "task_list_fragment";
    private static final String DRAWER_FRAGMENT = "drawer_fragment";
    private String mWorkspaceSelectedTitle;
    private String mProjectSelectedTitle;
    private long mWorkspaceSelectedId;
    private long mProjectSelectedId;

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
                    getSupportActionBar().setTitle(mTitle);
                    invalidateOptionsMenu();
                }
            };

            // Set the drawer toggle as the DrawerListener
            mDrawerLayout.setDrawerListener(mDrawerToggle);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);

            if (findViewById(R.id.task_detail_container) != null) {
                // The detail container view will be present only in the large-screen layouts
                // (res/layout-sw600dp). If this view is present, then the activity should be
                // in two-pane mode.
                mTwoPane = true;
                // In two-pane mode, show the detail view in this activity by
                // adding or replacing the detail fragment using a
                // fragment transaction.
                if (savedInstanceState == null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.task_detail_container, new TaskDetailFragment())
                            .commit();
                }
            } else {
                mTwoPane = false;
            }

            if (savedInstanceState == null) {
                TaskListFragment taskListFragment = TaskListFragment.newInstance(mAccessToken);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.drawer_container, MainDrawerFragment.newInstance(), DRAWER_FRAGMENT)
                        .add(R.id.container, taskListFragment, TASK_LIST_FRAGMENT)
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
            startActivity(new Intent(this, LoginActivity.class));
            return true;
        } else if (id == R.id.action_filter_dismiss) {
            // Dismiss any task filter
            onProjectSelected(0, null);
            onWorkspaceSelected(0, null);
            ((MainDrawerFragment) getSupportFragmentManager().findFragmentByTag(DRAWER_FRAGMENT))
                    .resetDrawer();
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
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putString(TaskDetailActivity.TASK_KEY, taskId);

            TaskDetailFragment fragment = new TaskDetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.task_detail_container, fragment)
                    .commit();
        } else {
            Intent intent = new Intent(this, TaskDetailActivity.class)
                    .putExtra(TaskDetailActivity.TASK_KEY, taskId);
            startActivity(intent);
        }
    }

    @Override
    public void notifyUserInfo(String userInfo) {
        Log.v(LOG_TAG, "Notifying user info");
        TaskListFragment taskListFragment = (TaskListFragment) getSupportFragmentManager()
                .findFragmentByTag(TASK_LIST_FRAGMENT);
        taskListFragment.updateGreetingsTextView(userInfo);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(LOG_TAG, "Saving state");
        outState.putLong(SELECTED_WORKSPACE_ID, mWorkspaceSelectedId);
        outState.putLong(SELECTED_PROJECT_ID, mProjectSelectedId);
        outState.putString(SELECTED_WORKSPACE_TITLE, mWorkspaceSelectedTitle);
        outState.putString(SELECTED_PROJECT_TITLE, mProjectSelectedTitle);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore selected state
        Log.d(LOG_TAG, "Restoring state");
        mWorkspaceSelectedId = savedInstanceState.getLong(SELECTED_WORKSPACE_ID, 0);
        mProjectSelectedId = savedInstanceState.getLong(SELECTED_PROJECT_ID, 0);
        mWorkspaceSelectedTitle = savedInstanceState.getString(SELECTED_WORKSPACE_TITLE);
        mProjectSelectedTitle = savedInstanceState.getString(SELECTED_WORKSPACE_TITLE);
        onWorkspaceSelected(mWorkspaceSelectedId, mWorkspaceSelectedTitle);
        onProjectSelected(mProjectSelectedId, mProjectSelectedTitle);
    }

    @Override
    public void onWorkspaceSelected(long workspaceId, String title) {
        mWorkspaceSelectedId = workspaceId;
        ((MainDrawerFragment.OnGsanaDrawerItemSelected) getSupportFragmentManager()
                .findFragmentByTag(TASK_LIST_FRAGMENT)).onWorkspaceSelected(workspaceId, title);
        if (workspaceId > 0) {
            // Set title
            mWorkspaceSelectedTitle = title;
            setTitle(mWorkspaceSelectedTitle);
        } else {
            setTitle(getString(R.string.app_name));
        }
    }

    @Override
    public void onProjectSelected(long projectId, String title) {
        ((MainDrawerFragment.OnGsanaDrawerItemSelected) getSupportFragmentManager()
                .findFragmentByTag(TASK_LIST_FRAGMENT)).onProjectSelected(projectId, title);
        if (projectId > 0) {
            mProjectSelectedTitle = title;
            getSupportActionBar().setSubtitle(mProjectSelectedTitle);
            mDrawerLayout.closeDrawers();
            return;
        } else {
            getSupportActionBar().setSubtitle(null);
        }
    }
}
