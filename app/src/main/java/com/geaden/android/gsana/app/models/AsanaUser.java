package com.geaden.android.gsana.app.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO for the Asana User.
 */
public class AsanaUser extends BaseModel {
    private final String LOG_TAG = getClass().getSimpleName();

    // JSON keys
    private final String ASANA_USER_WORKSPACES = "workspaces";
    final String ASANA_USER_PHOTO = "photo";
    final String ASANA_USER_PHOTO_URL = "image_60x60";

    // User fields
    private UserPhoto photo;
    private List<AsanaWorkspace> workspaces;

    /**
     * Constructs AsanaUser from JSON response
     * @param userData {JSONObject} the user JSON response
     */
    public AsanaUser(JSONObject userData) {
        super(userData);
        photo = new UserPhoto(getJSONObject(userData, ASANA_USER_PHOTO));
        workspaces = new ArrayList<AsanaWorkspace>();
        JSONArray jsonWorkspaces = getJSONArray(userData, ASANA_USER_WORKSPACES);
        for (int i = 0; i < jsonWorkspaces.length(); i++) {
            try {
                workspaces.add(new AsanaWorkspace(jsonWorkspaces.getJSONObject(i)));
            } catch (JSONException e) {
                Log.d(LOG_TAG, "Failed to get user worspace " + i);
                e.printStackTrace();
            }
        }
    }

    public AsanaUser() {};

    public class UserPhoto {
        private String photoUrl;

        UserPhoto(JSONObject userPhotoData) {
            photoUrl = getStringValue(userPhotoData, ASANA_USER_PHOTO_URL);
        }

        public String getPhotoUrl() {
            return photoUrl;
        }
    }

    public UserPhoto getPhoto() {
        return photo;
    }

    public List<AsanaWorkspace> getWorkspaces() {
        return workspaces;
    }

    public void setPhoto(UserPhoto photo) {
        this.photo = photo;
    }

    public void setWorkspaces(List<AsanaWorkspace> workspaces) {
        this.workspaces = workspaces;
    }
}
