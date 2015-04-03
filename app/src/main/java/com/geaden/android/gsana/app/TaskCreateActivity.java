package com.geaden.android.gsana.app;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.geaden.android.gsana.app.fragments.TaskCreateFragment;

/**
 * Task create activity
 */
public class TaskCreateActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_new);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_task_new, new TaskCreateFragment())
                    .commit();
        }
    }
}
