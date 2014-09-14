package com.geaden.android.gsana.app.api;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Interface for Asana Api
 * <a href="http://developer.asana.com/documentation/">Documentation for more methods</a>
 */
public interface AsanaApi {
    /**
     * Gets logged in user info
     * @return user info as {@link JSONObject}
     */
    public JSONObject getUserInfo();

    /**
     * Gets list of tasks for default workspace
     * @return {@link org.json.JSONObject} of tasks
     */
    public JSONObject getTasks();

    /**
     * Gets list of all workspaces for current user
     * @return list of workspaces
     */
    public JSONArray getWorkspaces();

    /**
     * Gets task data by task id
     * @param taskId id of task to get data
     * @return {@link JSONObject} of task data
     */
    public JSONObject getTaskData(String taskId);

    /**
     * Gets list of all projects for workspace id
     * @param workspaceId the workspace id to retrieve projects for
     * @return list of workspace projects
     */
    public JSONArray getProjects(String workspaceId);

}
