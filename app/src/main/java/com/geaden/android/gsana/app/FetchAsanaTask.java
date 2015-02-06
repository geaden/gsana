package com.geaden.android.gsana.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.AsyncTask;
import android.util.Log;

import com.geaden.android.gsana.app.api.AsanaApi2;


import com.geaden.android.gsana.app.api.AsanaCallback;
import com.geaden.android.gsana.app.api.AsanaResponse;
import com.geaden.android.gsana.app.data.GsanaContract.TaskEntry;
import com.geaden.android.gsana.app.models.AsanaTask;
import com.geaden.android.gsana.app.models.AsanaWorkspace;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Vector;

/**
 * Fetches tasks from Asana
 */
public class FetchAsanaTask extends AsyncTask<String, Void, Void> {
    private final String LOG_TAG = getClass().getSimpleName();

    private Context mContext;

    private List<AsanaTask> mAsanaTasks;

    private boolean DEBUG = true;

    private JSONObject mJsonTasksData;
    private AsanaWorkspace mAsanaWorkspace;

    public FetchAsanaTask(Context context, AsanaWorkspace workspace) {
        mContext = context;
        mAsanaWorkspace = workspace;
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
     * Forms array of strings from JSON string
     * @param asanaTasksDataJson json data of response from Asana
     * @return array of tasks string representation
     * @throws org.json.JSONException
     */
    private String[] getTasksDataFromJson(JSONObject asanaTasksDataJson)
            throws JSONException, AsanaResponse.MalformedResponseException {
        AsanaResponse response = new AsanaResponse(asanaTasksDataJson);
        JSONArray tasks = (JSONArray) response.getData();
        String[] tasksArray = new String[tasks.length()];

        // Get and insert the new tasks information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(tasks.length());
        for (int i = 0; i < tasks.length(); i++) {
            AsanaTask task = new AsanaTask(tasks.getJSONObject(i));

            ContentValues taskValues = new ContentValues();

            taskValues.put(TaskEntry.COLUMN_TASK_ID, task.getId());
            taskValues.put(TaskEntry.COLUMN_TASK_CREATED_AT, task.getCreatedAt());
            taskValues.put(TaskEntry.COLUMN_TASK_WORKSPACE_ID, task.getWorkspace().getId());
            taskValues.put(TaskEntry.COLUMN_TASK_ASSIGNEE_STATUS, task.getAssigneeStatus());
            taskValues.put(TaskEntry.COLUMN_TASK_COMPLETED, task.getCompleted());
            taskValues.put(TaskEntry.COLUMN_TASK_NAME, task.getName());
            taskValues.put(TaskEntry.COLUMN_TASK_DUE_ON, task.getDueOn());
            taskValues.put(TaskEntry.COLUMN_TASK_NOTES, task.getNotes());
            taskValues.put(TaskEntry.COLUMN_TASK_COMPLETED_AT, task.getCompletedAt());
            taskValues.put(TaskEntry.COLUMN_TASK_MODIFIED_AT, task.getModifiedAt());
            taskValues.put(TaskEntry.COLUMN_TASK_ASSIGNEE_ID, task.getAssigneeId());
            taskValues.put(TaskEntry.COLUMN_TASK_PROJECT_ID, task.getProjects().get(0).getId());
            cVVector.add(taskValues);

            tasksArray[i] = task.getId() + ":" + task.getName();
        }
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            int rowsInserted = mContext.getContentResolver()
                    .bulkInsert(TaskEntry.CONTENT_URI, cvArray);
            Log.v(LOG_TAG, "inserted " + rowsInserted + " tasks");
            // Use a DEBUG variable to gate whether or not you do this, so you can easily
            // turn it on and off, and so that it's easy to see what you can rip out if
            // you ever want to remove it.
            if (DEBUG) {
                Cursor taskCursor = mContext.getContentResolver().query(
                        TaskEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                );

                if (taskCursor.moveToFirst()) {
                    ContentValues resultValues = new ContentValues();
                    DatabaseUtils.cursorRowToContentValues(taskCursor, resultValues);
                    Log.v(LOG_TAG, "Query succeeded! **********");
                    for (String key : resultValues.keySet()) {
                        Log.v(LOG_TAG, key + ": " + resultValues.getAsString(key));
                    }
                } else {
                    Log.v(LOG_TAG, "Query failed! :( **********");
                }
            }
        }
        return tasksArray;
    }

    @Override
    protected Void doInBackground(String... params) {
        // Get access token from input parameters
        String accessToken = params[0];

        // Initialize Asana api
        AsanaApi2 asanaApi = AsanaApi2.getInstance(mContext, accessToken);

        asanaApi.tasks(mAsanaWorkspace, new AsanaCallback<List<AsanaTask>>() {
            @Override
            public void onResult(List<AsanaTask> asanaTask) {
                mAsanaTasks = asanaTask;
            }

            @Override
            public void onError(Throwable e) {
                Log.d(LOG_TAG, "Error " + e.getMessage());
            }
        });
        return null;
    }
}