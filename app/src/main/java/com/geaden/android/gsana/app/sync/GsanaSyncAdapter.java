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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.geaden.android.gsana.app.R;
import com.geaden.android.gsana.app.Utility;
import com.geaden.android.gsana.app.api.AsanaApi;
import com.geaden.android.gsana.app.api.AsanaApi2;
import com.geaden.android.gsana.app.api.AsanaApiImpl;
import com.geaden.android.gsana.app.api.AsanaCallback;
import com.geaden.android.gsana.app.data.GsanaContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;


public class GsanaSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = GsanaSyncAdapter.class.getSimpleName();

    private String mDefaultWorkspaceId;

    // Interval at which to sync with Asana, in milliseconds.
    // 1000 milliseconds (1 second) * 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    private boolean DEBUG = true;

    // Json tasks key
    final String ASANA_TASK_ID = "id";
    final String ASANA_TASK_NAME = "name";
    final String ASANA_TASK_NOTES = "notes";
    final String ASANA_TASK_WORKSPACE = "workspace";
    final String ASANA_TASK_ASSIGNEE_ID = "assignee_id";
    final String ASANA_TASK_MODIFIED_AT = "modified_at";
    final String ASANA_TASK_PROJECTS = "projects";
    final String ASANA_TASK_DUE_ON = "due_on";
    final String ASANA_TASK_COMPLETED = "completed";
    final String ASANA_TASK_CREATED_AT = "created_at";
    final String ASANA_TASK_COMPLETED_AT = "completed_at";
    final String ASANA_TASK_ASSIGNEE_STATUS = "assignee_status";

    // Json project keys
    final String ASANA_PROJECT_ID = "id";
    final String ASANA_PROJECT_NAME = "name";

    // Json workspace keys
    final String ASANA_WORKSPACE_ID = "id";
    final String ASANA_WORKSPACE_NAME = "name";

    private final Context mContext;

    public GsanaSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        Log.d(LOG_TAG, "Creating Sync Adapter");
        mContext = context;
    }

    /**
     * Gets value from data as {@link JSONObject} by value
     * @param data json representation of data
     * @param key the key to get value for
     * @return value
     */
    private String getValue(JSONObject data, String key) {
        if (data.has(key)) {
            try {
                return data.getString(key);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error to get value for " + key);
            }
        }
        return null;
    }

    /**
     * Performs data insert
     * @param contentUri content uri to call data insertion
     * @param cVVector
     */
    private void performDataInsert(Uri contentUri, Vector<ContentValues> cVVector) {
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            int rowsInserted = mContext.getContentResolver()
                    .bulkInsert(contentUri, cvArray);
            Log.d(LOG_TAG, "inserted " + rowsInserted + " tasks");
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
     * Performs workspaces insertion
     * @param workspacesData workspace data as JSON returned from calling Asana api
     */
    private void performWorkspacesInsert(JSONObject workspacesData) {
        final String DATA = "data";
        try {
            JSONArray workspaces = workspacesData.getJSONArray(DATA);
            // Get and insert the new workspaces information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(workspaces.length());
            for (int i = 0; i < workspaces.length(); i++) {
                JSONObject workspaceObj = workspaces.getJSONObject(i);
                String workspaceIdKey = "id";
                String workspaceNameKey = "name";

                String workspaceId = workspaceObj.getString(workspaceIdKey);
                String workspaceName = workspaceObj.getString(workspaceNameKey);

                ContentValues workspaceValues = new ContentValues();
                mDefaultWorkspaceId = workspaceId;
                Log.d(LOG_TAG, "Default workspace id " + mDefaultWorkspaceId);
                workspaceValues.put(GsanaContract.WorkspaceEntry.COLUMN_WORKSPACE_ID, workspaceId);
                workspaceValues.put(GsanaContract.WorkspaceEntry.COLUMN_WORKSPACE_NAME, workspaceName);
                cVVector.add(workspaceValues);
            }
            performDataInsert(GsanaContract.WorkspaceEntry.CONTENT_URI, cVVector);
        } catch (JSONException e) {
            Log.d(LOG_TAG, e.getMessage(), e);
        }
    }

    /**
     * Performs tasks insertion
     * @param asanaTasksJson returned asana tasks data
     */
    private void performTasksInsertion(JSONObject asanaTasksJson) {
        final String DATA = "data";
        try {
            JSONArray tasks = asanaTasksJson.getJSONArray(DATA);

            // Get and insert the new tasks information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(tasks.length());
            for (int i = 0; i < tasks.length(); i++) {
                // Collected values
                long taskId;
                String taskName;
                String taskNotes;
                long taskAssigneeId;
                String taskAssigneeStatus;
                long taskProjectId;
                String taskCompleted;
                String taskCompletedAt;
                String taskModifiedAt;
                String taskCreatedAt;
                String taskDueOn;
                long taskWorkspaceId;

                // Task object and it's fields
                JSONObject taskJson = tasks.getJSONObject(i);

                taskId = taskJson.getLong(ASANA_TASK_ID);
                taskName = taskJson.getString(ASANA_TASK_NAME);
                taskNotes = getValue(taskJson, ASANA_TASK_NOTES);
                taskAssigneeStatus = getValue(taskJson, ASANA_TASK_ASSIGNEE_STATUS);
                taskAssigneeId = taskJson.has(ASANA_TASK_ASSIGNEE_ID) ? taskJson.getLong(ASANA_TASK_ASSIGNEE_ID) : 0;
                taskCompleted = getValue(taskJson, ASANA_TASK_COMPLETED);
                taskCompletedAt = getValue(taskJson, ASANA_TASK_COMPLETED_AT);
                taskModifiedAt = getValue(taskJson, ASANA_TASK_MODIFIED_AT);
                taskCreatedAt = getValue(taskJson, ASANA_TASK_CREATED_AT);
                taskDueOn = getValue(taskJson, ASANA_TASK_DUE_ON);
                if (taskJson.has(ASANA_TASK_WORKSPACE)) {
                    JSONObject taskWorkspace = taskJson.getJSONObject(ASANA_TASK_WORKSPACE);
                    taskWorkspaceId = taskWorkspace.getLong(ASANA_WORKSPACE_ID);
                } else {
                    taskWorkspaceId = 0;
                }
                if (taskJson.has(ASANA_TASK_PROJECTS)) {
                    JSONArray taskProjects = taskJson.getJSONArray(ASANA_TASK_PROJECTS);
                    // For now just take first project
                    taskProjectId = 0;
                    if (taskProjects.length() > 0) {
                        JSONObject taskProject = taskProjects.getJSONObject(0);
                        taskProjectId = taskProject.getLong(ASANA_PROJECT_ID);
                    }
                } else {
                    taskProjectId = 0;
                }

                ContentValues taskValues = new ContentValues();

                taskValues.put(GsanaContract.TaskEntry.COLUMN_TASK_ID, taskId);
                taskValues.put(GsanaContract.TaskEntry.COLUMN_TASK_CREATED_AT, taskCreatedAt);
                taskValues.put(GsanaContract.TaskEntry.COLUMN_TASK_WORKSPACE_ID, taskWorkspaceId);
                taskValues.put(GsanaContract.TaskEntry.COLUMN_TASK_ASSIGNEE_STATUS, taskAssigneeStatus);
                taskValues.put(GsanaContract.TaskEntry.COLUMN_TASK_COMPLETED, taskCompleted);
                taskValues.put(GsanaContract.TaskEntry.COLUMN_TASK_NAME, taskName);
                taskValues.put(GsanaContract.TaskEntry.COLUMN_TASK_DUE_ON, taskDueOn);
                taskValues.put(GsanaContract.TaskEntry.COLUMN_TASK_NOTES, taskNotes);
                taskValues.put(GsanaContract.TaskEntry.COLUMN_TASK_COMPLETED_AT, taskCompletedAt);
                taskValues.put(GsanaContract.TaskEntry.COLUMN_TASK_MODIFIED_AT, taskModifiedAt);
                taskValues.put(GsanaContract.TaskEntry.COLUMN_TASK_ASSIGNEE_ID, taskAssigneeId);

                if (taskProjectId > 0) {
                    taskValues.put(GsanaContract.TaskEntry.COLUMN_TASK_PROJECT_ID, taskProjectId);
                }

                cVVector.add(taskValues);
            }
            performDataInsert(GsanaContract.TaskEntry.CONTENT_URI, cVVector);
        } catch (JSONException e) {
            Log.d(LOG_TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String authority,
                              ContentProviderClient contentProviderClient, SyncResult syncResult) {
        Log.d(LOG_TAG, "Start sync");
        // Getting the access token to send to the Api
        String accessToken = Utility.getAccessToken(mContext);

        // Initialize AsanaApiClient
        AsanaApi2 asanaApi = AsanaApi2.getInstance(mContext, accessToken);

        // Order matters, as we should obtain default workspace id
        asanaApi.workspaces(new AsanaCallback() {

            @Override
            public void onResult(JSONObject data) {
                if (data == null) {
                    return;
                }
                try {
                    Log.d(LOG_TAG, data.toString(4));
                    performWorkspacesInsert(data);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
            }

            @Override
            public void onError() {
                Log.e(LOG_TAG, "Error retrieving workspaces");
            }
        });

        asanaApi.tasks(mDefaultWorkspaceId, new AsanaCallback() {
            @Override
            public void onResult(JSONObject data) {
                if (data == null) {
                    return;
                }
                try {
                    Log.d(LOG_TAG, data.toString(4));
                    performTasksInsertion(data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError() {
                Log.e(LOG_TAG, "Error retrieving tasks");
            }
        });

    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context An app context
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
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
        getSyncAccount(context);
    }

}
