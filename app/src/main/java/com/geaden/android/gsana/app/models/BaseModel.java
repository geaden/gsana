package com.geaden.android.gsana.app.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Base model that has id and name
 */
public class BaseModel extends Model {
    // Json keys
    private final String KEY_ID = "id";
    private final String KEY_NAME = "name";

    // Base model fields
    private Long id;
    private String name;

    /**
     * Base model default constructor
     */
    public BaseModel() {};

    /**
     * Constructs base model from Json object
     * @param data {@link org.json.JSONObject} the data to construct model from
     */
    public BaseModel(JSONObject data) {
        id = getLongValue(data, KEY_ID);
        name = getStringValue(data, KEY_NAME);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        JSONObject result = this.toJSONObject();
        if (result == null) {
            return  "{\"" + KEY_ID + "\": \"" + getId() + "\", \"" + KEY_NAME + "\": \"" + getName() + "\"}";
        }
        return result.toString();
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject result = null;
        try {
            result = new JSONObject().put(KEY_ID, getId()).put(KEY_NAME, getName());
        } catch (JSONException e) {};
        return result;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}

