package com.geaden.android.gsana.app.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * Main Gsana content provider
 */
public class GsanaProvider extends ContentProvider {
    private static final String LOG_TAG = GsanaProvider.class.getSimpleName();
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private GsanaDbHelper mOpenHelper;

    private static final int WORKSPACE = 100;
    private static final int WORKSPACE_ID = 101;
    private static final int PROJECT = 200;
    private static final int PROJECT_ID = 201;
    private static final int PROJECT_WITH_WORKSPACE = 202;
    private static final int TASK = 300;
    private static final int TASK_ID = 301;
    private static final int TASK_WITH_WORKSPACE = 302;
    private static final int USER = 400;
    private static final int USER_ID = 401;

    private static final SQLiteQueryBuilder sTasksByWorkspaceQueryBuilder;

    static{
        sTasksByWorkspaceQueryBuilder = new SQLiteQueryBuilder();
        sTasksByWorkspaceQueryBuilder.setTables(
                GsanaContract.WorkspaceEntry.TABLE_NAME + " INNER JOIN " +
                        GsanaContract.TaskEntry.TABLE_NAME +
                        " ON " + GsanaContract.WorkspaceEntry.TABLE_NAME +
                        "." + GsanaContract.WorkspaceEntry.COLUMN_WORKSPACE_ID +
                        " = " + GsanaContract.TaskEntry.TABLE_NAME +
                        "." + GsanaContract.TaskEntry.COLUMN_TASK_WORKSPACE_ID);
    }

    /** Define common use SQL **/
    private static final String sTaskWorkspaceSelectioin = "";


    /**
     * Gets tasks by workspace
     * @param uri
     * @param projection
     * @param sortOrder
     * @return
     */
    private Cursor getTasksByWorkspaceId(
            Uri uri, String[] projection, String sortOrder) {
        return sTasksByWorkspaceQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                null,
                null,
                null,
                null,
                sortOrder);

    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new GsanaDbHelper(getContext());
        return true;

    }

    private static UriMatcher buildUriMatcher() {
        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = GsanaContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, GsanaContract.PATH_WORKSPACE, WORKSPACE);
        matcher.addURI(authority, GsanaContract.PATH_WORKSPACE + "/#", WORKSPACE_ID);

        matcher.addURI(authority, GsanaContract.PATH_PROJECT, PROJECT);
        matcher.addURI(authority, GsanaContract.PATH_PROJECT + "/#", PROJECT_ID);
        matcher.addURI(authority, GsanaContract.PATH_PROJECT + "/*", PROJECT_WITH_WORKSPACE);

        matcher.addURI(authority, GsanaContract.PATH_TASK, TASK);
        matcher.addURI(authority, GsanaContract.PATH_TASK + "/#", TASK_ID);
        matcher.addURI(authority, GsanaContract.PATH_TASK + "/*", TASK_WITH_WORKSPACE);

        matcher.addURI(authority, GsanaContract.PATH_USER, USER);
        matcher.addURI(authority, GsanaContract.PATH_USER + "/#", USER_ID);

        return matcher;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "workspace"
            case WORKSPACE:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        GsanaContract.WorkspaceEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            // "workspace/*"
            case WORKSPACE_ID:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        GsanaContract.WorkspaceEntry.TABLE_NAME,
                        projection,
                        GsanaContract.WorkspaceEntry.COLUMN_WORKSPACE_ID + " = '" + ContentUris.parseId(uri) + "'",
                        null,
                        null,
                        null,
                        sortOrder);
                break;
            }
            // "project"
            case PROJECT:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        GsanaContract.ProjectEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            // "project/*"
            case PROJECT_ID:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                    GsanaContract.ProjectEntry.TABLE_NAME,
                        projection,
                        GsanaContract.ProjectEntry.COLUMN_PROJECT_ID + " = '" + ContentUris.parseId(uri) + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "task"
            case TASK:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        GsanaContract.TaskEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            // "task/*"
            case TASK_ID:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        GsanaContract.TaskEntry.TABLE_NAME,
                        projection,
                        GsanaContract.TaskEntry.COLUMN_TASK_ID + " = '" + ContentUris.parseId(uri) + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "user"
            case USER:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        GsanaContract.UserEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            // "user/*
            case USER_ID:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        GsanaContract.UserEntry.TABLE_NAME,
                        projection,
                        GsanaContract.UserEntry.COLUMN_USER_ID + " = '" + ContentUris.parseId(uri) + "'",
                        null,
                        null,
                        null,
                        sortOrder);
                break;
            }
            default: throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case WORKSPACE_ID:
                return GsanaContract.WorkspaceEntry.CONTENT_ITEM_TYPE;
            case WORKSPACE:
                return GsanaContract.WorkspaceEntry.CONTENT_TYPE;
            case PROJECT_ID:
                return GsanaContract.ProjectEntry.CONTENT_ITEM_TYPE;
            case PROJECT:
                return GsanaContract.ProjectEntry.CONTENT_TYPE;
            case TASK_ID:
                return GsanaContract.TaskEntry.CONTENT_ITEM_TYPE;
            case TASK:
                return GsanaContract.TaskEntry.CONTENT_TYPE;
            case USER_ID:
                return GsanaContract.UserEntry.CONTENT_ITEM_TYPE;
            case USER:
                return GsanaContract.UserEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case WORKSPACE: {
                long _id = db.insert(GsanaContract.WorkspaceEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = GsanaContract.WorkspaceEntry.buildWorkspaceUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case USER: {
                long userId = values.getAsLong("user_id");
                long _id = db.insert(GsanaContract.UserEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = GsanaContract.UserEntry.buildUserUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TASK: {
                long _id = db.insert(GsanaContract.TaskEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = GsanaContract.TaskEntry.buildTaskUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case PROJECT: {
                long _id = db.insert(GsanaContract.ProjectEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = GsanaContract.ProjectEntry.buildProjectUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Log.d(LOG_TAG, uri.toString() + " " + match);
        int rowsDeleted;
        switch (match) {
            case WORKSPACE:
                rowsDeleted = db.delete(
                        GsanaContract.WorkspaceEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TASK:
                rowsDeleted = db.delete(
                        GsanaContract.TaskEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case USER:
                rowsDeleted = db.delete(
                        GsanaContract.UserEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PROJECT:
                rowsDeleted = db.delete(
                        GsanaContract.ProjectEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case WORKSPACE:
                rowsUpdated = db.update(GsanaContract.WorkspaceEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case PROJECT:
                rowsUpdated = db.update(GsanaContract.ProjectEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case TASK:
                rowsUpdated = db.update(GsanaContract.TaskEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case USER:
            rowsUpdated = db.update(GsanaContract.TaskEntry.TABLE_NAME, values, selection,
                    selectionArgs);
            break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        switch (match) {
            case WORKSPACE:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(GsanaContract.WorkspaceEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case PROJECT:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(GsanaContract.ProjectEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case TASK:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(GsanaContract.TaskEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case USER:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(GsanaContract.UserEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
