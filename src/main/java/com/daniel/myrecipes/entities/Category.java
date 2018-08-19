package com.daniel.myrecipes.entities;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;

public class Category {

    private int id;
    private String name;
    private String description;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        return 31 * 17 + Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof Category && id == ((Category) obj).id);
    }

    @Override
    public String toString() {
        return "[" + id + "] " + name + ": " + description;
    }

    public JSONObject toJSON() throws JSONException {
        return new JSONObject()
                .put("id", id)
                .put("name", name)
                .put("description", description);
    }

    public static Category fromJSON(JSONObject data) {
        Category category = new Category();

        if (data.has("id"))
            category.setId(data.getInt("id"));

        if (data.has("name"))
            category.setName(data.getString("name"));

        if (data.has("description"))
            category.setDescription(data.getString("description"));

        return category;
    }

}
