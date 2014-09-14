package com.geaden.android.gsana.app;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.geaden.android.gsana.app.data.GsanaContract.TaskEntry;

/**
 * Task list fragment.
 */
public class TaskListFragment extends Fragment implements LoaderCallbacks<Cursor> {
    private final String LOG_TAG = getClass().getSimpleName();

    private SimpleCursorAdapter mAsanaAdapter;

    private static final int ASANA_TASK_LOADER = 0;

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

    // The indices that correspond to ASANA_TASK_COLUMNS
    public static final int COL_TASK_ID = 1;
    public static final int COL_TASK_NAME = 2;
    public static final int COL_TASK_NOTES = 3;
    public static final int COL_TASK_COMPLETED = 4;
    public static final int COL_TASK_COMPLETED_AT = 5;
    public static final int COL_TASK_CREATED = 6;
    public static final int COL_TASK_CREATED_AT = 7;
    public static final int COL_TASK_PROJECT_ID = 8;
    public static final int COL_TASK_WORKSPACE_ID = 9;
    public static final int COL_TASK_ASSIGNEE_ID = 10;
    public static final int COL_TASK_ASSIGNEE_STATUS = 11;
    public static final int COL_TASK_DUE_ON = 12;
    public static final int COL_TASK_MODIFIED_AT = 13;
    public static final int COL_TASK_PARENT_ID = 14;

    public TaskListFragment() {
    }

    /**
     * Create new instance of tasks fragment with access token
     * @param accessToken Asana access token
     * @return new instance of fragment
     */
    public static TaskListFragment newInstance(String accessToken) {
        TaskListFragment taskListFragment = new TaskListFragment();
        Bundle args = new Bundle();
        args.putString(MainActivity.ACCESS_TOKEN_KEY, accessToken);
        taskListFragment.setArguments(args);
        return taskListFragment;
    }

    public String getAccessToken() {
        return getArguments().getString(MainActivity.ACCESS_TOKEN_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Create some dummy data for the ListView.  Here's a sample weekly forecast
        String[] data = {
                "Finish Mockups - Final Project - Today",
                "Pay Bills - Householding - Today",
                "Finish Summary - Final Project - Aug 10",
                "Fix CSS - Website - Aug 26"
        };
        List<String> userTasks = new ArrayList<String>(Arrays.asList(data));
        // The SimpleCursorAdapter will take data from the database through the
        // Loader and use it to populate the ListView it's attached to.
        mAsanaAdapter =
                new SimpleCursorAdapter(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_asana, // The name of the layout ID.
                        null,
                        // The column names to use to fill the textviews
                        new String[]{
                                TaskEntry.COLUMN_TASK_NAME,
                                TaskEntry.COLUMN_TASK_DUE_ON
                        },
                        // The texviews to fill with the data pulled from the columns above
                        new int[] {
                                R.id.list_item_asana_task_name,
                                R.id.list_item_asana_task_due_on
                        }, 0);

        // TODO: set custom view binder
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        FloatingActionButton fabButton = new FloatingActionButton.Builder(getActivity())
                .withDrawable(getResources().getDrawable(R.drawable.ic_content_new))
                .withButtonColor(Color.GREEN)
                .withGravity(Gravity.BOTTOM | Gravity.CENTER)
                .withMargins(0, 0, 0, 16)
                .create();

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), TaskCreateActivity.class);
                startActivity(intent);
            }
        });

        ListView listView = (ListView) rootView.findViewById(R.id.listview_asana);
        listView.setAdapter(mAsanaAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = mAsanaAdapter.getCursor();

                if (cursor != null && cursor.moveToPosition(position)) {
                    final String DELIMITER = "\n";
                    String taskName = cursor.getString(COL_TASK_NAME);
                    String taskNotes = cursor.getString(COL_TASK_NOTES);
                    String taskCompleted = cursor.getString(COL_TASK_COMPLETED);
                    String taskCompletedAt = cursor.getString(COL_TASK_DUE_ON);
                    String taskModifiedAt = cursor.getString(COL_TASK_MODIFIED_AT);
                    String detailString = "Task name: " + taskName + DELIMITER +
                            "Task notes: " + taskNotes + DELIMITER +
                            "Task completed: " + taskCompleted + DELIMITER +
                            "Task completed at: " + taskCompletedAt + DELIMITER +
                            "Task modified at: " + taskModifiedAt + DELIMITER;
                    Intent intent = new Intent(getActivity(), TaskDetailActivity.class)
                            .putExtra(Intent.EXTRA_TEXT, detailString);
                    startActivity(intent);
                }
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(ASANA_TASK_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private void getTasks() {
        FetchAsanaTask task = new FetchAsanaTask(getActivity());
        task.execute(getAccessToken());
    }

    @Override
    public void onStart() {
        super.onStart();
        getTasks();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, get the String representation for today,
        // and filter the query to return weather only for dates after or including today.
        // Only return data after today.

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                TaskEntry.CONTENT_URI,
                ASANA_TASK_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor data) {
        mAsanaAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAsanaAdapter.swapCursor(null);
    }
}