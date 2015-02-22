package com.geaden.android.gsana.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.geaden.android.gsana.app.api.toggl.GToggl;
import com.geaden.android.gsana.app.api.toggl.TimeEntry;
import com.geaden.android.gsana.app.api.toggl.util.DateUtil;
import com.geaden.android.gsana.app.data.GsanaContract;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link GsanaTasksAdapter} exposes a list of tasks
 * from {@link android.database.Cursor} to a {@link android.widget.ListView}
 */
public class GsanaTasksAdapter extends CursorAdapter {
    private static final String LOG_TAG = GsanaTasksAdapter.class.getSimpleName();
    private String mTogglApiKey;
    private Context mContext;

    /**
     * Cache of the children views for an Asana tasks list item
     */
    public static class ViewHolder {
        public final TextView taskNameView;
        public final TextView taskDueOnView;
        public final View taskProjectColor;
        public final ToggleButton taskStartTimer;
        private final Chronometer taskTimer;

        public ViewHolder(View view) {
            taskNameView = (TextView) view.findViewById(R.id.list_item_asana_task_name);
            taskDueOnView = (TextView) view.findViewById(R.id.list_item_asana_task_due_on);
            taskProjectColor = view.findViewById(R.id.list_item_asana_task_project_color);
            taskStartTimer = (ToggleButton) view.findViewById(R.id.list_item_asana_task_start_timer);
            taskTimer = (Chronometer) view.findViewById(R.id.toggl_task_timer);
        }

    }

    private boolean mTogglEnabled = false;

    public GsanaTasksAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        mContext = context;
        View view = LayoutInflater.from(context).inflate(R.layout.list_asana_task_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        mTogglApiKey = Utility.getPreference(context, context.getResources().getString(R.string.pref_toggl_api_key));
        Log.i(LOG_TAG, "Toggle API Key: " + mTogglApiKey);
        if (mTogglApiKey == null || mTogglApiKey.length() < 32) {
            viewHolder.taskStartTimer.setVisibility(View.GONE);
        } else {
            mTogglEnabled = true;
        }

        return view;
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        mContext = context;
        final ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Read task name from cursor
        String taskName = cursor.getString(LoadersColumns.COL_TASK_NAME);
        // Find TextView and set task name on it.
        viewHolder.taskNameView.setText(taskName);

        // Task due on
        String taskDueOn = cursor.getString(LoadersColumns.COL_TASK_DUE_ON);
        viewHolder.taskDueOnView.setText(taskDueOn != null ? taskDueOn : "");

        // Task project color
        String projectColor = cursor.getString(LoadersColumns.COL_TASK_PROJECT_COLOR);
        viewHolder.taskProjectColor.setBackgroundColor(GsanaProjectsAdapter.getColor(context, projectColor));

        if (mTogglEnabled) {
            String startDate = cursor.getString(LoadersColumns.COL_TASK_TOGGLE_START_DATE);
            String endDate = cursor.getString(LoadersColumns.COL_TASK_TOGGLE_END_DATE);
            if (startDate != null) {
                if (endDate != null) {
                    viewHolder.taskStartTimer.setChecked(false);
                } else {
                    Date start = DateUtil.convertStringToDate(startDate);
                    long duration = System.currentTimeMillis() - start.getTime();
                    viewHolder.taskTimer.setBase(duration);
                    viewHolder.taskStartTimer.setChecked(true);
                    viewHolder.taskTimer.start();
                }
            }

            viewHolder.taskStartTimer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(LOG_TAG, "Start timer");
                    if (viewHolder.taskStartTimer.isChecked()) {
                        viewHolder.taskStartTimer.setChecked(false);
                        // TODO: stop time entry
                        viewHolder.taskTimer.stop();
                    } else {
                        viewHolder.taskStartTimer.setChecked(true);
                        Toast.makeText(context,
                                "Starting timer for " + cursor.getString(LoadersColumns.COL_TASK_ID),
                                Toast.LENGTH_SHORT).show();
                        StartTimeAsyncTask startTimeAsyncTask = new StartTimeAsyncTask();
                        startTimeAsyncTask.execute(new Object[]{cursor, viewHolder});
                    }
                }
            });
        }
    }

    private class StartTimeAsyncTask extends AsyncTask<Object, Void, TimeEntry> {
        private ViewHolder mViewHolder;

        @Override
        protected TimeEntry doInBackground(Object... params) {
            Cursor cursor = (Cursor) params[0];
            mViewHolder = (ViewHolder) params[1];
            TimeEntry timeEntry = new TimeEntry();
            Long tEntryId = cursor.getLong(LoadersColumns.COL_TASK_TOGGL_ENTRY_ID);
            if (tEntryId != null) {
                timeEntry.setId(tEntryId);
            }
            timeEntry.setStart(new Date());
            timeEntry.setDescription(cursor.getString(LoadersColumns.COL_TASK_NAME));
            GToggl gToggl = new GToggl(mContext, mTogglApiKey);
            timeEntry = gToggl.startTimeEntry(timeEntry);
            ContentValues cv = new ContentValues();
            cv.put(GsanaContract.TaskEntry.COLUMN_TOGGL_START_DATE, DateUtil.convertDateToString(timeEntry.getStart()));
            cv.put(GsanaContract.TaskEntry.COLUMN_TOGGL_ENTRY_ID, timeEntry.getId());
            mContext.getContentResolver().update(GsanaContract.TaskEntry.buildTaskUri(
                            cursor.getLong(LoadersColumns.COL_TASK_ID)),
                    cv, null, null);
            return timeEntry;
        }

        @Override
        protected void onPostExecute(TimeEntry timeEntry) {
            super.onPostExecute(timeEntry);
            // Start timer
            mViewHolder.taskTimer.start();
        }
    }
}
