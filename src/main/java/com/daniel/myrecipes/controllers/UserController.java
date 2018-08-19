package com.daniel.myrecipes.controllers;

import com.daniel.myrecipes.database.DatabaseManager;
import com.daniel.myrecipes.entities.daos.UserDao;
import com.daniel.myrecipes.networking.AuthorizationManager;
import com.daniel.myrecipes.entities.User;
import com.daniel.myrecipes.networking.ResponseException;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;

@Path("/users")
public class UserController {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@Context HttpHeaders headers) {
        try (Connection con = DatabaseManager.getConnection()) {
            User user = AuthorizationManager.authorizeUsingHeaders(con, headers);
            return Response.ok(user.toJSON().toString()).build();
        } catch (ResponseException e) {
            return e.getResponse();
        } catch (Exception e) {
            return ResponseException.createResponse(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    @POST
    @Path("/register")
    public Response registerUser(String requestBody) {
        try (Connection con = DatabaseManager.getConnection()) {
            JSONObject data = new JSONObject(requestBody);
            User user = UserDao.addUser(con, data.getString("username"), data.getString("password"));
            return Response.ok(user.toJSON().toString()).build();
        } catch (JSONException e) {
            return ResponseException.createResponse(Response.Status.BAD_REQUEST, e);
        } catch (ResponseException e) {
            return e.getResponse();
        } catch (Exception e) {
            return ResponseException.createResponse(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    @POST
    @Path("/update")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePassword(@Context HttpHeaders headers, String requestBody) {
        try (Connection con = DatabaseManager.getConnection()) {
            User user = AuthorizationManager.authorizeUsingHeaders(con, headers);
            UserDao.updatePassword(con, user, requestBody);
            return Response.ok().build();
        } catch (ResponseException e) {
            return e.getResponse();
        } catch (Exception e) {
            return ResponseException.createResponse(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

}
