package com.geaden.android.gsana.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.geaden.android.gsana.app.R;
import com.geaden.android.gsana.app.api.AsanaApi;
import com.geaden.android.gsana.app.api.AsanaApiImpl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


/**
 * Gsana main activity class
 */
public class MainActivity extends ActionBarActivity {
    private final String LOG_TAG = getClass().getSimpleName();

    public static final String ACCESS_TOKEN_KEY = "access_token";

    public AsanaApi mAsanaApi;

    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private TextView mDrawerUserInfo;
    private ImageView mDrawerUserPic;

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
            mAsanaApi = new AsanaApiImpl(mAccessToken);
            // Fetch user info
            mDrawerTitles = new String[]{"Gennady Denisov", "Projects", "Workspace"};
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            mDrawerToggle = new ActionBarDrawerToggle(
                    this,                  /* host Activity */
                    mDrawerLayout,         /* DrawerLayout object */
                    R.drawable.ic_navigation_drawer,  /* nav drawer icon to replace 'Up' caret */
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

            mDrawerList = (ListView) findViewById(R.id.left_drawer_workspace_list);

            mDrawerUserInfo = (TextView) findViewById(R.id.left_drawer_user_name);
            mDrawerUserPic = (ImageView) findViewById(R.id.left_drawer_user_pic);

            FetchUserInfoTask userInfoTask = new FetchUserInfoTask();
            userInfoTask.execute();

            // Set the list's click listener
            mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

            if (savedInstanceState == null) {
                TasksFragment tasksFragment = TasksFragment.newInstance(mAccessToken);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, tasksFragment)
                        .commit();
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
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
            invalidateAccessToken();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Invalidates access token
     */
    public void invalidateAccessToken() {
        Editor editor = getSharedPreferences(Constants.SHARED_PREF_KEY,
                Context.MODE_PRIVATE).edit();
        editor.remove(ACCESS_TOKEN_KEY);
        editor.commit();
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            selectItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        // Create a new fragment and specify the planet to show based on position

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mDrawerTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
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
    public class FetchUserInfoTask extends AsyncTask<Void, Void, JSONObject> {
        private String LOG_TAG = getClass().getSimpleName();
        private JSONObject userInfo = null;

        @Override
        protected JSONObject doInBackground(Void... voids) {
            userInfo = mAsanaApi.getUserInfo();
            return userInfo;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            final String DATA = "data";
            final String USER_NAME = "name";
            final String WORKSPACES = "workspaces";
            final String WORKSPACE_NAME = "name";
            final String USER_PHOTO = "photo";
            final String USER_PHOTO_URL = "image_60x60";
            try {
                mDrawerUserInfo.setText(userInfo.getJSONObject(DATA).getString(USER_NAME));
                String userPhotoUrl = userInfo.getJSONObject(DATA).getJSONObject(USER_PHOTO)
                        .getString(USER_PHOTO_URL);

                /** Fetch user pic **/
                FetchUserPicTask fetchUserPicTask = new FetchUserPicTask();
                fetchUserPicTask.execute(userPhotoUrl);

                JSONArray workspaces = userInfo.getJSONObject(DATA).getJSONArray(WORKSPACES);
                mDrawerTitles = new String[workspaces.length()];
                for (int i = 0; i < workspaces.length(); i++) {
                    JSONObject workspace = workspaces.getJSONObject(i);
                    mDrawerTitles[i] = workspace.getString(WORKSPACE_NAME);
                }
                // Set the adapter for the list view
                mWorkspaceAdapter = new ArrayAdapter<String>(getApplicationContext(),
                        R.layout.drawer_list_item, mDrawerTitles);
                mDrawerList.setAdapter(mWorkspaceAdapter);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error setting user info", e);
                invalidateAccessToken();
            } catch (NullPointerException e) {
                Log.e(LOG_TAG, "Error", e);
                invalidateAccessToken();
            }
        }
    }
}