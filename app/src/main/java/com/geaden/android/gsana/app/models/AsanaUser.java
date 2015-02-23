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
    private final String ASANA_USER_PHOTO = "photo";
    private final String ASANA_USER_EMAIL = "email";
    private final String ASANA_USER_PHOTO_URL = "image_60x60";
    private final String ASANA_USER_PHOTO_60_URL = "image_60x60";
    private final String ASANA_USER_PHOTO_128_URL = "image_128x128";

    // User fields
    private String email;
    private UserPhoto photo;
    private List<AsanaWorkspace> workspaces;

    /**
     * Constructs AsanaUser from JSON response
     * @param userData {JSONObject} the user JSON response
     */
    public AsanaUser(JSONObject userData) {
        super(userData);
        email = getStringValue(userData, ASANA_USER_EMAIL);
        photo = new UserPhoto(getJSONObject(userData, ASANA_USER_PHOTO));
        workspaces = new ArrayList<AsanaWorkspace>();
        JSONArray jsonWorkspaces = getJSONArray(userData, ASANA_USER_WORKSPACES);
        if (jsonWorkspaces != null) {
            for (int i = 0; i < jsonWorkspaces.length(); i++) {
                try {
                    workspaces.add(new AsanaWorkspace(jsonWorkspaces.getJSONObject(i)));
                } catch (JSONException e) {
                    Log.d(LOG_TAG, "Failed to get user workspace " + i);
                    e.printStackTrace();
                }
            }
        }
    }

    public AsanaUser() {};

    public class UserPhoto {
        private String photoUrl;
        private String photo60Url;
        private String photo128Url;

        UserPhoto(JSONObject userPhotoData) {
            photoUrl = getStringValue(userPhotoData, ASANA_USER_PHOTO_URL);
            photo60Url = getStringValue(userPhotoData, ASANA_USER_PHOTO_60_URL);
            photo128Url = getStringValue(userPhotoData, ASANA_USER_PHOTO_128_URL);
        }

        public String getPhotoUrl() {
            return photoUrl;
        }

        public String getPhoto60Url() {
            return photo60Url;
        }

        public String getPhoto128Url() {
            return photo128Url;
        }
    }

    public UserPhoto getPhoto() {
        return photo;
    }

    public List<AsanaWorkspace> getWorkspaces() {
        return workspaces;
    }

    public String getEmail() {
        return email;
    }

    public void setPhoto(UserPhoto photo) {
        this.photo = photo;
    }

    public void setWorkspaces(List<AsanaWorkspace> workspaces) {
        this.workspaces = workspaces;
    }
}
