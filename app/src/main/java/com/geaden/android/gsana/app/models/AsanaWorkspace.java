package com.geaden.android.gsana.app.models;

import org.json.JSONObject;

/**
 *  POJO for the Asana Workspace.
 */
public class AsanaWorkspace extends BaseModel {

    public AsanaWorkspace() { };

    public AsanaWorkspace(JSONObject workspaceData) {
        super(workspaceData);
    }
}
