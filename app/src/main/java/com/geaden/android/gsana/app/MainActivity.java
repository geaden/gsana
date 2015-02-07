package com.geaden.android.gsana.app;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.geaden.android.gsana.app.api.AsanaApi2;
import com.geaden.android.gsana.app.api.AsanaCallback;
import com.geaden.android.gsana.app.models.AsanaUser;
import com.geaden.android.gsana.app.models.AsanaWorkspace;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;


/**
 * Gsana main activity class
 */
public class MainActivity extends ActionBarActivity implements TaskListFragment.Callback {
    private final String LOG_TAG = getClass().getSimpleName();

    public static final String ACCESS_TOKEN_KEY = "access_token";

    public AsanaApi2 mAsanaApi;

    private ActionBarDrawerToggle mDrawerToggle;

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private TextView mDrawerUserInfo;
    private ImageView mDrawerUserPic;
    private AsanaWorkspace mSelectedWorkspace;

    private String[] mDrawerTitles;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private String mAccessToken;

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
            mAsanaApi = AsanaApi2.getInstance(this, mAccessToken);
            // Fetch user info
            mDrawerTitles = new String[]{"Gennady Denisov", "Projects", "Workspace"};
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

            mDrawerList = (ListView) findViewById(R.id.left_drawer_workspaces_list);

            mDrawerUserInfo = (TextView) findViewById(R.id.left_drawer_user_name);
            mDrawerUserPic = (ImageView) findViewById(R.id.left_drawer_user_pic);

            FetchUserInfoTask userInfoTask = new FetchUserInfoTask();
            userInfoTask.execute();

            // Set the list's click listener
            mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

            if (savedInstanceState == null) {
                TaskListFragment taskListFragment = TaskListFragment.newInstance(mAccessToken);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, taskListFragment)
                        .commit();
            }
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
            return true;
        } else if (id == R.id.action_logout) {
            // Remove access token. If logout chosen.
            Utility.invalidateAccessToken(this);
            startActivity(getIntent());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            selectItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        // TODO: Query for projects
        Toast.makeText(getApplicationContext(), "Selected " + position, Toast.LENGTH_SHORT).show();
//        setTitle(mDrawerTitles[position]);
//        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        this.getSupportActionBar().setTitle(mTitle);
    }

    private class FetchUserPicTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap userPic = null;
            try {
                URL url = new URL(params[0]);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                userPic = BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
            return userPic;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mDrawerUserPic.setImageBitmap(bitmap);
        }
    }

    /**
     * Fetches user info in background
     */
    public class FetchUserInfoTask extends AsyncTask<Void, Void, AsanaUser> {
        private String LOG_TAG = getClass().getSimpleName();
        private String refreshToken = null;

        @Override
        protected AsanaUser doInBackground(Void... voids) {
            final AsanaUser asanaUser = new AsanaUser();
            mAsanaApi.me(new AsanaCallback<AsanaUser>() {
                @Override
                public void onResult(AsanaUser me) {
                    Log.i(LOG_TAG, "User info: " + me);
                    asanaUser.setId(me.getId());
                    asanaUser.setName(me.getName());
                    asanaUser.setPhoto(me.getPhoto());
                    asanaUser.setWorkspaces(me.getWorkspaces());
                }

                @Override
                public void onError(Throwable e) {
                    Log.d(LOG_TAG, "Error retrieving user info " + e.getMessage());
                    Utility.invalidateAccessToken(getApplicationContext());
                    // Obtain a new token by calling new activity
                    refreshToken = Utility.getRefreshToken(getApplicationContext());
                }
            });
            return asanaUser;
        }

        @Override
        protected void onPostExecute(AsanaUser userInfo) {
            if (userInfo == null && refreshToken != null) {
                // Move to login activity
                Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                loginIntent.putExtra(Utility.REFRESH_TOKEN_KEY, refreshToken);
                getApplicationContext().startActivity(loginIntent);
                finish();
                return;
            }

            mDrawerUserInfo.setText(userInfo.getName());

            /** Fetch user pic **/
            FetchUserPicTask fetchUserPicTask = new FetchUserPicTask();
            fetchUserPicTask.execute(userInfo.getPhoto().getPhotoUrl());

            List<AsanaWorkspace> workspaces = userInfo.getWorkspaces();
            mDrawerTitles = new String[workspaces.size()];
            for (int i = 0; i < workspaces.size(); i++) {
                mDrawerTitles[i] = workspaces.get(i).getName();
            }
            // Set the adapter for the list view
            mWorkspaceAdapter = new ArrayAdapter<String>(getApplicationContext(),
                    R.layout.drawer_list_item, mDrawerTitles);
            mDrawerList.setAdapter(mWorkspaceAdapter);
            // Select first workspace by default
            mDrawerList.setSelection(0);
            mSelectedWorkspace = workspaces.get(0);
        }
    }

    @Override
    public void onItemSelected(String taskId) {
        // TODO: implement two panes mode
        Intent intent = new Intent(this, TaskDetailActivity.class)
                .putExtra(TaskDetailActivity.TASK_KEY, taskId);
        startActivity(intent);
    }

    @Override
    public void bindValues(CursorAdapter cursorAdapter, Cursor cursor) {
        // Bind data to drawer layout
        if (cursor.getCount() > 0) {
            if (mDrawerLayout == null) {
                mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            }
            cursorAdapter.bindView(mDrawerLayout, getApplicationContext(), cursor);
        } else {
            Log.d(LOG_TAG, "No data returned");
        }
    }
}
