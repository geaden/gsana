package com.geaden.android.gsana.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.geaden.android.gsana.app.api.AsanaApi;
import com.geaden.android.gsana.app.api.AsanaApiImpl;

import com.geaden.android.gsana.app.data.GsanaContract.TaskEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

/**
 * Fetches tasks from Asana
 */
public class FetchAsanaTask extends AsyncTask<String, Void, String[]> {
    private final String LOG_TAG = getClass().getSimpleName();

    private Context mContext;

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

    public FetchAsanaTask(Context context) {
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
     * Forms array of strings from JSON string
     * @param asanaTasksDataJsonStr json string as response from Asana
     * @return array of tasks string representation
     * @throws org.json.JSONException
     */
    private String[] getTasksDataFromJson(String asanaTasksDataJsonStr)
            throws JSONException {
        // Fields of data json
        final String TASKS_DATA = "data";
        final String TASK_ID = "id";
        final String TASK_NAME = "name";

        JSONObject asanaTasksJson = new JSONObject(asanaTasksDataJsonStr);
        JSONArray tasks = asanaTasksJson.getJSONArray(TASKS_DATA);
        String[] tasksArray = new String[tasks.length()];

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

            taskValues.put(TaskEntry.COLUMN_TASK_ID, taskId);
            taskValues.put(TaskEntry.COLUMN_TASK_CREATED_AT, taskCreatedAt);
            taskValues.put(TaskEntry.COLUMN_TASK_WORKSPACE_ID, taskWorkspaceId);
            taskValues.put(TaskEntry.COLUMN_TASK_ASSIGNEE_STATUS, taskAssigneeStatus);
            taskValues.put(TaskEntry.COLUMN_TASK_COMPLETED, taskCompleted);
            taskValues.put(TaskEntry.COLUMN_TASK_NAME, taskName);
            taskValues.put(TaskEntry.COLUMN_TASK_DUE_ON, taskDueOn);
            taskValues.put(TaskEntry.COLUMN_TASK_NOTES, taskNotes);
            taskValues.put(TaskEntry.COLUMN_TASK_COMPLETED_AT, taskCompletedAt);
            taskValues.put(TaskEntry.COLUMN_TASK_MODIFIED_AT, taskModifiedAt);
            taskValues.put(TaskEntry.COLUMN_TASK_ASSIGNEE_ID, taskAssigneeId);

            if (taskProjectId > 0) {
                taskValues.put(TaskEntry.COLUMN_TASK_PROJECT_ID, taskProjectId);
            }

            cVVector.add(taskValues);

            tasksArray[i] = taskJson.getString(TASK_ID) + ":" + taskJson.getString(TASK_NAME);
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
    protected String[] doInBackground(String... params) {
        // Get access token from input parameters
        String accessToken = params[0];

        // Initialize Asana api
        AsanaApi asanaApi = new AsanaApiImpl(mContext, accessToken);

        try {
            String tasksJsonStr = asanaApi.getTasks();
            return getTasksDataFromJson(tasksJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error getting JSON", e);
        }
        // This will only happen if there was an error getting or parsing the tasks data.
        return null;
    }
}