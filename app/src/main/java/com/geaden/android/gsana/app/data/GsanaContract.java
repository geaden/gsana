package com.geaden.android.gsana.app.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the database
 */
public class GsanaContract {
    // Name for content provider
    public static final String CONTENT_AUTHORITY = "com.geaden.android.gsana.app";

    // URI to contact the content provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths appended to to base content URI
    public static final String PATH_WORKSPACE = "workspace";
    public static final String PATH_PROJECT = "project";
    public static final String PATH_TASK = "task";
    public static final String PATH_USER = "user";

    /**
     * Inner class that defines table contents of the Workspace table
     */
    public static final class WorkspaceEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_WORKSPACE).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_WORKSPACE;

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_WORKSPACE;

        // Table name
        public static final String TABLE_NAME = "workspace";

        // Workspace id column name
        public static final String COLUMN_WORKSPACE_ID = "workspace_id";

        // Human readable workspace name column name
        public static final String COLUMN_WORKSPACE_NAME = "workspace_name";

        public static Uri buildWorkspaceUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /**
     * Inner class that defines content of Project table
     */
    public static class ProjectEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PROJECT).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_PROJECT;

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_PROJECT;

        // Table name
        public static final String TABLE_NAME = "project";

        // Project id column name
        public static final String COLUMN_PROJECT_ID = "project_id";

        // Project archived state column name
        public static final String COLUMN_PROJECT_ARCHIVED = "project_archived";

        // Project creation time column name
        public static final String COLUMN_PROJECT_CREATED_AT = "project_created_at";

        // Project modified at column name
        public static final String COLUMN_PROJECT_MODIFIED_AT = "project_modified_at";

        // Project name column name
        public static final String COLUMN_PROJECT_NAME = "project_name";

        // Project color column name
        public static final String COLUMN_PROJECT_COLOR = "project_color";

        // Project notes column name
        public static final String COLUMN_PROJECT_NOTES = "project_notes";

        // Project workspace column name
        public static final String COLUMN_PROJECT_WORKSPACE_ID = "project_workspace_id";

        public static Uri buildProjectUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /**
     * Inner class that defines content of Task table
     */
    public static class TaskEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TASK).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_TASK;

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_TASK;

        // Table name
        public static final String TABLE_NAME = "task";

        // Task created at column name
        public static final String COLUMN_TASK_CREATED_AT = "task_created_at";

        // Task completion status column name
        public static final String COLUMN_TASK_COMPLETED = "task_completed";

        // Task completion time column name
        public static final String COLUMN_TASK_COMPLETED_AT = "task_completed_at";

        // Task modification time column name
        public static final String COLUMN_TASK_MODIFIED_AT = "task_modified_at";

        // Task id column name
        public static final String COLUMN_TASK_ID = "task_id";

        // Task name column name
        public static final String COLUMN_TASK_NAME = "task_name";

        // Task notes column name
        public static final String COLUMN_TASK_NOTES = "task_notes";

        // Task parent id column name
        public static final String COLUMN_TASK_PARENT_ID = "task_parent_id";

        // Task workspace id column name
        public static final String COLUMN_TASK_WORKSPACE_ID = "task_workspace_id";

        // Task assignee id column name
        public static final String COLUMN_TASK_ASSIGNEE_ID = "task_assignee";

        // Task project id column name
        public static final String COLUMN_TASK_PROJECT_ID = "task_project_id";

        // Task assignee status column name
        public static final String COLUMN_TASK_ASSIGNEE_STATUS = "task_assignee_status";

        // Task due date column name
        public static final String COLUMN_TASK_DUE_ON = "task_due_on";

        public static Uri buildTaskUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /**
     * Inner class that defines content of User table
     */
    public static class UserEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_USER).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_USER;

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" +PATH_USER;

        // Table name
        public static final String TABLE_NAME = "user";

        // User id column name
        public static final String COLUMN_USER_ID = "user_id";

        // User email column name
        public static final String COLUMN_USER_EMAIL = "user_email";

        // User name column name
        public static final String COLUMN_USER_NAME = "user_name";

        // 60x60 photo url of user
        public static final String COLUMN_USER_PHOTO_60 = "user_photo_60";

        public static Uri buildUserUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
