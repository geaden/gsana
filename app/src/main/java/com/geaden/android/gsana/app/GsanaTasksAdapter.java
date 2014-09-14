package com.geaden.android.gsana.app;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * {@link GsanaTasksAdapter} exposes a list of tasks
 * from {@link android.database.Cursor} to a {@link android.widget.ListView}
 */
public class GsanaTasksAdapter extends CursorAdapter {
    /**
     * Cache of the children views for an Asana tasks list item
     */
    public static class ViewHolder {
        public final TextView taskNameView;
        public final TextView taskDueOnView;

        public ViewHolder(View view) {
            taskNameView = (TextView) view.findViewById(R.id.list_item_asana_task_name);
            taskDueOnView = (TextView) view.findViewById(R.id.list_item_asana_task_due_on);
        }

    }

    public GsanaTasksAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_asana, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // TODO: set task project color

        // Read task name from cursor
        String taskName = cursor.getString(TaskListFragment.COL_TASK_NAME);
        // Find TextView and set task name on it.
        viewHolder.taskNameView.setText(taskName);

        // Task due on
        String taskDueOn = cursor.getString(TaskListFragment.COL_TASK_DUE_ON);
        viewHolder.taskDueOnView.setText(taskDueOn);

    }
}
