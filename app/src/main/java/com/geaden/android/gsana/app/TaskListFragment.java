package com.geaden.android.gsana.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.geaden.android.gsana.app.api.AsanaApi;
import com.geaden.android.gsana.app.api.AsanaApiImpl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Task list fragment.
 */
public class TaskListFragment extends Fragment {
    private final String LOG_TAG = getClass().getSimpleName();

    private ArrayAdapter<String> mAsanaAdapter;

    public TaskListFragment() {
    }

    /**
     * Create new instance of tasks fragment with access token
     * @param accessToken Asana access token
     * @return new instance of fragment
     */
    public static TaskListFragment newInstance(String accessToken) {
        TaskListFragment taskListFragment = new TaskListFragment();
        Bundle args = new Bundle();
        args.putString(MainActivity.ACCESS_TOKEN_KEY, accessToken);
        taskListFragment.setArguments(args);
        return taskListFragment;
    }

    public String getAccessToken() {
        return getArguments().getString(MainActivity.ACCESS_TOKEN_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Create some dummy data for the ListView.  Here's a sample weekly forecast
        String[] data = {
                "Finish Mockups - Final Project - Today",
                "Pay Bills - Householding - Today",
                "Finish Summary - Final Project - Aug 10",
                "Fix CSS - Website - Aug 26"
        };
        List<String> userTasks = new ArrayList<String>(Arrays.asList(data));
        // Now that we have some dummy forecast data, create an ArrayAdapter.
        // The ArrayAdapter will take data from a source (like our dummy forecast) and
        // use it to populate the ListView it's attached to.
        mAsanaAdapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_asana, // The name of the layout ID.
                        R.id.list_item_asana_textview, // The ID of the textview to populate.
                        userTasks);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        FloatingActionButton fabButton = new FloatingActionButton.Builder(getActivity())
                .withDrawable(getResources().getDrawable(R.drawable.ic_content_new))
                .withButtonColor(Color.GREEN)
                .withGravity(Gravity.BOTTOM | Gravity.CENTER)
                .withMargins(0, 0, 0, 16)
                .create();

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), TaskCreateActivity.class);
                startActivity(intent);
            }
        });

        ListView listView = (ListView) rootView.findViewById(R.id.listview_asana);
        listView.setAdapter(mAsanaAdapter);
        getTasks();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String taskData = mAsanaAdapter.getItem(position);
                String taskId = taskData.split(":")[0];
                Intent intent = new Intent(getActivity(), TaskDetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, taskId);
                startActivity(intent);
            }
        });
        return rootView;
    }

    private void getTasks() {
        FetchTask task = new FetchTask();
        task.execute(getAccessToken());
    }

    /**
     * Fetches tasks from Asana
     */
    public class FetchTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = getClass().getSimpleName();

        /**
         * Forms array of strings from JSON string
         * @param asanaTasksDataJsonStr json string as response from Asana
         * @return array of tasks string representation
         * @throws JSONException
         */
        private String[] getTasksDataFromJson(String asanaTasksDataJsonStr)
            throws JSONException {
            // Fields of data json
            final String TASKS_DATA = "data";
            final String TASK_ID = "id";
            final String TASK_NAME = "name";

            JSONObject asanaTasksJson = new JSONObject(asanaTasksDataJsonStr);
            JSONArray tasks = asanaTasksJson.getJSONArray(TASKS_DATA);
            String[] tasksArray = new String[tasks.length()];
            for (int i = 0; i < tasks.length(); i++) {
                JSONObject taskJson = tasks.getJSONObject(i);
                tasksArray[i] = taskJson.getString(TASK_ID) + ":" + taskJson.getString(TASK_NAME);
            }
            return tasksArray;
        }

        @Override
        protected String[] doInBackground(String... params) {
            // Get access token from input parameters
            String accessToken = params[0];

            // Initialize Asana api
            AsanaApi asanaApi = new AsanaApiImpl(accessToken);

            // Fetch asana tasks
            String tasksJsonStr = null;
            try {
                tasksJsonStr = asanaApi.getTasks();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error communicating with Asana api", e);
                Utility.invalidateAccessToken(getActivity());
            }

            try {
                return getTasksDataFromJson(tasksJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error getting JSON", e);
            }
            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                mAsanaAdapter.clear();
                for (String taskStr : result) {
                    mAsanaAdapter.add(taskStr);
                }
            }
        }
    }
}