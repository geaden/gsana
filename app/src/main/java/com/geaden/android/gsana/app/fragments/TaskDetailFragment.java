package com.geaden.android.gsana.app.fragments;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
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
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.geaden.android.gsana.app.LoadersColumns;
import com.geaden.android.gsana.app.LoginActivity;
import com.geaden.android.gsana.app.MainActivity;
import com.geaden.android.gsana.app.R;
import com.geaden.android.gsana.app.TaskDetailActivity;
import com.geaden.android.gsana.app.Utility;
import com.geaden.android.gsana.app.api.toggl.GToggl;
import com.geaden.android.gsana.app.api.toggl.TimeEntry;
import com.geaden.android.gsana.app.api.toggl.util.DateUtil;
import com.geaden.android.gsana.app.data.GsanaContract;
import com.geaden.android.gsana.app.data.GsanaContract.TaskEntry;

import java.util.Date;

/**
 * Task detail fragment.
 */
public class TaskDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TASK_TIMER_RUNNING = "task_timer_running";
    private final String LOG_TAG = TaskDetailFragment.class.getSimpleName();

    /** Loaders **/
    private static final int TASK_DETAIL_LOADER = 0;
    private static final int USER_DETAIL_LOADER = 1;

    // TODO: Implement task comments loading
    private static final int TASK_COMMENTS_LOADER = 2;

    private static final int TASK_NOTIFICATION_ID = 3004;

    private ShareActionProvider mShareActionProvider;

    private static final String GSANA_SHARE_HASHTAG = " #YetAnotherAndroidClientForAsana";

    private String mTaskDuration;

    private String mTaskId;
    private String mShareTask;

    private final String TRUE = "true";
    private final String FALSE = "false";

    private TextView mAssigneeTextView;
    private TextView mTaskNameTextView;
    private CheckBox mTaskCompletedView;
    private TextView mTaskNotesTextView;
    private ImageView mAssigneeImageView;

    /** Toggl integration */
    private String mTogglApiKey;
    private boolean mTogglEnabled;
    private boolean mTogglKeyValid;

    private ToggleButton mTaskTimerToggeButton;
    private Chronometer mTaskTimer;
    private TextView mTaskProject;
    private boolean mTaskTimerRunning;


    public TaskDetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(TaskDetailActivity.TASK_KEY, mTaskId);
        // Save state of our timer
        outState.putBoolean(TASK_TIMER_RUNNING, mTaskTimerRunning);
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
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mShareTask + GSANA_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mTaskId = arguments.getString(TaskDetailActivity.TASK_KEY);
            Log.v(LOG_TAG, "Task ID: " + mTaskId);
        }

        if (savedInstanceState != null) {
            mTaskId = savedInstanceState.getString(TaskDetailActivity.TASK_KEY);
        }

        String accessToken = Utility.getAccessToken(getActivity());
        if (accessToken == null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            return null;
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mTogglEnabled = Utility.getBooleanPreference(getActivity(), getString(R.string.pref_toggl_enable));
        mTogglApiKey = Utility.getPreference(getActivity(), getActivity().getResources().getString(R.string.pref_toggl_api_key));
        mTogglKeyValid = mTogglApiKey != null && mTogglApiKey.length() >= 32;

        mAssigneeTextView = (TextView) rootView.findViewById(R.id.asana_task_assignee);
        mAssigneeImageView = (ImageView) rootView.findViewById(R.id.asana_task_assignee_photo);
        mTaskNameTextView = (TextView) rootView.findViewById(R.id.asana_task_name);
        mTaskCompletedView = (CheckBox) rootView.findViewById(R.id.asana_task_compeleted);
        mTaskNotesTextView = (TextView) rootView.findViewById(R.id.asana_task_description);
        mTaskTimerToggeButton = (ToggleButton) rootView.findViewById(R.id.btn_toggl_start_timer);
        mTaskTimer = (Chronometer) rootView.findViewById(R.id.chrono_toggl_timer);
        mTaskProject = (TextView) rootView.findViewById(R.id.asana_task_detail_project);

        if (!mTogglEnabled) {
            rootView.findViewById(R.id.toggl_timer_section).setVisibility(View.GONE);
        }

        if (mTogglEnabled && !mTogglKeyValid) {
            Toast.makeText(getActivity(), getString(R.string.msg_enter_valid_toggl_key), Toast.LENGTH_SHORT).show();
            mTaskTimerToggeButton.setEnabled(false);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mTaskId = savedInstanceState.getString(TaskDetailActivity.TASK_KEY);
            mTaskTimerRunning = savedInstanceState.getBoolean(TASK_TIMER_RUNNING);
        }

        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(TaskDetailActivity.TASK_KEY)) {
            getLoaderManager().initLoader(TASK_DETAIL_LOADER, null, this);
            getLoaderManager().initLoader(USER_DETAIL_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle args) {
        CursorLoader cursorLoader = null;
        switch (i) {
            case TASK_DETAIL_LOADER:
                Uri taskByIdUri = TaskEntry.buildTaskUri(Long.parseLong(mTaskId));
                cursorLoader = new CursorLoader(
                        getActivity(),
                        taskByIdUri,
                        LoadersColumns.ASANA_TASK_COLUMNS,
                        null,
                        null,
                        null);
                break;
            case USER_DETAIL_LOADER:
                Uri userByIdUri = GsanaContract.UserEntry.buildUserUri(
                        Long.valueOf(Utility.getSettingsStringValue(getActivity(),
                                Utility.CURRENT_USER_KEY)));
                cursorLoader = new CursorLoader(
                        getActivity(),
                        userByIdUri,
                        LoadersColumns.ASANA_USER_COLUMNS,
                        null,
                        null,
                        null);
                break;
            default:
                break;
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, final Cursor data) {
        switch (cursorLoader.getId()) {
            case TASK_DETAIL_LOADER:
                if (data != null && data.moveToFirst()) {
                    Log.v(LOG_TAG, "Loading task data");
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

                    String taskProject = data.getString(LoadersColumns.COL_TASK_PROJECT_NAME);
                    mTaskProject.setText(taskProject);

                    // Share intent task string
                    mShareTask = String.format("%s - %s - %s", taskName,
                            taskCompleted, taskNotes, mTaskDuration);

                    if (mTaskDuration != null) {
                        mShareTask += "#duration " + mTaskDuration;
                    }

                    // If onCreateOptionsMenu has already happened, we need to update the share intent now.
                    if (mShareActionProvider != null) {
                        mShareActionProvider.setShareIntent(createShareForecastIntent());
                    }

                    if (mTogglEnabled && mTogglKeyValid) {
                        long duration = data.getLong(LoadersColumns.COL_TASK_TOGGL_DURATION);
                        if (duration > 0) {
                            // Already running
                            mTaskTimer.start();
                            mTaskTimer.setBase(duration);
                            mTaskTimer.setText(Utility.getTimerFormatted(duration));
                            mTaskTimerToggeButton.setChecked(true);
                            mTaskTimerRunning = true;
                        }
                        mTaskTimerToggeButton.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                Toast.makeText(getActivity(),
                                        String.format("%s timer for " + data.getString(LoadersColumns.COL_TASK_NAME),
                                                isChecked ? "Starting" : "Stopping"),
                                        Toast.LENGTH_SHORT).show();
                                if (isChecked) {
                                    if (!mTaskTimerRunning) mTaskTimer.start();
                                    mTaskTimerRunning = true;
                                } else {
                                    mTaskTimer.stop();
                                    mTaskTimerRunning = false;
                                }
                                TimeEntryAsyncTask timeEntryAsyncTask = new TimeEntryAsyncTask();
                                timeEntryAsyncTask.execute(
                                        new Object[]{data, isChecked});
                            };
                        });
                    }
                }
                break;
            case USER_DETAIL_LOADER:
                if (null != data && data.moveToFirst()) {
                    Log.v(LOG_TAG, "Loading user data");
                    String userName = data.getString(LoadersColumns.COL_USER_NAME);
                    mAssigneeTextView.setText(userName);

                    byte[] blob = data.getBlob(LoadersColumns.COL_USER_PHOTO);
                    if (blob != null) {
                        Bitmap userPic = BitmapFactory.decodeByteArray(blob, 0, blob.length);
                        mAssigneeImageView.setImageBitmap(userPic);
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        return;
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(TaskDetailActivity.TASK_KEY)) {
            getLoaderManager().restartLoader(TASK_DETAIL_LOADER, null, this);
            if (arguments.containsKey(TASK_TIMER_RUNNING)) {
                mTaskTimerRunning = arguments.getBoolean(TASK_TIMER_RUNNING);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * Async task to request Toggl API
     */
    private class TimeEntryAsyncTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            GToggl gToggl = new GToggl(getActivity(), mTogglApiKey);
            Cursor cursor = (Cursor) params[0];
            Boolean started = (Boolean) params[1];
            Log.v(LOG_TAG, "Started " + started);
            TimeEntry timeEntry = new TimeEntry();
            Long tEntryId = cursor.getLong(LoadersColumns.COL_TASK_TOGGL_ENTRY_ID);
            timeEntry.setDescription(cursor.getString(LoadersColumns.COL_TASK_NAME));
            timeEntry.setDuration(cursor.getLong(LoadersColumns.COL_TASK_TOGGL_DURATION));
            if (tEntryId > 0) {
                timeEntry.setId(tEntryId);
                if (timeEntry.getDuration() < 0) {
                    // Already running. Stop it.
                    timeEntry = gToggl.stopTimeEntry(timeEntry);
                    mTaskDuration = Utility.getTimerFormatted(timeEntry.getDuration());
                    Log.v(LOG_TAG, "Spent " + mTaskDuration);
                } else {
                    timeEntry.setStart(new Date());
                    timeEntry = gToggl.startTimeEntry(timeEntry);
                }
            } else {
                timeEntry = gToggl.startTimeEntry(timeEntry);
            }
            ContentValues cv = new ContentValues();
            cv.put(GsanaContract.TaskEntry.COLUMN_TOGGL_START_DATE, DateUtil.convertDateToString(timeEntry.getStart()));
            if (timeEntry.getStop() != null) {
                cv.put(GsanaContract.TaskEntry.COLUMN_TOGGL_END_DATE, DateUtil.convertDateToString(timeEntry.getStop()));
            }
            cv.put(TaskEntry.COLUMN_TOGGL_DURATION, timeEntry.getDuration());
            cv.put(GsanaContract.TaskEntry.COLUMN_TOGGL_ENTRY_ID, timeEntry.getId());
            getActivity().getContentResolver().update(GsanaContract.TaskEntry.buildTaskUri(
                            cursor.getLong(LoadersColumns.COL_TASK_ID)),
                    cv, null, null);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(getActivity())
                            .setSmallIcon(R.drawable.ic_toggl_notification)
                            .setContentTitle(timeEntry.getDescription())
                            .setUsesChronometer(true)
                            .setShowWhen(true)
                            .setContentText(getString(R.string.app_name));

            // Make something interesting happen when the user clicks on the notification.
            // In this case, opening the app is sufficient.
            Intent resultIntent = new Intent(getActivity(), TaskDetailActivity.class);
            resultIntent.putExtra(TaskDetailActivity.TASK_KEY, mTaskId);

            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getActivity());
            // Adds the back stack
//            stackBuilder.addParentStack(TaskDetailActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);

            NotificationManager mNotificationManager =
                    (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            // TASK_NOTIFICATION_ID allows you to update the notification later on.
            if (started) {
                Log.v(LOG_TAG, "Show notification");
                mNotificationManager.notify(TASK_NOTIFICATION_ID, mBuilder.build());
            } else {
                Log.v(LOG_TAG, "Hide notification");
                mNotificationManager.cancel(TASK_NOTIFICATION_ID);
            }
            return null;
        }
    }
}