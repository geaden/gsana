package com.geaden.android.gsana.app.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * POJO for the Asana Project Model.
 */
public class AsanaProject extends BaseModel {
    // Json keys for Project
    private final String ASANA_PROJECT_ARCHIVED = "archived";
    private final String ASANA_PROJECT_CREATED_AT = "created_at";
    private final String ASANA_PROJECT_MODIFIED_AT = "modified_at";
    private final String ASANA_PROJECT_COLOR = "color";
    private final String ASANA_PROJECT_NOTES = "notes";
    private final String ASANA_PROJECT_WORKSPACE = "workspace";
    private final String ASANA_PROJECT_TEAM = "team";


    // Project fields.
    private String color;
    private String archived;
    private String notes;
    private AsanaWorkspace workspace;
    private AsanaTeam team;
    private String createdAt;
    private String modifiedAt;


    public AsanaProject(JSONObject projectData) {
        super(projectData);
        team = new AsanaTeam(getJSONObject(projectData, ASANA_PROJECT_TEAM));
        color = getStringValue(projectData, ASANA_PROJECT_COLOR);
        notes = getStringValue(projectData, ASANA_PROJECT_NOTES);
        archived = getStringValue(projectData, ASANA_PROJECT_ARCHIVED);
        createdAt = getStringValue(projectData, ASANA_PROJECT_CREATED_AT);
        modifiedAt = getStringValue(projectData, ASANA_PROJECT_MODIFIED_AT);
        workspace = new AsanaWorkspace(getJSONObject(projectData, ASANA_PROJECT_WORKSPACE));
    }

    public String getColor() {
        return color;
    }

    public String getArchived() {
        return archived;
    }

    public String getNotes() {
        return notes;
    }

    public AsanaWorkspace getWorkspace() {
        return workspace;
    }

    public AsanaTeam getTeam() {
        return team;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getModifiedAt() {
        return modifiedAt;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setArchived(String archived) {
        this.archived = archived;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setWorkspace(AsanaWorkspace workspace) {
        this.workspace = workspace;
    }

    public void setTeam(AsanaTeam team) {
        this.team = team;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setModifiedAt(String modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject result = super.toJSONObject();
        if (result == null) {
            return result;
        }
        try {
            result.put(ASANA_PROJECT_ARCHIVED, getArchived())
                   .put(ASANA_PROJECT_NOTES, getNotes())
                   .put(ASANA_PROJECT_COLOR, getColor())
                   .put(ASANA_PROJECT_WORKSPACE, getWorkspace().toJSONObject())
                   .put(ASANA_PROJECT_TEAM, getTeam())
                   .put(ASANA_PROJECT_CREATED_AT, getCreatedAt())
                   .put(ASANA_PROJECT_MODIFIED_AT, getModifiedAt());
        } catch (JSONException e) {

        }
        return result;
    }
}

