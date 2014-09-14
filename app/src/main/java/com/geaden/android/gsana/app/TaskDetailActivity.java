package com.geaden.android.gsana.app;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.geaden.android.gsana.app.api.AsanaApi;
import com.geaden.android.gsana.app.api.AsanaApiImpl;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Task detail activity.
 */
public class TaskDetailActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.detail_container, new TaskDetailFragment())
                    .commit();
        }
    }

    public static class TaskDetailFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            String accessToken = Utility.getAccessToken(getActivity());
            if (accessToken == null) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                return null;
            }
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            TextView detailView = (TextView) rootView.findViewById(R.id.task_detail);
            // The detail Activity called via intent.  Inspect the intent for task data.
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                String taskData = intent.getStringExtra(Intent.EXTRA_TEXT);
                detailView.setText(taskData);
            }
            return rootView;
        }
    }
}
