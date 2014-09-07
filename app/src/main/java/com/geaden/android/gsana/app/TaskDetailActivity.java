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
import android.widget.ListView;
import android.widget.TextView;

import com.geaden.android.gsana.app.api.AsanaApi;
import com.geaden.android.gsana.app.api.AsanaApiImpl;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

/**
 * Task detail activity.
 */
public class TaskDetailActivity extends ActionBarActivity {
    private static String mTaskId;

    private static TextView mTaskDetailView;

    private static AsanaApi mAsanaApi;

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
            String accessToken = Utils.getAccessToken(getActivity());
            if (accessToken == null) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                return null;
            }
            mAsanaApi = new AsanaApiImpl(accessToken);
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            TextView detailView = (TextView) rootView.findViewById(R.id.task_detail);
            // The detail Activity called via intent.  Inspect the intent for forecast data.
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                mTaskId = intent.getStringExtra(Intent.EXTRA_TEXT);
                mTaskDetailView = (TextView) rootView.findViewById(R.id.task_detail);
                getTaskData(mTaskId);
            }
            return rootView;
        }
    }

    /**
     * Gets task data by id
     * @param taskId id of task
     */
    private static void getTaskData(String taskId) {
        FetchTaskDataTask taskDataTask = new FetchTaskDataTask();
        taskDataTask.execute(taskId);
    }

    /**
     * Fetch task data in background.
     */
    private static class FetchTaskDataTask extends AsyncTask<String, Void, JSONObject> {
        private final String LOG_TAG = getClass().getSimpleName();

        @Override
        protected JSONObject doInBackground(String... params) {
            Log.v(LOG_TAG, "Fetching task data");
            String taskId = params[0];
            JSONObject taskData = mAsanaApi.getTaskData(taskId);
            return taskData;
        }

        @Override
        protected void onPostExecute(JSONObject data) {
            final String TASK_NAME = "name";
            final String TASK_NOTES = "notes";
            final String TASK_COMPLETED = "completed";
            final String TASK_CREATED_AT = "created_at";
            try {
                JSONObject taskData = data.getJSONObject("data");
                mTaskDetailView.setText(
                        taskData.getString(TASK_NAME) + "\n" +
                        taskData.getString(TASK_NOTES) + "\n" +
                        taskData.getString(TASK_COMPLETED) + "\n" +
                        taskData.getString(TASK_CREATED_AT)
                );
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error setting task data", e);
            }

        }
    }
}
