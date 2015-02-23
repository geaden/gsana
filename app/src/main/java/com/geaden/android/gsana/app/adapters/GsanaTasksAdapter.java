package com.geaden.android.gsana.app.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.geaden.android.gsana.app.LoadersColumns;
import com.geaden.android.gsana.app.R;
import com.geaden.android.gsana.app.Utility;
import com.geaden.android.gsana.app.api.toggl.GToggl;
import com.geaden.android.gsana.app.api.toggl.TimeEntry;
import com.geaden.android.gsana.app.api.toggl.util.DateUtil;
import com.geaden.android.gsana.app.data.GsanaContract;

import java.util.Date;

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
//        public final ToggleButton toggleTimer;
//        private final Chronometer taskTimer;

        public ViewHolder(View view) {
            taskNameView = (TextView) view.findViewById(R.id.list_item_asana_task_name);
            taskDueOnView = (TextView) view.findViewById(R.id.list_item_asana_task_due_on);
            taskProjectColor = view.findViewById(R.id.list_item_asana_task_project_color);
//            toggleTimer = (ToggleButton) view.findViewById(R.id.list_item_asana_task_start_timer);
//            taskTimer = (Chronometer) view.findViewById(R.id.toggl_task_timer);
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
//            viewHolder.toggleTimer.setVisibility(View.GONE);
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
        viewHolder.taskDueOnView.setText(!taskDueOn.equals("null") ? Utility.sqlStringDateFormat(taskDueOn) : "");

        // Task project color
        String projectColor = cursor.getString(LoadersColumns.COL_TASK_PROJECT_COLOR);
        viewHolder.taskProjectColor.setBackgroundColor(GsanaProjectsAdapter.getColor(context, projectColor));

        if (mTogglEnabled) {
            final String startDate = cursor.getString(LoadersColumns.COL_TASK_TOGGL_START_DATE);
            final String endDate = cursor.getString(LoadersColumns.COL_TASK_TOGGL_END_DATE);
            if (startDate != null) {
                if (endDate != null) {
//                    viewHolder.toggleTimer.setChecked(false);
                } else {
//                    viewHolder.toggleTimer.setChecked(true);
//                    Date start = DateUtil.convertStringToDate(startDate);
//                    long duration = SystemClock.elapsedRealtime() + start.getTime();
//                    viewHolder.taskTimer.setBase(duration);
//                    viewHolder.taskTimer.start();
                }
            }

//            viewHolder.toggleTimer.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (viewHolder.toggleTimer.isChecked()) {
//                        Toast.makeText(context,
//                                "Starting timer for " + cursor.getString(LoadersColumns.COL_TASK_ID),
//                                Toast.LENGTH_SHORT).show();
//                        Date start = new Date();
//                        if (endDate != null) {
//                            start = DateUtil.convertStringToDate(endDate);
//                        }
//                        if (startDate != null) {
//                            start = DateUtil.convertStringToDate(startDate);
//                        }
//                        long duration = System.currentTimeMillis() - start.getTime();
//                        viewHolder.taskTimer.setBase(duration);
//                        viewHolder.taskTimer.start();
//                    } else {
//                        Toast.makeText(context,
//                                "Stopping timer for " + cursor.getString(LoadersColumns.COL_TASK_ID),
//                                Toast.LENGTH_SHORT).show();
//                        viewHolder.taskTimer.stop();
//                    }
//                    TimeEntryAsyncTask timeEntryAsyncTask = new TimeEntryAsyncTask();
//                    timeEntryAsyncTask.execute(
//                            new Object[]{cursor, viewHolder.toggleTimer.isChecked()});
//                }
//            });
        }
    }

    private class TimeEntryAsyncTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            GToggl gToggl = new GToggl(mContext, mTogglApiKey);
            Cursor cursor = (Cursor) params[0];
            Boolean isStarted = (Boolean) params[1];
            TimeEntry timeEntry = new TimeEntry();
            Long tEntryId = cursor.getLong(LoadersColumns.COL_TASK_TOGGL_ENTRY_ID);
            timeEntry.setDescription(cursor.getString(LoadersColumns.COL_TASK_NAME));
            if (tEntryId != null) {
                timeEntry.setId(tEntryId);
                if (!isStarted) {
                    timeEntry.setStop(new Date());
                    timeEntry = gToggl.updateTimeEntry(timeEntry);
                } else {
                    timeEntry.setStart(new Date());
                    timeEntry.setStop(null);
                    timeEntry = gToggl.updateTimeEntry(timeEntry);
                }
            } else {
                timeEntry = gToggl.startTimeEntry(timeEntry);
            }
            ContentValues cv = new ContentValues();
            cv.put(GsanaContract.TaskEntry.COLUMN_TOGGL_START_DATE, DateUtil.convertDateToString(timeEntry.getStart()));
            if (timeEntry.getStop() != null) {
                cv.put(GsanaContract.TaskEntry.COLUMN_TOGGL_END_DATE, DateUtil.convertDateToString(timeEntry.getStop()));
            }
            cv.put(GsanaContract.TaskEntry.COLUMN_TOGGL_ENTRY_ID, timeEntry.getId());
            mContext.getContentResolver().update(GsanaContract.TaskEntry.buildTaskUri(
                            cursor.getLong(LoadersColumns.COL_TASK_ID)),
                    cv, null, null);
            mContext.getContentResolver().notifyChange(GsanaContract.TaskEntry.CONTENT_URI, null);
            return null;
        }
    }
}
