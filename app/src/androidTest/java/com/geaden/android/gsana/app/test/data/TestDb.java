package com.geaden.android.gsana.app.test.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.geaden.android.gsana.app.data.GsanaContract.UserEntry;
import com.geaden.android.gsana.app.data.GsanaContract.TaskEntry;
import com.geaden.android.gsana.app.data.GsanaContract.ProjectEntry;
import com.geaden.android.gsana.app.data.GsanaContract.WorkspaceEntry;
import com.geaden.android.gsana.app.data.GsanaDbHelper;


import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public static final int TEST_WORKSPACE_ID = 1337;
    public static final int TEST_PROJECT_ID = 1337;
    public static final int TEST_USER_ID = 5678;
    public static final int TEST_TASK_ID = 1001;

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(GsanaDbHelper.DATABASE_NAME);
        SQLiteDatabase db = GsanaDbHelper.getInstance(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {
        GsanaDbHelper dbHelper = GsanaDbHelper.getInstance(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testWorkspaceValues = createWorkspaceValues();

        long workspaceRowId;
        workspaceRowId = db.insert(WorkspaceEntry.TABLE_NAME, null, testWorkspaceValues);

        // Verify row is returned back.
        assertTrue(workspaceRowId != -1);
        Log.d(LOG_TAG, "New workspace row id: " + workspaceRowId);

        // Query for workspace
        Cursor cursor = db.query(
                WorkspaceEntry.TABLE_NAME, // Table to Query
                null, // all columns
                null, // columns for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sor order
        );

        validateCursor(cursor, testWorkspaceValues);

        // Add projects
        ContentValues testProjectValues = createProjectValues(TEST_WORKSPACE_ID);

        long projectRowId = db.insert(ProjectEntry.TABLE_NAME, null, testProjectValues);
        Log.d(LOG_TAG, "New project row id: " + projectRowId);
        assertTrue(projectRowId != -1);

        // Query for project
        Cursor projectCursor = db.query(
                ProjectEntry.TABLE_NAME, // Table to Query
                null, // all columns
                null, // columns for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sor order
        );

        validateCursor(projectCursor, testProjectValues);

        // Add user
        ContentValues testUserValues = createUserValues();

        long userId = db.insert(UserEntry.TABLE_NAME, null, testUserValues);

        assertTrue(userId != -1);

        Cursor userCursor = db.query(
                UserEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        validateCursor(userCursor, testUserValues);

        // Add task
        ContentValues testTaskValues = createTaskValues(TEST_WORKSPACE_ID, TEST_PROJECT_ID,
                TEST_USER_ID);

        long taskId = db.insert(TaskEntry.TABLE_NAME, null, testTaskValues);
        assertTrue(taskId != -1);

        Cursor taskCursor = db.query(
                TaskEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        validateCursor(taskCursor, testTaskValues);

        dbHelper.close();
    }

    static ContentValues createWorkspaceValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testWorkspaceValues = new ContentValues();
        testWorkspaceValues.put(WorkspaceEntry.COLUMN_WORKSPACE_ID, TEST_WORKSPACE_ID);
        testWorkspaceValues.put(WorkspaceEntry.COLUMN_WORKSPACE_NAME, "My Favorite Workspace");

        return testWorkspaceValues;
    }

    static ContentValues createProjectValues(int workspaceId) {
        ContentValues testProjectValues = new ContentValues();
        testProjectValues.put(ProjectEntry.COLUMN_PROJECT_WORKSPACE_ID, workspaceId);
        testProjectValues.put(ProjectEntry.COLUMN_PROJECT_ID, TEST_PROJECT_ID);
        testProjectValues.put(ProjectEntry.COLUMN_PROJECT_NAME, "Stuff to buy");
        testProjectValues.put(ProjectEntry.COLUMN_PROJECT_COLOR, "dark-red");
        testProjectValues.put(ProjectEntry.COLUMN_PROJECT_ARCHIVED, 0);
        testProjectValues.put(ProjectEntry.COLUMN_PROJECT_CREATED_AT, "20120222");
        testProjectValues.put(ProjectEntry.COLUMN_PROJECT_NOTES, "These are things we need to purchase");

        return testProjectValues;
    }

    static ContentValues createUserValues() {
        ContentValues testUserValues = new ContentValues();
        testUserValues.put(UserEntry.COLUMN_USER_ID, TEST_USER_ID);
        testUserValues.put(UserEntry.COLUMN_USER_NAME, "Greg Sanchez");
        testUserValues.put(UserEntry.COLUMN_USER_EMAIL, "gsanchez@example.com");
        testUserValues.put(UserEntry.COLUMN_USER_PHOTO_60, "https://s3.amazon.com/abcdef/");

        return testUserValues;
    }

    static ContentValues createTaskValues(int workspaceId, int projectId, int assigneeId) {
        ContentValues testTaskValues = new ContentValues();
        testTaskValues.put(TaskEntry.COLUMN_TASK_ID, TEST_TASK_ID);
        testTaskValues.put(TaskEntry.COLUMN_TASK_NAME, "Hello, world!");
        testTaskValues.put(TaskEntry.COLUMN_TASK_NOTES, "How are you today?");
        testTaskValues.put(TaskEntry.COLUMN_TASK_PROJECT_ID, projectId);
        testTaskValues.put(TaskEntry.COLUMN_TASK_WORKSPACE_ID, workspaceId);
        testTaskValues.put(TaskEntry.COLUMN_TASK_CREATED_AT, "20120222");
        testTaskValues.put(TaskEntry.COLUMN_TASK_COMPLETED, 0);
        testTaskValues.put(TaskEntry.COLUMN_TASK_ASSIGNEE_ID, assigneeId);
        testTaskValues.put(TaskEntry.COLUMN_TASK_ASSIGNEE_STATUS, "upcoming");

        return testTaskValues;
    }

    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {

        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }
}
