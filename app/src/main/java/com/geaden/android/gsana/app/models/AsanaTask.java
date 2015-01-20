package com.geaden.android.gsana.app.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO for the Asana Task.
 */
public class AsanaTask extends BaseModel {
    // Json tasks keys
    private final String ASANA_TASK_NOTES = "notes";
    private final String ASANA_TASK_WORKSPACE = "workspace";
    private final String ASANA_TASK_ASSIGNEE_ID = "assignee_id";
    private final String ASANA_TASK_MODIFIED_AT = "modified_at";
    private final String ASANA_TASK_PROJECTS = "projects";
    private final String ASANA_TASK_DUE_ON = "due_on";
    private final String ASANA_TASK_COMPLETED = "completed";
    private final String ASANA_TASK_CREATED_AT = "created_at";
    private final String ASANA_TASK_COMPLETED_AT = "completed_at";
    private final String ASANA_TASK_ASSIGNEE_STATUS = "assignee_status";

    // Task fields
    private String notes;
    private long assigneeId;
    private String assigneeStatus;
    private String completed;
    private String completedAt;
    private String modifiedAt;
    private String createdAt;
    private String dueOn;
    private AsanaWorkspace workspace;
    private List<AsanaProject> projects;

    /**
     * Constructs Asana Task model from JSON data
     * @param taskData the json representation of task from Asana Api
     */
    public AsanaTask(JSONObject taskData) {
        super(taskData);
        notes = getStringValue(taskData, ASANA_TASK_NOTES);
        assigneeStatus = getStringValue(taskData, ASANA_TASK_ASSIGNEE_STATUS);
        assigneeId = getLongValue(taskData, ASANA_TASK_ASSIGNEE_ID);
        completed = getStringValue(taskData, ASANA_TASK_COMPLETED);
        completedAt = getStringValue(taskData, ASANA_TASK_COMPLETED_AT);
        modifiedAt = getStringValue(taskData, ASANA_TASK_MODIFIED_AT);
        createdAt = getStringValue(taskData, ASANA_TASK_CREATED_AT);
        dueOn = getStringValue(taskData, ASANA_TASK_DUE_ON);
        if (taskData.has(ASANA_TASK_WORKSPACE)) {
            workspace = new AsanaWorkspace(getJSONObject(taskData, ASANA_TASK_WORKSPACE));
        }
        projects = new ArrayList<AsanaProject>();
        if (taskData.has(ASANA_TASK_PROJECTS)) {
            JSONArray taskProjects = getJSONArray(taskData, ASANA_TASK_PROJECTS);
            for (int i = 0; i < taskProjects.length(); i++) {
                try {
                    JSONObject projectData = taskProjects.getJSONObject(0);
                    projects.add(new AsanaProject(projectData));
                } catch (JSONException e) {
                    // TODO: Do something...
                }
            }
        }
    }

    public String getNotes() {
        return notes;
    }

    public long getAssigneeId() {
        return assigneeId;
    }

    public String getAssigneeStatus() {
        return assigneeStatus;
    }

    public String getCompleted() {
        return completed;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public String getModifiedAt() {
        return modifiedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getDueOn() {
        return dueOn;
    }

    public AsanaWorkspace getWorkspace() {
        return workspace;
    }

    public List<AsanaProject> getProjects() {
        return projects;
    }


}
