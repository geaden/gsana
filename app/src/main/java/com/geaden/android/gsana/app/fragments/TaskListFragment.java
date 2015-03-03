package com.geaden.android.gsana.app.fragments;

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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.geaden.android.gsana.app.adapters.GsanaProjectsAdapter;
import com.geaden.android.gsana.app.adapters.GsanaTasksAdapter;
import com.geaden.android.gsana.app.LoadersColumns;
import com.geaden.android.gsana.app.MainActivity;
import com.geaden.android.gsana.app.R;
import com.geaden.android.gsana.app.TaskCreateActivity;
import com.geaden.android.gsana.app.Utility;
import com.geaden.android.gsana.app.data.GsanaContract.TaskEntry;

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
    private TextView mGreetingTextView;

    private ToggleButton mStartTimerButton;

    private static final String SELECTED_KEY = "selected_position";

    private static final int ASANA_TASK_LOADER = 0;

    private FloatingActionButton mFabButton;

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

        mGreetingTextView = (TextView) rootView.findViewById(R.id.greeting);

        updateGreetingsTextView();

        mTasksForToday = (TextView) rootView.findViewById(R.id.today_tasks);

        mTaskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = mTasksAdapter.getCursor();

                if (cursor != null && cursor.moveToPosition(position)) {
                    Log.v(LOG_TAG, "Task Id: " + cursor.getString(LoadersColumns.COL_TASK_ID));
                    ((Callback) getActivity())
                            .onItemSelected(cursor.getString(LoadersColumns.COL_TASK_ID));
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

    /**
     * Updates greetings text
     */
    private void updateGreetingsTextView() {
        if (mGreetingTextView == null) {
            return;
        }
        String greetingTemplate = getString(R.string.greeting_template);
        mGreetingTextView.setText(String.format(greetingTemplate, Utility.getTimeOfTheDay(),
                MainActivity.sCurrentUser));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(ASANA_TASK_LOADER, null, this);
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
                        LoadersColumns.ASANA_TASK_COLUMNS,
                        null, //TaskEntry.COLUMN_TASK_WORKSPACE_ID + " = ?",
                        null, //new String[]{mCurrentWorkspace},
                        null);
                break;
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
        switch (cursorLoader.getId()) {
            case ASANA_TASK_LOADER:
                mTasksAdapter.swapCursor(data);
                int tasksForToday = data.getCount();
                mTasksForToday.setText(String.format("You have %s %s for today", tasksForToday,
                        tasksForToday == 1 ? "task" : "tasks"));
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