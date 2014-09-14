package com.geaden.android.gsana.app.test;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.geaden.android.gsana.app.data.GsanaContract.WorkspaceEntry;
import com.geaden.android.gsana.app.data.GsanaContract.TaskEntry;
import com.geaden.android.gsana.app.data.GsanaContract.ProjectEntry;
import com.geaden.android.gsana.app.data.GsanaContract.UserEntry;


public class TestProvider extends AndroidTestCase {
    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    // brings the database to an empty state
    public void deleteAllRecords() {
        mContext.getContentResolver().delete(
                UserEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                TaskEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                ProjectEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                WorkspaceEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                TaskEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                ProjectEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                WorkspaceEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                UserEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    public void setUp() {
        deleteAllRecords();
    }

    public void testInsertReadProvider() {
        ContentValues testWorkspaceValues = TestDb.createWorkspaceValues();

        Uri workspaceUri = mContext.getContentResolver().insert(WorkspaceEntry.CONTENT_URI,
                testWorkspaceValues);
        Log.d(LOG_TAG, "Workspace URI " + workspaceUri);
        long workspaceRowId = ContentUris.parseId(workspaceUri);

        // Verify we got a row back.
        assertTrue(workspaceRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                WorkspaceEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDb.validateCursor(cursor, testWorkspaceValues);

        // Now see if we can successfully query if we include the row id
        cursor = mContext.getContentResolver().query(
                WorkspaceEntry.buildWorkspaceUri(TestDb.TEST_WORKSPACE_ID),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDb.validateCursor(cursor, testWorkspaceValues);

        // Add projects
        ContentValues testProjectValues = TestDb.createProjectValues(TestDb.TEST_WORKSPACE_ID);

        Uri projectUri = mContext.getContentResolver().insert(
                ProjectEntry.CONTENT_URI, testProjectValues);
        long projectRowId = ContentUris.parseId(projectUri);
        Log.d(LOG_TAG, "New project row id: " + projectRowId);
        assertTrue(projectRowId != -1);

        // Query for project
        Cursor projectCursor = mContext.getContentResolver().query(
                ProjectEntry.CONTENT_URI, // Table to Query
                null,   // leaving "columns" null just returns all the columns.
                null,   // cols for "where" clause
                null,   // values for "where" clause
                null);  // sort order

        TestDb.validateCursor(projectCursor, testProjectValues);

        // Add user
        ContentValues testUserValues = TestDb.createUserValues();
        Uri userUri = mContext.getContentResolver().insert(
                UserEntry.CONTENT_URI, testUserValues);
        long userId = ContentUris.parseId(userUri);

        assertTrue(userId != -1);

        // Query for user
        Cursor userCursor = mContext.getContentResolver().query(
                UserEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        TestDb.validateCursor(userCursor, testUserValues);

        // Add task
        ContentValues testTaskValues = TestDb.createTaskValues(TestDb.TEST_WORKSPACE_ID, TestDb.TEST_PROJECT_ID,
                TestDb.TEST_USER_ID);
        Uri taskUri = mContext.getContentResolver().insert(TaskEntry.CONTENT_URI, testTaskValues);
        long taskId = ContentUris.parseId(taskUri);
        assertTrue(taskId != -1);

        Cursor taskCursor = mContext.getContentResolver().query(
                TaskEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        TestDb.validateCursor(taskCursor, testTaskValues);
    }

    public void testGetType() {
        // content://com.geaden.android.gsana.app/worspace/
        String type = mContext.getContentResolver().getType(WorkspaceEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.geaden.android.gsana.app/worspace
        assertEquals(WorkspaceEntry.CONTENT_TYPE, type);

        long testWorkspace = 1337;
        // content://com.geaden.android.gsana.app/worskpace/1337
        type = mContext.getContentResolver().getType(
                WorkspaceEntry.buildWorkspaceUri(testWorkspace));
        // vnd.android.cursor.dir/com.geaden.android.gsana.app/worspace/1337
        assertEquals(WorkspaceEntry.CONTENT_ITEM_TYPE, type);

        // content://com.geaden.android.gsana.app/project/
        type = mContext.getContentResolver().getType(ProjectEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.geaden.android.gsana.app/project
        assertEquals(ProjectEntry.CONTENT_TYPE, type);

        long projectId = 14641;
        // content://com.geaden.android.gsana.app/project/14641
        type = mContext.getContentResolver().getType(
                ProjectEntry.buildProjectUri(projectId));
        // vnd.android.cursor.dir/com.geaden.android.gsana.app/project/14641
        assertEquals(ProjectEntry.CONTENT_ITEM_TYPE, type);

        // content://com.geaden.android.gsana.app/task/
        type = mContext.getContentResolver().getType(TaskEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.geaden.android.gsana.app/task
        assertEquals(TaskEntry.CONTENT_TYPE, type);

        long taskId = 1001;
        // content://com.geaden.android.gsana.app/task/1001
        type = mContext.getContentResolver().getType(
                TaskEntry.buildTaskUri(taskId));
        // vnd.android.cursor.dir/com.geaden.android.gsana.app/task/1001
        assertEquals(TaskEntry.CONTENT_ITEM_TYPE, type);

        // content://com.geaden.android.gsana.app/user/
        type = mContext.getContentResolver().getType(UserEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.geaden.android.gsana.app/user
        assertEquals(UserEntry.CONTENT_TYPE, type);

        long userId = 1234;
        // content://com.geaden.android.gsana.app/user/1234
        type = mContext.getContentResolver().getType(
                UserEntry.buildUserUri(userId));
        // vnd.android.cursor.dir/com.geaden.android.gsana.app/user/1234
        assertEquals(UserEntry.CONTENT_ITEM_TYPE, type);
    }

    final static int GSANA_WORKSPACE_ID = 1337;
    final static int GSANA_PROJECT_ID = 1001;

    static ContentValues createGsanaWorkspace() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(WorkspaceEntry.COLUMN_WORKSPACE_ID, GSANA_WORKSPACE_ID);
        testValues.put(WorkspaceEntry.COLUMN_WORKSPACE_NAME, "Gsana");

        return testValues;
    }

    static ContentValues createGsanaProject() {
        ContentValues testValues = new ContentValues();
        testValues.put(ProjectEntry.COLUMN_PROJECT_ID, GSANA_PROJECT_ID);
        testValues.put(ProjectEntry.COLUMN_PROJECT_WORKSPACE_ID, GSANA_WORKSPACE_ID);
        testValues.put(ProjectEntry.COLUMN_PROJECT_NAME, "Develop Android Client for Asana");
        testValues.put(ProjectEntry.COLUMN_PROJECT_COLOR, "dark-orange");
        testValues.put(ProjectEntry.COLUMN_PROJECT_ARCHIVED, 0);
        testValues.put(ProjectEntry.COLUMN_PROJECT_CREATED_AT, "20120222");
        testValues.put(ProjectEntry.COLUMN_PROJECT_NOTES, "These are things we need to purchase");

        return testValues;
    }

    // Inserts both the location and weather data for the Kalamazoo data set.
    public void insertGsanaWorkspaceData() {
        ContentValues gsanaWorksapceData = createGsanaWorkspace();
        Uri workspaceInsertUri = mContext.getContentResolver()
                .insert(WorkspaceEntry.CONTENT_URI, gsanaWorksapceData);
        assertTrue(workspaceInsertUri != null);

        ContentValues gsanaProjectValues = createGsanaProject();
        Uri projectInserUri = mContext.getContentResolver()
                .insert(ProjectEntry.CONTENT_URI, gsanaProjectValues);
        assertTrue(projectInserUri != null);
    }


    public void testUpdateAndReadWorkspace() {
        insertGsanaWorkspaceData();
        String newName = "Gsana Android Dev :)";

        // Make an update to one value.
        ContentValues gsanaWorkspaceUpdate = new ContentValues();
        gsanaWorkspaceUpdate.put(WorkspaceEntry.COLUMN_WORKSPACE_NAME, newName);

        mContext.getContentResolver().update(
                WorkspaceEntry.CONTENT_URI, gsanaWorkspaceUpdate, null, null);

        // A cursor is your primary interface to the query results.
        Cursor gsanaCursor = mContext.getContentResolver().query(
                WorkspaceEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestDb.validateCursor(gsanaCursor, gsanaWorkspaceUpdate);
    }

    // Make sure we can still delete after adding/updating stuff
    public void testDeleteRecordsAtEnd() {
        deleteAllRecords();
    }
}
