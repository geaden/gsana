package com.geaden.android.gsana.app;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.geaden.android.gsana.app.data.GsanaContract;
import com.geaden.android.gsana.app.data.GsanaContract.WorkspaceEntry;
import com.geaden.android.gsana.app.data.GsanaContract.TaskEntry;
import com.geaden.android.gsana.app.data.GsanaContract.ProjectEntry;
import com.geaden.android.gsana.app.models.AsanaWorkspace;
import com.geaden.android.gsana.app.sync.GsanaSyncAdapter;
import com.melnykov.fab.FloatingActionButton;

/**
 * Task list fragment.
 */
public class TaskListFragment extends Fragment implements LoaderCallbacks<Cursor> {
    private final String LOG_TAG = getClass().getSimpleName();

    private GsanaTasksAdapter mTasksAdapter;
    private GsanaProjectsAdapter mProjectsAdapter;

    private ListView mTaskListView;
    private int mPosition = ListView.INVALID_POSITION;

    private String mCurrentWorkspace = "";

    private TextView mTasksForToday;

    private static final String SELECTED_KEY = "selected_position";

    private static final int ASANA_WORKSPACES_LOADER = 0;
    private static final int ASANA_TASK_LOADER = 1;
    private static final int ASANA_PROJECTS_LOADER = 2;
    private static final int ASANA_USER_LOADER = 3;

    private FloatingActionButton mFabButton;

    // Specify the order of columns for workspaces
    private static final String[] ASANA_WORKSPACES_COLUMNS = {
            WorkspaceEntry.TABLE_NAME + "." + WorkspaceEntry._ID,
            WorkspaceEntry.COLUMN_WORKSPACE_ID,
            WorkspaceEntry.COLUMN_WORKSPACE_NAME
    };

    // The indices that correspond to ASANA_WORKSPACES_COLUMNS
    public static final int COL_WORKSPACE_ID = 1;
    public static final int COL_WORKSPACE_NAME = 2;

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

    // Specify the order of columns for projects
    private static final String[] ASANA_PROJECTS_COLUMNS = {
        ProjectEntry.TABLE_NAME + "." + ProjectEntry._ID,
        ProjectEntry.COLUMN_PROJECT_ID,
        ProjectEntry.COLUMN_PROJECT_NAME,
        ProjectEntry.COLUMN_PROJECT_COLOR
    };

    // The indices that correspond to ASANA_PROJECT_COLUMNS
    public static final int COL_PROJECT_ID = 1;
    public static final int COL_PROJECT_NAME = 2;
    public static final int COL_PROJECT_COLOR = 3;

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


    private static int mLastFirstVisibleItem;
    private static boolean mIsScrollingUp;

    public TaskListFragment() {
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * TaskDetailFragment for when an item has been selected.
         */
        public void onItemSelected(String taskId);

        /**
         * Propagate project values
         */
        public void bindValues(CursorAdapter cursorAdapter, Cursor cursor);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // The ArrayAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mTasksAdapter = new GsanaTasksAdapter(getActivity(), null, 0);
        mProjectsAdapter = new GsanaProjectsAdapter(getActivity(), null, 0);

        // TODO: set custom view binder
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mTaskListView = (ListView) rootView.findViewById(R.id.listview_asana_tasks);
        mTaskListView.setAdapter(mTasksAdapter);

        mFabButton = (FloatingActionButton) rootView
                .findViewById(R.id.button_floating_action);
        mFabButton.attachToListView(mTaskListView);

        mFabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), TaskCreateActivity.class);
                startActivity(intent);
            }
        });

        String greetingTemplate = "Good %s, %s";

        TextView greetingTextView = (TextView) rootView.findViewById(R.id.greeting);
        greetingTextView.setText(String.format(greetingTemplate, Utility.getTimeOfTheDay(), "Gennady"));

        mTasksForToday = (TextView) rootView.findViewById(R.id.today_tasks);

        mTaskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = mTasksAdapter.getCursor();

                if (cursor != null && cursor.moveToPosition(position)) {
                    Log.v(LOG_TAG, "Task Id: " + cursor.getString(COL_TASK_ID));
                    ((Callback) getActivity())
                            .onItemSelected(cursor.getString(COL_TASK_ID));
                }
                mPosition = position;
            }
        });
        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(ASANA_WORKSPACES_LOADER, null, this);
        getLoaderManager().initLoader(ASANA_TASK_LOADER, null, this);
        getLoaderManager().initLoader(ASANA_PROJECTS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        CursorLoader cursorLoader;
        // This is called when a new Loader needs to be created.  This
        switch (i) {
            case ASANA_TASK_LOADER:
                cursorLoader = new CursorLoader(
                        getActivity(),
                        TaskEntry.CONTENT_URI,
                        ASANA_TASK_COLUMNS,
                        TaskEntry.COLUMN_TASK_WORKSPACE_ID + " = ?",
                        new String[]{mCurrentWorkspace},
                        null);
                break;
            case ASANA_PROJECTS_LOADER:
                cursorLoader = new CursorLoader(
                        getActivity(),
                        ProjectEntry.CONTENT_URI,
                        ASANA_PROJECTS_COLUMNS,
                        ProjectEntry.COLUMN_PROJECT_WORKSPACE_ID + " = ?",
                        new String[]{mCurrentWorkspace},
                        null);
                break;
            case ASANA_WORKSPACES_LOADER:
                cursorLoader = new CursorLoader(
                        getActivity(),
                        WorkspaceEntry.CONTENT_URI,
                        ASANA_WORKSPACES_COLUMNS,
                        null,
                        null,
                        null);
                break;
            case ASANA_USER_LOADER:
            default:
                cursorLoader = null;
        }
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return cursorLoader;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(ASANA_WORKSPACES_LOADER, null, this);
        getLoaderManager().restartLoader(ASANA_TASK_LOADER, null, this);
        getLoaderManager().restartLoader(ASANA_PROJECTS_LOADER, null, this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor data) {
        switch (cursorLoader.getId()) {
            case ASANA_PROJECTS_LOADER:
                // Propagate values to project loader
                mProjectsAdapter.swapCursor(data);
                ((Callback) getActivity()).bindValues(mProjectsAdapter, data);
                break;
            case ASANA_TASK_LOADER:
                mTasksAdapter.swapCursor(data);
                int tasksForToday = data.getCount();
                mTasksForToday.setText(String.format("You have %s %s for today", tasksForToday,
                        tasksForToday == 1 ? "task" : "tasks"));
                break;
            case ASANA_WORKSPACES_LOADER:
                if (data.moveToFirst()) {
                    mCurrentWorkspace = data.getString(COL_WORKSPACE_ID);
                }
                break;
            default:
                Log.d(LOG_TAG, "Loader: " + cursorLoader.getId());
        }
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mTaskListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mTasksAdapter.swapCursor(null);
        mProjectsAdapter.swapCursor(null);
    }
}