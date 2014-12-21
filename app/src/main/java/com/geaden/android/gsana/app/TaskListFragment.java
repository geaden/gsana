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
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.geaden.android.gsana.app.data.GsanaContract.TaskEntry;
import com.geaden.android.gsana.app.data.GsanaContract.ProjectEntry;
import com.geaden.android.gsana.app.sync.GsanaSyncAdapter;
import com.melnykov.fab.FloatingActionButton;

/**
 * Task list fragment.
 */
public class TaskListFragment extends Fragment implements LoaderCallbacks<Cursor> {
    private final String LOG_TAG = getClass().getSimpleName();

    private GsanaTasksAdapter mAsanaAdapter;
    private ListView mListView;

    private int mPosition = ListView.INVALID_POSITION;

    private static final String SELECTED_KEY = "selected_position";

    private static final int ASANA_TASK_LOADER = 0;
    private static final int ASANA_USER_LOADER = 1;
    private static final int ASANA_PROJECTS_LOADER = 2;
    private static final int ASANA_WORKSPACES_LOADER = 3;

    private FloatingActionButton mFabButton;

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

    // Specify the order of columns for tasks
    private static final String[] ASANA_PROJECTS_COLUMNS = {
        ProjectEntry.TABLE_NAME + "." + ProjectEntry._ID,
        ProjectEntry.COLUMN_PROJECT_ID,
        ProjectEntry.COLUMN_PROJECT_NAME,
        ProjectEntry.COLUMN_PROJECT_COLOR,
    }

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

    protected int getListViewScrollY() {
        View topChild = mListView.getChildAt(0);
        return topChild == null ? 0 : mListView.getFirstVisiblePosition() * topChild.getHeight() -
                topChild.getTop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // The ArrayAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mAsanaAdapter = new GsanaTasksAdapter(getActivity(), null, 0);

        // TODO: set custom view binder
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mListView = (ListView) rootView.findViewById(R.id.listview_asana);
        mListView.setAdapter(mAsanaAdapter);

        mFabButton = (FloatingActionButton) rootView
                .findViewById(R.id.button_floating_action);
        mFabButton.attachToListView(mListView);

        mFabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), TaskCreateActivity.class);
                startActivity(intent);
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = mAsanaAdapter.getCursor();

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
        getLoaderManager().initLoader(ASANA_TASK_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private void getTasks() {
        FetchAsanaTask task = new FetchAsanaTask(getActivity());
        task.execute(getAccessToken());
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
                        null,
                        null,
                        null);
                break;
            case ASANA_PROJECTS_LOADER:
                cursorLoader = new CursorLoader(
                        getActivity(),
                        ProjectEntry.CONTENT_URI,
                        ASANA_TASK_COLUMNS,
                        null,
                        null,
                        null);
            case ASANA_WORKSPACES_LOADER:
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
        getLoaderManager().restartLoader(ASANA_TASK_LOADER, null, this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor data) {
        if (data.getCount() == 0) {
            GsanaSyncAdapter.syncImmediately(getActivity());
        }
        mAsanaAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAsanaAdapter.swapCursor(null);
    }
}