package com.geaden.android.app.gsana.api;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Interface for Asana Api
 * @see @link{http://developer.asana.com/documentation/
 */
public interface AsanaApi {
    /**
     * Gets list of tasks for default workspace
     * @return list of tasks
     */
    public String getTasks();

    /**
     * Gets list of all workspaces for current user
     * @return list of workspaces
     */
    public JSONArray getWorkspaces();

    /**
     * Gets list of all projects for workspace id
     * @param workspaceId the workspace id to retrieve projects for
     * @return list of workspace projects
     */
    public JSONArray getProjects(String workspaceId);

}
