package com.daniel.myrecipes.entities.daos;

import com.daniel.myrecipes.entities.User;
import com.daniel.myrecipes.networking.ResponseException;

import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class UserDao {

    public static User getUser(Connection con, String username, String password) throws Exception {
        try (PreparedStatement stmt = con.prepareStatement(
                     "SELECT * FROM users WHERE username = ? LIMIT 1")) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && password.equals(rs.getString(3))) {
                    User user = new User();
                    user.setId(rs.getInt(1));
                    user.setUsername(rs.getString(2));
                    user.setPassword(rs.getString(3));
                    return user;
                }
            }
        }

        return null;
    }

    public static User addUser(Connection con, String username, String password) throws Exception {

        // Check if the user exists
        try (PreparedStatement stmt = con.prepareStatement(
                "SELECT * FROM users WHERE username = ?")) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    throw new ResponseException(Response.Status.BAD_REQUEST,
                            "Username \"" + username + "\" already exists!");
                }
            }

        }

        // User doesn't exist - add this one
        User user = new User(username, password);

        try (PreparedStatement stmt = con.prepareStatement(
                "INSERT INTO users (username, password) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
            }

        } catch (Exception e) {
            throw new ResponseException(Response.Status.INTERNAL_SERVER_ERROR,
                    "Failed to add user: " + username);
        }

        return user;

    }

    public static void updatePassword(Connection con, User authorizedUser, String newPassword) throws Exception {
        try (PreparedStatement stmt = con.prepareStatement(
                     "UPDATE users SET password = ? WHERE id = ? LIMIT 1")) {

            stmt.setString(1, newPassword);
            stmt.setInt(2, authorizedUser.getId());
            stmt.executeUpdate();
        }

    }

}
