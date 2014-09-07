package com.geaden.android.gsana.app.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Main Gsana content provider
 */
public class GsanaProvider extends ContentProvider {
    private GsanaDbHelper mOpenHelper;

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

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings2, String s2) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
