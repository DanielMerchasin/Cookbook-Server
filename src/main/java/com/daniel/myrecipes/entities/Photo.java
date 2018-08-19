package com.daniel.myrecipes.entities;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Photo {

    private int id;
    private int recipeId;
    private String location;
    private byte[] thumbnail;

    public Photo() {}

    public Photo(int recipeId, String location, byte[] thumbnail) {
        this.recipeId = recipeId;
        this.location = location;
        this.thumbnail = thumbnail;
    }

    public Photo(int id, int recipeId, String location, byte[] thumbnail) {
        this.id = id;
        this.recipeId = recipeId;
        this.location = location;
        this.thumbnail = thumbnail;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public byte[] getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }

    public static Photo fromJSON(JSONObject data) {
        Photo photo = new Photo();

        if (data.has("id"))
            photo.setId(data.getInt("id"));

        if (data.has("recipe_id"))
            photo.setId(data.getInt("recipe_id"));

        if (data.has("location"))
            photo.setLocation(data.getString("location"));

        if (data.has("thumbnail"))
            photo.setThumbnail(Base64.getDecoder().decode(data.getString("thumbnail")));

        return photo;
    }

    public JSONObject toJSON() {
        JSONObject data = new JSONObject();

        if (id != 0)
            data.put("id", id);

        if (recipeId != 0)
            data.put("recipe_id", recipeId);

        if (location != null)
            data.put("location", location);

        if (thumbnail != null)
            data.put("thumbnail", new String(Base64.getEncoder().encode(thumbnail),
                    StandardCharsets.UTF_8));

        return data;
    }

}
