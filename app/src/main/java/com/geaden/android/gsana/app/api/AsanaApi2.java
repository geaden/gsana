package com.geaden.android.gsana.app.api;


import android.content.Context;
import android.util.Log;

import com.geaden.android.gsana.app.models.AsanaProject;
import com.geaden.android.gsana.app.models.AsanaStory;
import com.geaden.android.gsana.app.models.AsanaTask;
import com.geaden.android.gsana.app.models.AsanaUser;
import com.geaden.android.gsana.app.models.AsanaWorkspace;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains methods for basic Asana APIs, which are
 * loaded into the background.
 *
 * Uses singleton pattern.
 */
public class AsanaApi2 {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private static AsanaApi2 instance = null;
    private AsanaApiBridge mAsanaApiBridge = null;

    /**
     * Singleton of Asana API implementation
     *
     * @param context the context of application
     */
    protected AsanaApi2(Context context) {
        mAsanaApiBridge = AsanaApiBridge.getInstance(context);
    }

    /**
     * Singleton implementation for Asana API realization
     *
     * @param context the context of application
     * @return Asana API instance
     */
    public static AsanaApi2 getInstance(Context context) {
        if (instance == null) {
            instance = new AsanaApi2(context);
        }
        return instance;
    }

    /**
     * Requests the user record for the logged-in user
     */
    public void me(final AsanaCallback<AsanaUser> callback) {
        Log.i(LOG_TAG, "Requesting user info");
        mAsanaApiBridge.request(HttpHelper.Method.GET, "/users/me", null, new AsanaCallback<AsanaResponse>() {
            @Override
            public void onResult(AsanaResponse response) {
                JSONObject data = (JSONObject) response.getData();
                AsanaUser user = new AsanaUser(data);
                callback.onResult(user);
            }

            @Override
            public void onError(Throwable exception) {
                callback.onError(exception);

            }
        });
    }

    /**
     * Makes an Asana API request to add a task in the system.
     *
     * @param task {@link com.geaden.android.gsana.app.models.AsanaTask} Task fields.
     * @param callback Callback on success.
     */
    public void createTask(AsanaTask task, final AsanaCallback<AsanaTask> callback) {
        Log.d(LOG_TAG, "Creating task " + task.getName());
        mAsanaApiBridge.request("POST", "/workspaces/" + task.getWorkspace().getId() + "/tasks",
                task.toString(),
                new AsanaCallback<AsanaResponse>() {
                    @Override
                    public void onResult(AsanaResponse response) {
                        callback.onResult(new AsanaTask((JSONObject) response.getData()));
                    }

                    @Override
                    public void onError(Throwable exception) {
                        callback.onError(exception);
                    }
                });
    }

    /**
     * Gets tasks from Asana for logged in user.
     *
     * @param workspace {@link com.geaden.android.gsana.app.models.AsanaWorkspace} the workspace object
     *  to retrieve tasks for.
     * @param callback {@link com.geaden.android.gsana.app.api.AsanaCallback} callback for data
     */
    public void tasks(AsanaWorkspace workspace, final AsanaCallback<List<AsanaTask>> callback) {
        Log.i(LOG_TAG, "Requesting tasks for workspace " + workspace.getId());
        mAsanaApiBridge.request(HttpHelper.Method.GET,
                "/tasks?workspace=" + workspace.getId() + "&assignee=me",
                null, new AsanaCallback<AsanaResponse>() {
                    @Override
                    public void onResult(AsanaResponse response) {
                        JSONArray data = (JSONArray) response.getData();
                        List<AsanaTask> tasks = new ArrayList<AsanaTask>();
                        try {
                            for (int i = 0; i < data.length(); i++) {
                                tasks.add(new AsanaTask(data.getJSONObject(i)));
                            }
                            callback.onResult(tasks);
                        } catch (JSONException e) {
                            callback.onError(e);
                        }
                    }

                    @Override
                    public void onError(Throwable exception) {
                        callback.onError(exception);
                    }
                });
    }

    /**
     * Gets task details
     *
     * @param asanaTask the task to get details for
     * @param callback {@link com.geaden.android.gsana.app.api.AsanaCallback} callback for data
     */
    public void getTaskDetail(AsanaTask asanaTask, final AsanaCallback<AsanaTask> callback) {
        Log.i(LOG_TAG, "Getting task details " + asanaTask.getId());
        mAsanaApiBridge.request(HttpHelper.Method.GET, "/tasks/" + asanaTask.getId(),
                null, new AsanaCallback<AsanaResponse>() {
                    @Override
                    public void onResult(AsanaResponse value) {
                        JSONObject data = (JSONObject) value.getData();
                        callback.onResult(new AsanaTask(data));
                    }

                    @Override
                    public void onError(Throwable exception) {
                        callback.onError(exception);
                    }
                });
    }


    /**
     * Requests the set of workspaces the logged-in user is in.
     * @param callback {@link com.geaden.android.gsana.app.api.AsanaCallback} callback for data
     */
    public void workspaces(final AsanaCallback<List<AsanaWorkspace>> callback) {
        // retrieve workspaces
        Log.i(LOG_TAG, "Requesting workspaces");
        mAsanaApiBridge.request(HttpHelper.Method.GET, "/workspaces", null, new AsanaCallback<AsanaResponse>() {
            @Override
            public void onResult(AsanaResponse response) {
                JSONArray data = (JSONArray) response.getData();
                List<AsanaWorkspace> workspaces = new ArrayList<AsanaWorkspace>();
                try {
                    for (int i = 0; i < data.length(); i++) {
                        workspaces.add(new AsanaWorkspace(data.getJSONObject(i)));
                    }
                    callback.onResult(workspaces);
                } catch (JSONException e) {
                    callback.onError(e);
                }
            }

            @Override
            public void onError(Throwable exception) {
                callback.onError(exception);
            }
        });
    }

    /**
     * Requests the set of users in a workspace.
     *
     * @param workspace workspace to retrieve users for
     * @param callback Callback on result or on error
     */
    public void users(AsanaWorkspace workspace, final AsanaCallback<List<AsanaUser>> callback) {
        mAsanaApiBridge.request(HttpHelper.Method.GET, "/workspaces/" + workspace.getId() + "/users",
                null, new AsanaCallback<AsanaResponse>() {
                    @Override
                    public void onResult(AsanaResponse response) {
                        JSONArray data = (JSONArray) response.getData();
                        List<AsanaUser> users = new ArrayList<AsanaUser>();
                        try {
                            for (int i = 0; i < data.length(); i++) {
                                users.add(new AsanaUser(data.getJSONObject(i)));
                            }
                            callback.onResult(users);
                        } catch (JSONException e) {
                            callback.onError(e);
                        }
                    }

                    @Override
                    public void onError(Throwable exception) {
                        callback.onError(exception);
                    }
                });
    }

    /**
     * Request the of projects for logged-in user
     * @param callback Callback on result or on error
     */
    public void projects(final AsanaWorkspace workspace, final AsanaCallback<List<AsanaProject>> callback) {
        // retrieve projects
        Log.i(LOG_TAG, "Requesting projects for worspace " + workspace.getId());
        mAsanaApiBridge.request(HttpHelper.Method.GET, "/workspaces/" + workspace.getId() + "/projects",
                null, new AsanaCallback<AsanaResponse>() {
                    @Override
                    public void onResult(AsanaResponse response) {
                        JSONArray data = (JSONArray) response.getData();
                        List<AsanaProject> projects = new ArrayList<AsanaProject>();
                        try {
                            for (int i = 0; i < data.length(); i++) {
                                AsanaProject project = new AsanaProject(data.getJSONObject(i));
                                project.setWorkspace(workspace);
                                projects.add(project);
                            }
                            callback.onResult(projects);
                        } catch (JSONException e) {
                            callback.onError(e);
                        }

                    }

                    @Override
                    public void onError(Throwable exception) {
                        callback.onError(exception);
                    }
                });
    }


    /**
     * Gets tasks for desired project
     * @param project the project to get tasks for
     * @param callback {@link com.geaden.android.gsana.app.api.AsanaCallback} callback for data
     */
    public void projectTasks(final AsanaProject project, final AsanaCallback<List<AsanaTask>> callback) {
        Log.i(LOG_TAG, "Requesting tasks for project " + project.getId());
        mAsanaApiBridge.request(HttpHelper.Method.GET, "/projects/" + project.getId() + "/tasks",
                null, new AsanaCallback<AsanaResponse>() {
                    @Override
                    public void onResult(AsanaResponse response) {
                        JSONArray data = (JSONArray) response.getData();
                        List<AsanaTask> projectTasks = new ArrayList<AsanaTask>();
                        try {
                            for (int i = 0; i < data.length(); i++) {
                                AsanaTask projectTask = new AsanaTask(data.getJSONObject(i));
                                List<AsanaProject> taskProjects = new ArrayList<AsanaProject>();
                                taskProjects.add(project);
                                projectTask.setWorkspace(project.getWorkspace());
                                projectTask.setProjects(taskProjects);
                                projectTasks.add(projectTask);
                            }
                            callback.onResult(projectTasks);
                        } catch (JSONException e) {
                            callback.onError(e);
                        }
                    }

                    @Override
                    public void onError(Throwable exception) {
                        callback.onError(exception);
                    }
                });
    }

    /**
     * Retrieves task stories
     */
    public void taskStories(long taskId, final AsanaCallback<List<AsanaStory>> callback) {
        Log.i(LOG_TAG, "Requesting stories for " + taskId);
        mAsanaApiBridge.request(HttpHelper.Method.GET, "/tasks/" + taskId + "/stories",
                null, new AsanaCallback<AsanaResponse>() {
                    @Override
                    public void onResult(AsanaResponse response) {
                        JSONArray data = (JSONArray) response.getData();
                        if (data == null) {
                            return;
                        }
                        List<AsanaStory> taskStories = new ArrayList<AsanaStory>();
                        try {
                            for (int i = 0; i < data.length(); i++) {
                                AsanaStory taskStory = new AsanaStory(data.getJSONObject(i));
                                taskStories.add(taskStory);
                            }
                            callback.onResult(taskStories);
                        } catch (JSONException e) {
                            callback.onError(e);
                        }
                    }

                    @Override
                    public void onError(Throwable exception) {
                        callback.onError(exception);
                    }
                });

    };


    /**
     * Adds comment to a task
     */
    public void addTaskComment(long taskId, String taskComment, final AsanaCallback<AsanaStory> callback) {
        Log.i(LOG_TAG, "Commenting on " + taskId);
        mAsanaApiBridge.request(HttpHelper.Method.POST, "/tasks/" + taskId + "/stories",
                "text=" + taskComment, new AsanaCallback<AsanaResponse>() {
                    @Override
                    public void onResult(AsanaResponse response) {
                        JSONObject data = (JSONObject) response.getData();
                        AsanaStory taskStory = new AsanaStory(data);
                        callback.onResult(taskStory);
                    }

                    @Override
                    public void onError(Throwable exception) {
                        callback.onError(exception);
                    }
                });

    };



    /**
     * Gets projects details
     *
     * @param project the project to get details for
     * @param callback {@link com.geaden.android.gsana.app.api.AsanaCallback} callback for data
     */
    public void getProjectDetails(final AsanaProject project, final AsanaCallback<AsanaProject> callback) {
        Log.i(LOG_TAG, "Requesting project details " + project.getId());
        mAsanaApiBridge.request(HttpHelper.Method.GET, "/projects/" + project.getId(),
                null, new AsanaCallback<AsanaResponse>() {
                    @Override
                    public void onResult(AsanaResponse response) {
                        JSONObject data = (JSONObject) response.getData();
                        AsanaProject responseProject = new AsanaProject(data);
                        callback.onResult(responseProject);
                    }

                    @Override
                    public void onError(Throwable exception) {
                        callback.onError(exception);
                    }
                });
    }
}
