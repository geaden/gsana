package com.geaden.android.gsana.app;

import com.geaden.android.gsana.app.data.GsanaContract;

/**
 * Loaders Columns
 */
public class LoadersColumns {
    // Specify the order of columns for workspaces
    public static final String[] ASANA_WORKSPACES_COLUMNS = {
            GsanaContract.WorkspaceEntry.TABLE_NAME + "." + GsanaContract.WorkspaceEntry._ID,
            GsanaContract.WorkspaceEntry.COLUMN_WORKSPACE_ID,
            GsanaContract.WorkspaceEntry.COLUMN_WORKSPACE_NAME
    };

    // The indices that correspond to ASANA_WORKSPACES_COLUMNS
    public static final int COL_WORKSPACE_ID = 1;
    public static final int COL_WORKSPACE_NAME = 2;

    // Specify the order of columns for tasks
    public static final String[] ASANA_TASK_COLUMNS = {
            GsanaContract.TaskEntry.TABLE_NAME + "." + GsanaContract.TaskEntry._ID,
            GsanaContract.TaskEntry.COLUMN_TASK_ID,
            GsanaContract.TaskEntry.COLUMN_TASK_NAME,
            GsanaContract.TaskEntry.COLUMN_TASK_NOTES,
            GsanaContract.TaskEntry.COLUMN_TASK_COMPLETED,
            GsanaContract.TaskEntry.COLUMN_TASK_COMPLETED_AT,
            GsanaContract.TaskEntry.COLUMN_TASK_CREATED_AT,
            GsanaContract.TaskEntry.COLUMN_TASK_PROJECT_ID,
            GsanaContract.ProjectEntry.TABLE_NAME + "." + GsanaContract.ProjectEntry.COLUMN_PROJECT_COLOR,
            GsanaContract.TaskEntry.COLUMN_TASK_WORKSPACE_ID,
            GsanaContract.TaskEntry.COLUMN_TASK_ASSIGNEE_ID,
            GsanaContract.TaskEntry.COLUMN_TASK_ASSIGNEE_STATUS,
            GsanaContract.TaskEntry.COLUMN_TASK_DUE_ON,
            GsanaContract.TaskEntry.COLUMN_TASK_MODIFIED_AT,
            GsanaContract.TaskEntry.COLUMN_TASK_PARENT_ID,
            GsanaContract.TaskEntry.COLUMN_TOGGL_ENTRY_ID,
            GsanaContract.TaskEntry.COLUMN_TOGGL_START_DATE,
            GsanaContract.TaskEntry.COLUMN_TOGGL_END_DATE,
            GsanaContract.ProjectEntry.TABLE_NAME + "." + GsanaContract.ProjectEntry.COLUMN_PROJECT_NAME,
            GsanaContract.TaskEntry.COLUMN_TOGGL_DURATION
    };

    // The indices that correspond to ASANA_TASK_COLUMNS
    public static final int COL_TASK_ID = 1;
    public static final int COL_TASK_NAME = 2;
    public static final int COL_TASK_NOTES = 3;
    public static final int COL_TASK_COMPLETED = 4;
    public static final int COL_TASK_COMPLETED_AT = 5;
    public static final int COL_TASK_CREATED_AT = 6;
    public static final int COL_TASK_PROJECT_ID = 7;
    public static final int COL_TASK_PROJECT_COLOR = 8;
    public static final int COL_TASK_WORKSPACE_ID = 9;
    public static final int COL_TASK_ASSIGNEE_ID = 10;
    public static final int COL_TASK_ASSIGNEE_STATUS = 11;
    public static final int COL_TASK_DUE_ON = 12;
    public static final int COL_TASK_MODIFIED_AT = 13;
    public static final int COL_TASK_PARENT_ID = 14;
    public static final int COL_TASK_TOGGL_ENTRY_ID = 15;
    public static final int COL_TASK_TOGGL_START_DATE = 16;
    public static final int COL_TASK_TOGGL_END_DATE = 17;
    public static final int COL_TASK_PROJECT_NAME = 18;
    public static final int COL_TASK_TOGGL_DURATION = 19;

    // Specify the order of columns for projects
    public static final String[] ASANA_PROJECTS_COLUMNS = {
            GsanaContract.ProjectEntry.TABLE_NAME + "." + GsanaContract.ProjectEntry._ID,
            GsanaContract.ProjectEntry.COLUMN_PROJECT_ID,
            GsanaContract.ProjectEntry.COLUMN_PROJECT_NAME,
            GsanaContract.ProjectEntry.COLUMN_PROJECT_COLOR
    };

    // The indices that correspond to ASANA_PROJECT_COLUMNS
    public static final int COL_PROJECT_ID = 1;
    public static final int COL_PROJECT_NAME = 2;
    public static final int COL_PROJECT_COLOR = 3;

    // The order of columns for user data.
    public static final String[] ASANA_USER_COLUMNS = {
            GsanaContract.UserEntry.TABLE_NAME + "." + GsanaContract.UserEntry._ID,
            GsanaContract.UserEntry.COLUMN_USER_ID,
            GsanaContract.UserEntry.COLUMN_USER_NAME,
            GsanaContract.UserEntry.COLUMN_USER_EMAIL,
            GsanaContract.UserEntry.COLUMN_USER_PHOTO_URL_60,
            GsanaContract.UserEntry.COLUMN_USER_PHOTO_URL_128,
            GsanaContract.UserEntry.COLUMN_USER_PHOTO
    };

    // The indices that correspond to ASANA_USER_COLUMNS
    public static final int COL_USER_ID = 1;
    public static final int COL_USER_NAME = 2;
    public static final int COL_USER_EMAIL = 3;
    public static final int COL_USER_PHOTO_60 = 4;
    public static final int COL_USER_PHOTO_128 = 5;
    public static final int COL_USER_PHOTO = 6;
}
