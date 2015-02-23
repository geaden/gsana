package com.geaden.android.gsana.app;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.geaden.android.gsana.app.fragments.TaskDetailFragment;

/**
 * Task detail activity.
 */
public class TaskDetailActivity extends ActionBarActivity {
    private final String LOG_TAG = getClass().getSimpleName();

    public static final String TASK_KEY = "asana_task_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            String taskId = getIntent().getStringExtra(TASK_KEY);

            Log.d(LOG_TAG, "Task Id: " + taskId);

            Bundle arguments = new Bundle();
            arguments.putString(TASK_KEY, taskId);

            TaskDetailFragment fragment = new TaskDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.detail_container, fragment)
                    .commit();
        }
    }
}
