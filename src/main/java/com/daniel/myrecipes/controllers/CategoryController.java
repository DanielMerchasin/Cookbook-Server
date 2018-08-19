package com.daniel.myrecipes.controllers;

import com.daniel.myrecipes.database.DatabaseManager;
import com.daniel.myrecipes.entities.Category;
import com.daniel.myrecipes.entities.daos.CategoryDao;
import com.daniel.myrecipes.networking.ResponseException;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.util.List;

@Path("categories")
public class CategoryController {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCategories() {
        try (Connection con = DatabaseManager.getConnection()) {
            List<Category> categories = CategoryDao.getAllCategories(con);

            JSONObject data = new JSONObject();
            JSONArray categoryArray = new JSONArray();
            for (Category category: categories)
                categoryArray.put(category.toJSON());
            data.put("categories", categoryArray);

            return Response.ok(data.toString()).build();
        } catch (Exception e) {
            return ResponseException.createResponse(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

}
