package com.geaden.android.gsana.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.geaden.android.gsana.app.R;
import com.geaden.android.gsana.app.Utility;
import com.geaden.android.gsana.app.api.AsanaApi2;
import com.geaden.android.gsana.app.api.AsanaCallback;
import com.geaden.android.gsana.app.data.GsanaContract;
import com.geaden.android.gsana.app.models.AsanaProject;
import com.geaden.android.gsana.app.models.AsanaTask;
import com.geaden.android.gsana.app.models.AsanaUser;
import com.geaden.android.gsana.app.models.AsanaWorkspace;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;


public class GsanaSyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String LOG_TAG = GsanaSyncAdapter.class.getSimpleName();

    private AsanaWorkspace mDefaultWorkspace;

    // Interval at which to sync with Asana, in milliseconds.
    // 1000 milliseconds (1 second) * 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 1000 * 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    private boolean DEBUG = false;

    private final Context mContext;

    public GsanaSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        Log.d(LOG_TAG, "Creating Sync Adapter");
        mContext = context;
    }

    /**
     * Performs insert if no data found or update.
     * @param contentUri content uri to call data insertion
     * @param cVVector
     */
    private void insertOrUpdate(Uri contentUri, Vector<ContentValues> cVVector) {
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            int rowsInserted = 0;
            try {
                rowsInserted = mContext.getContentResolver()
                        .bulkInsert(contentUri, cvArray);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error performing insert on " + contentUri.toString());
            }
            Log.d(LOG_TAG, "inserted " + rowsInserted);
            // Use a DEBUG variable to gate whether or not you do this, so you can easily
            // turn it on and off, and so that it's easy to see what you can rip out if
            // you ever want to remove it.
            if (DEBUG) {
                Cursor cursor = mContext.getContentResolver().query(
                        contentUri,
                        null,
                        null,
                        null,
                        null
                );

                if (cursor.moveToFirst()) {
                    ContentValues resultValues = new ContentValues();
                    DatabaseUtils.cursorRowToContentValues(cursor, resultValues);
                    Log.v(LOG_TAG, "Query succeeded! **********");
                    for (String key : resultValues.keySet()) {
                        Log.v(LOG_TAG, key + ": " + resultValues.getAsString(key));
                    }
                } else {
                    Log.v(LOG_TAG, "Query failed! :( **********");
                }
            }
        }
    }

    /**
     * Performs projects insertion
     * @param projects list of projects to insert
     */
    private void performaProjectsInsert(List<AsanaProject> projects) {
        Log.d(LOG_TAG, "Performing Projects Insertion");
        Vector<ContentValues> cVVector = new Vector<ContentValues>(projects.size());
        for (AsanaProject project : projects) {
            ContentValues values = new ContentValues();
            values.put(GsanaContract.ProjectEntry.COLUMN_PROJECT_ID, project.getId());
            values.put(GsanaContract.ProjectEntry.COLUMN_PROJECT_CREATED_AT, project.getCreatedAt());
            values.put(GsanaContract.ProjectEntry.COLUMN_PROJECT_COLOR, project.getColor());
            values.put(GsanaContract.ProjectEntry.COLUMN_PROJECT_WORKSPACE_ID, project.getWorkspace().getId());
            values.put(GsanaContract.ProjectEntry.COLUMN_PROJECT_ARCHIVED, project.getArchived());
            values.put(GsanaContract.ProjectEntry.COLUMN_PROJECT_NAME, project.getName());
            values.put(GsanaContract.ProjectEntry.COLUMN_PROJECT_MODIFIED_AT, project.getModifiedAt());
            cVVector.add(values);
        }
        insertOrUpdate(GsanaContract.ProjectEntry.CONTENT_URI, cVVector);
    }

    /**
     * Performs workspaces insertion
     * @param workspaces list of workspaces returned from calling Asana api
     */
    private void performWorkspacesInsert(List<AsanaWorkspace> workspaces) {
        // Get and insert the new workspaces information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(workspaces.size());
        mDefaultWorkspace = null;
        for (AsanaWorkspace workspace : workspaces) {
            ContentValues workspaceValues = new ContentValues();
            if (mDefaultWorkspace == null) {
                mDefaultWorkspace = workspace;
                Utility.putSettingsStringValue(mContext, Utility.DEFAULT_WORKSPACE_KEY,
                        String.valueOf(mDefaultWorkspace.getId()));
                Log.d(LOG_TAG, "Default workspace " + mDefaultWorkspace.toString());
            }
            workspaceValues.put(GsanaContract.WorkspaceEntry.COLUMN_WORKSPACE_ID, workspace.getId());
            workspaceValues.put(GsanaContract.WorkspaceEntry.COLUMN_WORKSPACE_NAME, workspace.getName());
            cVVector.add(workspaceValues);
        }
        insertOrUpdate(GsanaContract.WorkspaceEntry.CONTENT_URI, cVVector);
    }

    /**
     * Performs tasks insertion
     * @param asanaTasks returned asana tasks data
     */
    private void performTasksInsertion(List<AsanaTask> asanaTasks) {
        // Get and insert the new tasks information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(asanaTasks.size());
        for (AsanaTask task : asanaTasks) {
            ContentValues taskValues = new ContentValues();

            taskValues.put(GsanaContract.TaskEntry.COLUMN_TASK_ID, task.getId());
            taskValues.put(GsanaContract.TaskEntry.COLUMN_TASK_CREATED_AT, task.getCreatedAt());
            taskValues.put(GsanaContract.TaskEntry.COLUMN_TASK_WORKSPACE_ID, task.getWorkspace().getId());
            taskValues.put(GsanaContract.TaskEntry.COLUMN_TASK_ASSIGNEE_STATUS, task.getAssigneeStatus());
            taskValues.put(GsanaContract.TaskEntry.COLUMN_TASK_COMPLETED, task.getCompleted());
            taskValues.put(GsanaContract.TaskEntry.COLUMN_TASK_NAME, task.getName());
            taskValues.put(GsanaContract.TaskEntry.COLUMN_TASK_DUE_ON, task.getDueOn());
            taskValues.put(GsanaContract.TaskEntry.COLUMN_TASK_NOTES, task.getNotes());
            taskValues.put(GsanaContract.TaskEntry.COLUMN_TASK_COMPLETED_AT, task.getCompletedAt());
            taskValues.put(GsanaContract.TaskEntry.COLUMN_TASK_MODIFIED_AT, task.getModifiedAt());
            taskValues.put(GsanaContract.TaskEntry.COLUMN_TASK_ASSIGNEE_ID,
                    task.getAssignee() != null ? task.getAssignee().getId() : null);


            if (task.getProjects().size() > 0) {
                // Probably not a good way, but so far ok
                taskValues.put(GsanaContract.TaskEntry.COLUMN_TASK_PROJECT_ID, task.getProjects().get(0).getId());
            }

            cVVector.add(taskValues);
        }
        insertOrUpdate(GsanaContract.TaskEntry.CONTENT_URI, cVVector);
    }

    /**
     * Fetches user picture from url
     * @param userPhoto user photo data (i.e. 60x60 pics url)
     * @return {@link android.graphics.Bitmap} user picture
     */
    private Bitmap[] fetchUserPics(AsanaUser.UserPhoto userPhoto) {
        Bitmap[] userPics = new Bitmap[2];
        try {
            URL url = new URL(userPhoto.getPhoto60Url());
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            userPics[0] = BitmapFactory.decodeStream(input);
            url = new URL(userPhoto.getPhoto128Url());
            connection = (HttpsURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            input = connection.getInputStream();
            userPics[1] = BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        return userPics;
    }

    /**
     * Performs user data insertion
     * @param asanaUser returned asana user data
     */
    private void performUserInsertion(AsanaUser asanaUser) {
        // Get and insert user information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(1);
        ContentValues userValues = new ContentValues();
        userValues.put(GsanaContract.UserEntry.COLUMN_USER_ID, asanaUser.getId());
        userValues.put(GsanaContract.UserEntry.COLUMN_USER_NAME, asanaUser.getName());
        userValues.put(GsanaContract.UserEntry.COLUMN_USER_EMAIL, asanaUser.getEmail());
        userValues.put(GsanaContract.UserEntry.COLUMN_USER_PHOTO_URL_60, asanaUser.getPhoto().getPhoto60Url());
        userValues.put(GsanaContract.UserEntry.COLUMN_USER_PHOTO_URL_128, asanaUser.getPhoto().getPhoto128Url());
        /** Fetch user picture and insert to database **/
        Bitmap[] userPics = fetchUserPics(asanaUser.getPhoto());
        ByteArrayOutputStream outStr = new ByteArrayOutputStream();
        userPics[0].compress(Bitmap.CompressFormat.PNG, 100, outStr);
        byte[] blob60 = outStr.toByteArray();
        userValues.put(GsanaContract.UserEntry.COLUMN_USER_PHOTO_60, blob60);
        userValues.put(GsanaContract.UserEntry.COLUMN_USER_PHOTO, blob60);
        outStr = new ByteArrayOutputStream();
        userPics[1].compress(Bitmap.CompressFormat.PNG, 100, outStr);
        byte[] blob128 = outStr.toByteArray();
        userValues.put(GsanaContract.UserEntry.COLUMN_USER_PHOTO_128, blob128);
        cVVector.add(userValues);
        insertOrUpdate(GsanaContract.UserEntry.CONTENT_URI, cVVector);
    }


    @Override
    public void onPerformSync(Account account, Bundle bundle, String authority,
                              ContentProviderClient contentProviderClient, SyncResult syncResult) {
        // Getting the access token to send to the Api
        String accessToken = Utility.getAccessToken(mContext);

        // Get instance AsanaApiClient
        final AsanaApi2 asanaApi = AsanaApi2.getInstance(mContext);

        // TODO: Check if update needed

        // Retrieve user info
        asanaApi.me(new AsanaCallback<AsanaUser>() {
            @Override
            public void onResult(AsanaUser user) {
                // Put user id to shared preferences
                Utility.putSettingsStringValue(mContext, Utility.CURRENT_USER_KEY,
                        String.valueOf(user.getId()));
                performUserInsertion(user);
            }

            @Override
            public void onError(Throwable exception) {
                Log.e(LOG_TAG, "Error retrieving user info " + exception.getMessage());
            }
        });

        // Order matters, as we should obtain default workspace id first
        asanaApi.workspaces(new AsanaCallback<List<AsanaWorkspace>>() {

            @Override
            public void onResult(List<AsanaWorkspace> workspaces) {
                Log.d(LOG_TAG, "Retrieved workspaces");
                performWorkspacesInsert(workspaces);
                for (AsanaWorkspace asanaWorkspace : workspaces) {
                    asanaApi.projects(asanaWorkspace, new AsanaCallback<List<AsanaProject>>() {
                        @Override
                        public void onResult(List<AsanaProject> asanaProjects) {
                            for (final AsanaProject asanaProject : asanaProjects) {
                                asanaApi.getProjectDetails(asanaProject, new AsanaCallback<AsanaProject>() {
                                    @Override
                                    public void onResult(AsanaProject value) {
                                        asanaProject.setColor(value.getColor());
                                        asanaProject.setWorkspace(value.getWorkspace());
                                        asanaProject.setNotes(value.getNotes());
                                        asanaProject.setArchived(value.getArchived());
                                        asanaProject.setCreatedAt(value.getCreatedAt());
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Log.d(LOG_TAG, "Error Retrieving project details " + e.getMessage());
                                    }
                                });
                            }
                            performaProjectsInsert(asanaProjects);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d(LOG_TAG, "Error retrieving projects " + e.getMessage());
                        }
                    });

                    asanaApi.tasks(asanaWorkspace, new AsanaCallback<List<AsanaTask>>() {
                        @Override
                        public void onResult(final List<AsanaTask> asanaTasks) {
                            for (final AsanaTask asanaTask : asanaTasks) {
                                asanaApi.getTaskDetail(asanaTask, new AsanaCallback<AsanaTask>() {
                                    @Override
                                    public void onResult(AsanaTask value) {
                                        asanaTask.setAssigneeId(value.getAssignee());
                                        asanaTask.setNotes(value.getNotes());
                                        asanaTask.setAssigneeStatus(value.getAssigneeStatus());
                                        asanaTask.setCompleted(value.getCompleted());
                                        asanaTask.setDueOn(value.getDueOn());
                                        asanaTask.setProjects(value.getProjects());
                                        asanaTask.setWorkspace(value.getWorkspace());
                                    }

                                    @Override
                                    public void onError(Throwable exception) {
                                        Log.d(LOG_TAG, exception.getMessage());
                                    }
                                });
                            }
                            performTasksInsertion(asanaTasks);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(LOG_TAG, "Error retrieving tasks " + e.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(LOG_TAG, "Error retrieving workspaces " + e.getMessage());
            }
        });
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context An app context
     */
    public static void syncImmediately(Context context) {
        Log.d(LOG_TAG, "syncImmediately invoked");
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        /*
        * Request the sync for the default account, authority, and
        * manual sync settings
        */
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        Log.d(LOG_TAG, "getSyncAccount invoked");
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {
            // Add the account and account type, no password or user data
            // If successful, return the Account object, otherwise report an error.
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }

            // If you don't set android:syncable="true" in
            // in your <provider> element in the manifest,
            // then call context.setIsSyncable(account, AUTHORITY, 1)
            // here.
            onAccountCreated(newAccount, context);
        }
        Log.d(LOG_TAG, "getSyncAccount finished");
        return newAccount;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {

        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    setExtras(new Bundle()).
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    private static void onAccountCreated(Account newAccount, Context context) {

        // Schedule the sync for periodic execution
        GsanaSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        // Without calling setSyncAutomatically, our periodic sync will not be enabled.
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        // Let's do a sync to get things started.
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        Log.v(LOG_TAG, "Sync adapter initializing...");
        getSyncAccount(context);
    }

}
