package com.geaden.android.gsana.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.geaden.android.gsana.app.data.GsanaContract.TaskEntry;

/**
 * Task detail fragment.
 */
public class TaskDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private final String LOG_TAG = TaskDetailFragment.class.getSimpleName();

    private static final int TASK_DETAIL_LOADER = 0;

    private ShareActionProvider mShareActionProvider;

    private static final String ASANA_TASK_KEY = "task";

    private static final String GSANA_SHARE_HASHTAG = " #YetAnotherAndroidClientForAsana";

    private String mTaskId;
    private String mShareTask;

    private final String TRUE = "true";
    private final String FALSE = "false";

    private TextView mAssigneeTextView;
    private TextView mTaskNameTextView;
    private CheckBox mTaskCompletedView;
    private TextView mTaskNotesTextView;

    // Specify the order of columns for tasks
    private static final String[] ASANA_TASK_COLUMNS = {
            TaskEntry.TABLE_NAME + "." + TaskEntry._ID,
            TaskEntry.COLUMN_TASK_ID,
            TaskEntry.COLUMN_TASK_NAME,
            TaskEntry.COLUMN_TASK_NOTES,
            TaskEntry.COLUMN_TASK_COMPLETED,
            TaskEntry.COLUMN_TASK_COMPLETED_AT,
            TaskEntry.COLUMN_TASK_CREATED_AT,
            TaskEntry.COLUMN_TASK_PROJECT_ID,
            TaskEntry.COLUMN_TASK_WORKSPACE_ID,
            TaskEntry.COLUMN_TASK_ASSIGNEE_ID,
            TaskEntry.COLUMN_TASK_ASSIGNEE_STATUS,
            TaskEntry.COLUMN_TASK_DUE_ON,
            TaskEntry.COLUMN_TASK_MODIFIED_AT,
            TaskEntry.COLUMN_TASK_PARENT_ID
    };

    public TaskDetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(ASANA_TASK_KEY, mTaskId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.taskdetailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mShareTask != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mShareTask + GSANA_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        Log.v(LOG_TAG, "Arguments: " + arguments);
        if (arguments != null) {
            mTaskId = arguments.getString(TaskDetailActivity.TASK_KEY);
            Log.v(LOG_TAG, "Task ID: " + mTaskId);
        }

        String accessToken = Utility.getAccessToken(getActivity());
        if (accessToken == null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            return null;
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mAssigneeTextView = (TextView) rootView.findViewById(R.id.asana_task_assignee);
        mTaskNameTextView = (TextView) rootView.findViewById(R.id.asana_task_name);
        mTaskCompletedView = (CheckBox) rootView.findViewById(R.id.asana_task_compeleted);
        mTaskNotesTextView = (TextView) rootView.findViewById(R.id.asana_task_description);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mTaskId = savedInstanceState.getString(ASANA_TASK_KEY);
        }

        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(TaskDetailActivity.TASK_KEY)) {
            getLoaderManager().initLoader(TASK_DETAIL_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle args) {
        Uri taskByIdUri = TaskEntry.buildTaskUri(Long.parseLong(mTaskId));
        return new CursorLoader(
                getActivity(),
                taskByIdUri,
                ASANA_TASK_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor data) {
        Log.v(LOG_TAG, "Cursor " + data);
        if (data != null && data.moveToFirst()) {
            Log.v(LOG_TAG, "Loading data");
            String taskName = data.getString(data.getColumnIndex(TaskEntry.COLUMN_TASK_NAME));
            mTaskNameTextView.setText(taskName);

            String taskCompleted = data.getString(data.getColumnIndex(TaskEntry.COLUMN_TASK_COMPLETED));
            if (taskCompleted != null && !taskCompleted.equals(FALSE)) {
                mTaskCompletedView.setChecked(true);
            } else {
                mTaskCompletedView.setChecked(false);
            }

            String taskNotes = data.getString(data.getColumnIndex(TaskEntry.COLUMN_TASK_NOTES));
            mTaskNotesTextView.setText(taskNotes);

            // TODO: Populate assignee data
            //String taskAssignee = data.getString(data.getColumnIndex(TaskEntry.COLUMN_TASK_ASSIGNEE_ID))

            // Share intent task string
            mShareTask = String.format("%s - %s - %s ", taskName, taskCompleted, taskNotes);

            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(TaskDetailActivity.TASK_KEY)) {
            getLoaderManager().restartLoader(TASK_DETAIL_LOADER, null, this);
        }
    }
}