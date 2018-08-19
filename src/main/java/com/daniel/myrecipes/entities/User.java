package com.daniel.myrecipes.entities;

import org.json.JSONException;
import org.json.JSONObject;

public class User {

    private int id;
    private String username;
    private String password;

    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public JSONObject toJSON() {
        return new JSONObject()
                .put("id", id)
                .put("username", username)
                .put("password", password);
    }

    public static User fromJSON(JSONObject data) throws JSONException {
        User user = new User();
        user.setId(data.getInt("id"));
        user.setUsername(data.getString("username"));
        user.setPassword(data.getString("password"));
        return user;
    }

    @Override
    public String toString() {
        return "[" + id + "] " + username + " : " + password;
    }
}
