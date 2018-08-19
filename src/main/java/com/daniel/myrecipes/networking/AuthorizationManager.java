package com.daniel.myrecipes.networking;

import com.daniel.myrecipes.entities.User;
import com.daniel.myrecipes.entities.daos.UserDao;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.util.Base64;
import java.util.List;

public class AuthorizationManager {

    public static User authorizeUsingHeadersSafely(Connection con, HttpHeaders headers) {
        try {
            return authorizeUsingHeaders(con, headers);
        } catch (Exception e) {
            return null;
        }
    }

    public static User authorizeUsingHeaders(Connection con, HttpHeaders headers) throws ResponseException {

        List<String> authHeaders = headers.getRequestHeader(HttpHeaders.AUTHORIZATION);
        if (authHeaders == null || authHeaders.size() == 0)
            throw new ResponseException(Response.Status.BAD_REQUEST, "No authorization header.");

        String encoded = authHeaders.get(0).replaceFirst("Basic ", "");
        String decoded = new String(Base64.getDecoder().decode(encoded));

        int colonIdx = decoded.indexOf(':');

        if (colonIdx <= 0)
            throw new ResponseException(Response.Status.BAD_REQUEST, "Invalid data in authorization header.");

        String username = decoded.substring(0, colonIdx);
        String password = decoded.substring(colonIdx + 1);

        try {
            User user = UserDao.getUser(con, username, password);
            if (user == null)
                throw new ResponseException(Response.Status.UNAUTHORIZED, "Incorrect username or password.");
            return user;
        } catch (Exception e) {
            throw new ResponseException(Response.Status.SERVICE_UNAVAILABLE, "An error occurred during authorization.");
        }

    }

}
