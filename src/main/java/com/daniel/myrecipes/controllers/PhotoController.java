package com.daniel.myrecipes.controllers;

import com.daniel.myrecipes.database.DatabaseManager;
import com.daniel.myrecipes.entities.Photo;
import com.daniel.myrecipes.entities.User;
import com.daniel.myrecipes.entities.daos.PhotoDao;
import com.daniel.myrecipes.networking.AuthorizationManager;
import com.daniel.myrecipes.networking.ResponseException;
import org.json.JSONObject;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Path("/photos")
public class PhotoController {

    @GET
    @Path("/{name}")
    public Response getPhotoByName(@PathParam("name") String name) {
        try {
            byte[] data = PhotoDao.loadImage(name);
            String encodedData = Base64.getEncoder().encodeToString(data);
            return Response.ok(encodedData).build();
        } catch (ResponseException e) {
            return e.getResponse();
        } catch (Exception e) {
            return ResponseException.createResponse(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    @POST
    @Path("/recipe/{recipe_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadPhoto(@Context HttpHeaders headers, String requestBody, @PathParam("recipe_id") int recipeId) {
        try (Connection con = DatabaseManager.getConnection()) {
            User user = AuthorizationManager.authorizeUsingHeaders(con, headers);
            Photo photo = PhotoDao.addPhoto(con, user, recipeId, requestBody);
            JSONObject responseBody = new JSONObject().put("photo", photo.toJSON());
            return Response.ok(responseBody.toString()).build();
        } catch (ResponseException e) {
            return e.getResponse();
        } catch (Exception e) {
            return ResponseException.createResponse(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    @DELETE
    @Path("/{name}")
    public Response deletePhoto(@Context HttpHeaders headers, @PathParam("name") String filename) {
        try (Connection con = DatabaseManager.getConnection()) {
            User user = AuthorizationManager.authorizeUsingHeaders(con, headers);
            PhotoDao.deletePhoto(con, user, filename);
            return Response.ok().build();
        } catch (ResponseException e) {
            return e.getResponse();
        } catch (Exception e) {
            return ResponseException.createResponse(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

}
