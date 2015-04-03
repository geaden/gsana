package com.geaden.android.gsana.app.fragments;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.geaden.android.gsana.app.LoadersColumns;
import com.geaden.android.gsana.app.R;
import com.geaden.android.gsana.app.Utility;
import com.geaden.android.gsana.app.api.AsanaApi2;
import com.geaden.android.gsana.app.api.AsanaCallback;
import com.geaden.android.gsana.app.data.GsanaContract;
import com.geaden.android.gsana.app.data.GsanaProvider;
import com.geaden.android.gsana.app.models.AsanaProject;
import com.geaden.android.gsana.app.models.AsanaTask;
import com.geaden.android.gsana.app.models.AsanaUser;
import com.geaden.android.gsana.app.models.AsanaWorkspace;
import com.geaden.android.gsana.app.sync.GsanaSyncAdapter;

import java.util.ArrayList;
import java.util.List;

/**
* Task Create Fragment.
*
* @author Gennady Denisov
*/
public class TaskCreateFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private final String LOG_TAG = getClass().getSimpleName();

    private final int ASANA_WORKSPACES_LOADER = 0;
    private final int ASANA_PROJECTS_LOADER = 1;

    private long mSelectedWorkspaceId = 0L;
    private SimpleCursorAdapter mWorkspacesAdapter;
    private SimpleCursorAdapter mProjectsAdapter;
    private Spinner mWorkspacesSpinner;
    private Spinner mProjectsSpinner;
    private long mSelectedProjectId;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_task_new, container, false);
        Button button = (Button) rootView.findViewById(R.id.save_task_button);
        mWorkspacesSpinner = (Spinner) rootView.findViewById(R.id.spinner_workspaces);
        mWorkspacesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = mWorkspacesAdapter.getCursor();
                if (null != cursor) {
                    mSelectedWorkspaceId = cursor.getLong(LoadersColumns.COL_WORKSPACE_ID);
                    mSelectedProjectId = 0L;
                }

                getLoaderManager().restartLoader(ASANA_PROJECTS_LOADER, null, TaskCreateFragment.this);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mProjectsSpinner = (Spinner) rootView.findViewById(R.id.spinner_projects);
        mProjectsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = mProjectsAdapter.getCursor();
                if (null != cursor) {
                    mSelectedProjectId = cursor.getLong(LoadersColumns.COL_PROJECT_ID);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mWorkspacesAdapter = new SimpleCursorAdapter(getActivity(), R.layout.spinner_item,
                null, new String[]{GsanaContract.WorkspaceEntry.COLUMN_WORKSPACE_NAME}, new int[]{R.id.spinner_text}, 0);
        mProjectsAdapter = new SimpleCursorAdapter(getActivity(), R.layout.spinner_item,
                null, new String[]{GsanaContract.ProjectEntry.COLUMN_PROJECT_NAME}, new int[]{R.id.spinner_text}, 0);
        mWorkspacesSpinner.setAdapter(mWorkspacesAdapter);
        mProjectsSpinner.setAdapter(mProjectsAdapter);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView taskName = (TextView) rootView.findViewById(R.id.task_name);
                if (taskName.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "Task name is required.", Toast.LENGTH_SHORT).show();
                    return;
                }
                TextView taskNotes = (TextView) rootView.findViewById(R.id.task_description);
                AsanaTask task = new AsanaTask();
                task.setName(taskName.getText().toString());
                task.setNotes(taskNotes.getText().toString());
                AsanaWorkspace workspace = new AsanaWorkspace();
                workspace.setId(mSelectedWorkspaceId);
                task.setWorkspace(workspace);
                if (mSelectedProjectId > 0) {
                    AsanaProject project = new AsanaProject();
                    project.setId(mSelectedProjectId);
                    List<AsanaProject> projects = new ArrayList<AsanaProject>();
                    projects.add(project);
                    task.setProjects(projects);
                }
                String currentUserId = Utility.getSettingsStringValue(getActivity(), Utility.CURRENT_USER_KEY);
                if (null == currentUserId) {
                    AsanaUser assignee = new AsanaUser();
                    assignee.setId(Long.valueOf(currentUserId));
                    task.setAssignee(assignee);

                }
                createTask(task);
            }
        });
        return rootView;
    }

    /**
     * Creates task
     * @param task the task object to be created
     */
    private void createTask(AsanaTask task) {
        CreateTaskAsync createTaskAsync = new CreateTaskAsync(task);
        createTaskAsync.execute();
    }

    private class CreateTaskAsync extends AsyncTask<Void, Void, Void> {
        private AsanaTask mTask;

        public CreateTaskAsync(AsanaTask asanaTask) {
            mTask = asanaTask;
        }

        @Override
        protected void onPreExecute() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "Creating new task...", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        protected Void doInBackground(Void... params) {
            AsanaApi2 asanaApi2 = AsanaApi2.getInstance(getActivity());
            asanaApi2.createTask(mTask, new AsanaCallback<AsanaTask>() {
                @Override
                public void onResult(AsanaTask value) {
                    ContentValues taskValues = GsanaSyncAdapter.getTaskValues(value);
                    getActivity().getContentResolver().insert(GsanaContract.TaskEntry.CONTENT_URI,
                            taskValues);
                }

                @Override
                public void onError(Throwable exception) {
                    Log.e(LOG_TAG, "Error creating task", exception);
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "Task created!", Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
            });
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(ASANA_WORKSPACES_LOADER, null, this);
        getLoaderManager().initLoader(ASANA_PROJECTS_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = null;
        switch (id) {
            case ASANA_WORKSPACES_LOADER:
                cursorLoader = new CursorLoader(
                        getActivity(),
                        GsanaContract.WorkspaceEntry.CONTENT_URI,
                        LoadersColumns.ASANA_WORKSPACES_COLUMNS,
                        null,
                        null,
                        null);
                break;
            case ASANA_PROJECTS_LOADER:
                String selection = null;
                String[] selectionArgs = null;
                selection = GsanaContract.ProjectEntry.COLUMN_PROJECT_WORKSPACE_ID +
                            " = ?";
                selectionArgs = new String[]{String.valueOf(mSelectedWorkspaceId)};
                cursorLoader = new CursorLoader(
                        getActivity(),
                        GsanaContract.ProjectEntry.CONTENT_URI,
                        LoadersColumns.ASANA_PROJECTS_COLUMNS,
                        selection,
                        selectionArgs,
                        null
                );
                break;
            default:
                break;
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case ASANA_WORKSPACES_LOADER:
                mWorkspacesAdapter.swapCursor(data);
                break;
            case ASANA_PROJECTS_LOADER:
                mProjectsAdapter.swapCursor(data);
                break;
            default: break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mWorkspacesAdapter.swapCursor(null);
        mProjectsAdapter.swapCursor(null);
    }
}
