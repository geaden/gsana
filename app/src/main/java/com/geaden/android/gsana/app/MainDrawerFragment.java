package com.geaden.android.gsana.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.LoaderManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.geaden.android.gsana.app.data.GsanaContract;


/**
 * Main Drawer fragment
 */
public class MainDrawerFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private final String LOG_TAG = getClass().getSimpleName();

    // Loader managers order
    private static final int ASANA_WORKSPACES_LOADER = 0;
    private static final int ASANA_PROJECTS_LOADER = 1;
    private static final int ASANA_USER_LOADER = 3;

    // Cursor Adapters
    private GsanaProjectsAdapter mProjectsAdapter;
    private SimpleCursorAdapter mWorkspacesAdapter;

    private long SELECTED_WORKSPACE_ID = 0L;

    // List views
    private ListView mWorkspacesList;
    private ListView mProjectList;

    // User info view holder
    private GsanaUserViewHolder mGsanaUserVH;

    public MainDrawerFragment() {};

    public static MainDrawerFragment newInstance() {
        MainDrawerFragment mainDrawerFragment = new MainDrawerFragment();
        return mainDrawerFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_drawer, container, false);
        mWorkspacesList = (ListView) rootView.findViewById(R.id.left_drawer_workspaces_list);
        mProjectList = (ListView) rootView.findViewById(R.id.left_drawer_projects_list);
        mGsanaUserVH = new GsanaUserViewHolder(rootView);

        // Initialize cursor adapters
        int[] workspacesListItemViews = {R.id.left_drawer_workspaces_list_workspace_name};
        mWorkspacesAdapter = new SimpleCursorAdapter(getActivity(), R.layout.left_drawer_wokspaces_list_item,
                null, new String[]{GsanaContract.WorkspaceEntry.COLUMN_WORKSPACE_NAME}, workspacesListItemViews, 0);
        mProjectsAdapter = new GsanaProjectsAdapter(getActivity(), null, 0);

        // Attach cursor adapters to list views
        mWorkspacesList.setAdapter(mWorkspacesAdapter);
        mProjectList.setAdapter(mProjectsAdapter);

        mWorkspacesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> workspacesAdapterView, View view, int position, long id) {
                Cursor cursor = mWorkspacesAdapter.getCursor();
                if (null != cursor) {
                    SELECTED_WORKSPACE_ID = cursor.getLong(LoadersColumns.COL_WORKSPACE_ID);
                }
                Toast.makeText(getActivity(),
                        "Selected workspace " + SELECTED_WORKSPACE_ID, Toast.LENGTH_SHORT).show();
                ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(
                        cursor.getString(LoadersColumns.COL_WORKSPACE_NAME));
                getLoaderManager().restartLoader(ASANA_PROJECTS_LOADER, null, MainDrawerFragment.this);
            }
        });

        mProjectList.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Cursor cursor = mProjectsAdapter.getCursor();
                if (null != cursor) {
                    // 1. Instantiate an AlertDialog.Builder with its constructor
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    // 2. Chain together various setter methods to set the dialog characteristics
                    builder.setMessage(cursor.getString(LoadersColumns.COL_PROJECT_NAME)).setTitle(
                            cursor.getString(LoadersColumns.COL_PROJECT_NAME));
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    // 3. Get the AlertDialog from create()
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                return true;
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(ASANA_USER_LOADER, null, this);
        getLoaderManager().initLoader(ASANA_WORKSPACES_LOADER, null, this);
        getLoaderManager().initLoader(ASANA_PROJECTS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader;
        switch (id) {
            case ASANA_WORKSPACES_LOADER:
                cursorLoader = new CursorLoader(
                        getActivity(),
                        GsanaContract.WorkspaceEntry.CONTENT_URI,
                        LoadersColumns.ASANA_WORKSPACES_COLUMNS,
                        null,
                        null,
                        null
                );
                break;
            case ASANA_PROJECTS_LOADER:
                String selection = null;
                if (SELECTED_WORKSPACE_ID > 0) {
                    selection = GsanaContract.ProjectEntry.COLUMN_PROJECT_WORKSPACE_ID +
                            " = " + SELECTED_WORKSPACE_ID;
                }
                cursorLoader = new CursorLoader(
                        getActivity(),
                        GsanaContract.ProjectEntry.CONTENT_URI,
                        LoadersColumns.ASANA_PROJECTS_COLUMNS,
                        selection,
                        null,
                        null
                );
                break;
            case ASANA_USER_LOADER:
                // TODO: Store current user id in preferences
                cursorLoader = new CursorLoader(
                        getActivity(),
                        GsanaContract.UserEntry.CONTENT_URI,
                        LoadersColumns.ASANA_USER_COLUMNS,
                        null,
                        null,
                        null
                );
                break;
            default:
                cursorLoader = null;
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case ASANA_PROJECTS_LOADER:
                Log.v(LOG_TAG, "Performing Projects loading " + data.getCount() );
                mProjectsAdapter.swapCursor(data);
                break;
            case ASANA_WORKSPACES_LOADER:
                Log.v(LOG_TAG, "Performing Workspaces loading " + data.getCount());
                mWorkspacesAdapter.swapCursor(data);
                break;
            case ASANA_USER_LOADER:
                // Get user info
                Log.v(LOG_TAG, "Performing User loading " + data.getCount());
                if (data.moveToFirst()) {
                    // Save current user to shared preferences if needed
                    if (null == Utility.getSettingsStringValue(getActivity(), Utility.CURRENT_USER_KEY)) {
                        Utility.putSettingsStringValue(getActivity(), Utility.CURRENT_USER_KEY, data.getString(LoadersColumns.COL_USER_ID));
                    }
                    String currentUserName = data.getString(LoadersColumns.COL_USER_NAME);
                    mGsanaUserVH.userNameTextView.setText(currentUserName);
                    // Get user name
                    MainActivity.CURRENT_USER_NAME = currentUserName.split(" ")[0];
                    byte[] blob = data.getBlob(LoadersColumns.COL_USER_PHOTO);
                    if (blob != null) {
                        Bitmap userPic = BitmapFactory.decodeByteArray(blob, 0, blob.length);
                        mGsanaUserVH.userPicImageView.setImageBitmap(userPic);
                    }
                }
                break;
            default:
                Log.d(LOG_TAG, "Loader id " + loader.getId());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(ASANA_WORKSPACES_LOADER, null, this);
        getLoaderManager().restartLoader(ASANA_PROJECTS_LOADER, null, this);
        getLoaderManager().restartLoader(ASANA_USER_LOADER, null, this);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProjectsAdapter.swapCursor(null);
        mWorkspacesAdapter.swapCursor(null);
    }

    /**
     * User View Holder
     */
    private class GsanaUserViewHolder {
        public final TextView userNameTextView;
        public final ImageView userPicImageView;

        public GsanaUserViewHolder(View view) {
            userNameTextView = (TextView) view.findViewById(R.id.left_drawer_user_name);
            userPicImageView = (ImageView) view.findViewById(R.id.left_drawer_user_pic);

        };
    }
}
