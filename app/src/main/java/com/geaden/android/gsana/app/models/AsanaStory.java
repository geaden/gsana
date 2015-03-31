package com.geaden.android.gsana.app.models;

import org.json.JSONObject;

/**
 * Asana story class.
 *
 * @author Gennady Denisov
 */
public class AsanaStory extends BaseModel {
    // Json story keys
    private final String ASANA_STORY_TEXT = "text";
    private final String ASANA_STORY_CREATED_BY = "created_by";
    private final String ASANA_STORY_CREATED_AT = "created_at";
    private final String ASANA_STORY_TYPE = "type";

    // Story fields
    private String createdAt;
    private AsanaUser createdBy;
    private String type;
    private String text;

    public AsanaStory(JSONObject storyData) {
        super(storyData);
        text = getStringValue(storyData, ASANA_STORY_TEXT);
        createdBy = new AsanaUser(getJSONObject(storyData, ASANA_STORY_CREATED_BY));
        createdAt = getStringValue(storyData, ASANA_STORY_CREATED_AT);
        type = getStringValue(storyData, ASANA_STORY_TYPE);
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public AsanaUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(AsanaUser createdBy) {
        this.createdBy = createdBy;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
