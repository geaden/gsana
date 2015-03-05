package com.geaden.android.gsana.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.geaden.android.gsana.app.data.GsanaContract.WorkspaceEntry;
import com.geaden.android.gsana.app.data.GsanaContract.ProjectEntry;
import com.geaden.android.gsana.app.data.GsanaContract.TaskEntry;
import com.geaden.android.gsana.app.data.GsanaContract.UserEntry;

/**
 * Manages local database for Asana data
 */
public class GsanaDbHelper extends SQLiteOpenHelper {

    private static GsanaDbHelper sInstance;

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 16;

    public static final String DATABASE_NAME = "gsana.db";

    public static GsanaDbHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new GsanaDbHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static method "getInstance()" instead.
     */
    private GsanaDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create table to hold workspaces.
        final String SQL_CREATE_WORKSPACE_TABLE = "CREATE TABLE " + WorkspaceEntry.TABLE_NAME + " (" +
                WorkspaceEntry._ID + " INTEGER PRIMARY KEY, " +
                WorkspaceEntry.COLUMN_WORKSPACE_ID + " INTEGER UNIQUE NOT NULL, " +
                WorkspaceEntry.COLUMN_WORKSPACE_NAME + " TEXT NOT NULL" +
                " );";
        // Create table to hold projects
        final String SQL_CREATE_PROJECT_TABLE = "CREATE TABLE " + ProjectEntry.TABLE_NAME + " (" +
                ProjectEntry._ID + " INTEGER PRIMARY KEY, " +
                ProjectEntry.COLUMN_PROJECT_ID + " INTEGER UNIQUE NOT NULL, " +
                ProjectEntry.COLUMN_PROJECT_WORKSPACE_ID + " INTEGER NOT NULL, " +
                ProjectEntry.COLUMN_PROJECT_CREATED_AT + " TEXT NOT NULL, " +
                // 0 - false, 1 - true
                ProjectEntry.COLUMN_PROJECT_ARCHIVED + " INTEGER NOT NULL, " +
                ProjectEntry.COLUMN_PROJECT_COLOR + " TEXT NOT NULL, " +
                ProjectEntry.COLUMN_PROJECT_NAME + " TEXT NOT NULL, " +
                ProjectEntry.COLUMN_PROJECT_NOTES + " TEXT, " +
                ProjectEntry.COLUMN_PROJECT_MODIFIED_AT + " TEXT, " +

                // Setup project workspace as foreign key
                " FOREIGN KEY (" + ProjectEntry.COLUMN_PROJECT_WORKSPACE_ID + ") REFERENCES " +
                WorkspaceEntry.TABLE_NAME + " (" + WorkspaceEntry.COLUMN_WORKSPACE_ID + ")" +
                " );";
        // Create table to hold tasks
        final String SQL_CREATE_TASK_TABLE = "CREATE TABLE " + TaskEntry.TABLE_NAME + " (" +
                TaskEntry._ID + " INTEGER PRIMARY KEY, " +
                TaskEntry.COLUMN_TASK_ID + " INTEGER UNIQUE NOT NULL, " +
                TaskEntry.COLUMN_TASK_ASSIGNEE_ID + " INTEGER, " +
                TaskEntry.COLUMN_TASK_ASSIGNEE_STATUS + " TEXT, " +
                // 0 - false, 1 - true
                TaskEntry.COLUMN_TASK_COMPLETED + " INTEGER, " +
                TaskEntry.COLUMN_TASK_COMPLETED_AT + " TEXT, " +
                TaskEntry.COLUMN_TASK_CREATED_AT + " TEXT, " +
                TaskEntry.COLUMN_TASK_DUE_ON + " TEXT, " +
                TaskEntry.COLUMN_TASK_NAME + " TEXT NOT NULL, " +
                TaskEntry.COLUMN_TASK_NOTES + " TEXT, " +
                TaskEntry.COLUMN_TASK_MODIFIED_AT + " TEXT, " +
                TaskEntry.COLUMN_TASK_PARENT_ID + " INTEGER, " +
                TaskEntry.COLUMN_TASK_PROJECT_ID + " INTEGER, " +
                TaskEntry.COLUMN_TASK_WORKSPACE_ID + " INTEGER, " +
                TaskEntry.COLUMN_TOGGL_ENTRY_ID + " INTEGER, " +
                TaskEntry.COLUMN_TOGGL_START_DATE + " TEXT, " +
                TaskEntry.COLUMN_TOGGL_END_DATE + " TEXT, " +
                TaskEntry.COLUMN_TOGGL_DURATION + " INTEGER, " +
                // Associate task with project
                " FOREIGN KEY (" + TaskEntry.COLUMN_TASK_PROJECT_ID + ") REFERENCES " +
                ProjectEntry.TABLE_NAME + " (" + ProjectEntry.COLUMN_PROJECT_ID + "), " +
                // Associate task with workspace
                " FOREIGN KEY (" + TaskEntry.COLUMN_TASK_WORKSPACE_ID + ") REFERENCES " +
                WorkspaceEntry.TABLE_NAME + " (" + WorkspaceEntry.COLUMN_WORKSPACE_ID + "), " +
                // Associate task with user
                " FOREIGN KEY (" + TaskEntry.COLUMN_TASK_ASSIGNEE_ID + ") REFERENCES " +
                UserEntry.TABLE_NAME + " (" + UserEntry.COLUMN_USER_ID + ")" +
                " );";
        // Create table to hold users
        final String SQL_CREATE_USER_TABLE = "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
                UserEntry._ID + " INTEGER PRIMARY KEY, " +
                UserEntry.COLUMN_USER_ID + " INTEGER UNIQUE NOT NULL, " +
                UserEntry.COLUMN_USER_EMAIL + " TEXT NOT NULL, " +
                UserEntry.COLUMN_USER_NAME + " TEXT NOT NULL, " +
                UserEntry.COLUMN_USER_PHOTO_URL_60 + " TEXT NOT NULL, " +
                UserEntry.COLUMN_USER_PHOTO_URL_128 + " TEXT NOT NULL, " +
                UserEntry.COLUMN_USER_PHOTO_60 + " BLOB, " +
                UserEntry.COLUMN_USER_PHOTO_128 + " BLOB, " +
                UserEntry.COLUMN_USER_PHOTO + " BLOB" +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_USER_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_WORKSPACE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_PROJECT_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TASK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WorkspaceEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ProjectEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TaskEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
