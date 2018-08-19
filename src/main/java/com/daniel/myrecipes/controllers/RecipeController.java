package com.daniel.myrecipes.controllers;

import com.daniel.myrecipes.database.DatabaseManager;
import com.daniel.myrecipes.entities.Category;
import com.daniel.myrecipes.entities.Recipe;
import com.daniel.myrecipes.entities.User;
import com.daniel.myrecipes.entities.daos.CategoryDao;
import com.daniel.myrecipes.entities.daos.RecipeDao;
import com.daniel.myrecipes.networking.AuthorizationManager;
import com.daniel.myrecipes.networking.ResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.util.List;

@Path("/recipes")
public class RecipeController {

    @GET
    @Path("/id/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRecipe(@Context HttpHeaders headers, @PathParam("id") int id) {
        try (Connection con = DatabaseManager.getConnection()) {
            User user = AuthorizationManager.authorizeUsingHeadersSafely(con, headers);
            Recipe recipe = RecipeDao.getRecipeById(con, id, user);
            if (recipe != null) {
                return Response.ok().entity(recipe.toJSON().toString()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (ResponseException e) {
            return e.getResponse();
        } catch (Exception e) {
            return ResponseException.createResponse(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    @GET
    @Path("/user")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUserRecipes(@Context HttpHeaders headers) {
        try (Connection con = DatabaseManager.getConnection()) {
            User user = AuthorizationManager.authorizeUsingHeaders(con, headers);
            List<Recipe> recipes = RecipeDao.getAllUserRecipes(con, user);

            return responseFromRecipeList(recipes);
        } catch (ResponseException e) {
            return e.getResponse();
        } catch (Exception e) {
            return ResponseException.createResponse(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    @GET
    @Path("/latest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLatestRecipes(@Context HttpHeaders headers,
                                     @QueryParam("cat_id") int categoryId,
                                     @QueryParam("page_num") int pageNum,
                                     @DefaultValue("30") @QueryParam("recipes_per_page") int recipesPerPage,
                                     @QueryParam("max_upload_time") long maxUploadTime) {

        try (Connection con = DatabaseManager.getConnection()) {
            User user = AuthorizationManager.authorizeUsingHeadersSafely(con, headers);
            List<Recipe> recipes = RecipeDao.getLatestRecipes(con, user, categoryId,
                    pageNum, recipesPerPage, maxUploadTime);

            return responseFromRecipeList(recipes);
        } catch (ResponseException e) {
            return e.getResponse();
        } catch (Exception e) {
            return ResponseException.createResponse(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchRecipes(@Context HttpHeaders headers,
                                     @QueryParam("q") String q,
                                     @DefaultValue("") @QueryParam("cat_id") String categoryIds,
                                     @QueryParam("max_time") int maxTime,
                                     @QueryParam("page_num") int pageNum,
                                     @DefaultValue("30") @QueryParam("recipes_per_page") int recipesPerPage,
                                     @DefaultValue("") @QueryParam("sort") String sort,
                                     @QueryParam("max_upload_time") long maxUploadTime) {

        try (Connection con = DatabaseManager.getConnection()) {
            User user = AuthorizationManager.authorizeUsingHeadersSafely(con, headers);

            // Convert string of category IDs separated by commas to int array
            int[] categories = null;
            if (!categoryIds.trim().isEmpty()) {
                String[] categoryStrings = categoryIds.split("\\s*,\\s*");
                categories = new int[categoryStrings.length];
                try {
                    for (int i = 0; i < categoryStrings.length; i++)
                        categories[i] = Integer.valueOf(categoryStrings[i]);
                } catch (NumberFormatException e) {
                    return ResponseException.createResponse(Response.Status.BAD_REQUEST,
                            "Value of \"cat_id\" must consist of numbers and commas only.");
                }
            }

            List<Recipe> recipes = RecipeDao.performSearch(con, q, categories, maxTime,
                    user, pageNum, recipesPerPage, sort, maxUploadTime);

            return responseFromRecipeList(recipes);

        } catch (ResponseException e) {
            return e.getResponse();
        } catch (Exception e) {
            return ResponseException.createResponse(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateRecipe(@Context HttpHeaders headers, String requestBody) {
        try (Connection con = DatabaseManager.getConnection()) {
            User user = AuthorizationManager.authorizeUsingHeaders(con, headers);

            JSONObject data = new JSONObject(requestBody);
            Recipe recipe = Recipe.fromJSON(data);

            if (!RecipeDao.matchUserRecipe(con, user, recipe.getId()))
                throw new ResponseException(Response.Status.UNAUTHORIZED, "You can't update this recipe.");

            recipe.setUserId(user.getId());
            recipe.setUsername(user.getUsername());
            recipe.setEditTime(System.currentTimeMillis());

            RecipeDao.updateRecipe(con, recipe);

            return Response.ok(recipe.toJSON().toString()).build();

        } catch (ResponseException e) {
            return e.getResponse();
        } catch (Exception e) {
            return ResponseException.createResponse(Response.Status.INTERNAL_SERVER_ERROR, e);
        }

    }

    @DELETE
    @Path("/id/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteRecipe(@Context HttpHeaders headers, @PathParam("id") int recipeId) {
        try (Connection con = DatabaseManager.getConnection()) {
            User user = AuthorizationManager.authorizeUsingHeaders(con, headers);
            RecipeDao.deleteRecipe(con, recipeId, user);
            return Response.ok("Recipe Deleted Successfully.").build();
        } catch (ResponseException e) {
            return e.getResponse();
        } catch (Exception e) {
            return ResponseException.createResponse(Response.Status.INTERNAL_SERVER_ERROR, e);
        }

    }

    @POST
    public Response addRecipe(@Context HttpHeaders headers, String requestBody) {
        try (Connection con = DatabaseManager.getConnection()) {
            User user = AuthorizationManager.authorizeUsingHeaders(con, headers);

            JSONObject data = new JSONObject(requestBody);
            Recipe recipe = Recipe.fromJSON(data);

            // Check if the category exists
            Category category = CategoryDao.getCategoryById(con, recipe.getCategoryId());

            if (category == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Category ID " + recipe.getCategoryId() + " not found").build();
            }

            recipe.setUserId(user.getId());
            recipe.setUsername(user.getUsername());

            long now = System.currentTimeMillis();
            recipe.setEditTime(now);
            recipe.setUploadTime(now);

            int recipeId = RecipeDao.insertRecipe(con, recipe);
            recipe.setId(recipeId);

            return Response.ok(recipe.toJSON().toString()).build();
        } catch (JSONException e) {
            return ResponseException.createResponse(Response.Status.BAD_REQUEST, e);
        } catch (ResponseException e) {
            return e.getResponse();
        } catch (Exception e) {
            return ResponseException.createResponse(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    @POST
    @Path("/rate")
    public Response rateRecipes(@Context HttpHeaders headers,
                                @QueryParam("recipe_id") int id,
                                @QueryParam("rating") float rating) {

        try {
            // First check if rating value is valid
            if (rating > 100 || rating < 0)
                throw new ResponseException(Response.Status.BAD_REQUEST,
                        "Rating value must be between 0 and 100.");

            try (Connection con = DatabaseManager.getConnection()) {
                User user = AuthorizationManager.authorizeUsingHeaders(con, headers);
                RecipeDao.rateRecipe(con, user, id, rating);
                return Response.ok().build();
            }
        } catch (ResponseException e) {
            return e.getResponse();
        } catch (Exception e) {
            return ResponseException.createResponse(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    @GET
    @Path("/favorites")
    public Response getUserFavorites(@Context HttpHeaders headers) {
        try (Connection con = DatabaseManager.getConnection()) {
            User user = AuthorizationManager.authorizeUsingHeaders(con, headers);
            List<Recipe> recipes = RecipeDao.getUserFavoriteRecipes(con, user);

            return responseFromRecipeList(recipes);

        } catch (ResponseException e) {
            return e.getResponse();
        } catch (Exception e) {
            return ResponseException.createResponse(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    @POST
    @Path("/favorites")
    public Response addFavoriteRecipe(@Context HttpHeaders headers, @QueryParam("recipe_id") int recipeId) {
        try (Connection con = DatabaseManager.getConnection()) {
            User user = AuthorizationManager.authorizeUsingHeaders(con, headers);
            RecipeDao.addToUserFavorites(con, user, recipeId);
            return Response.ok().build();
        } catch (ResponseException e) {
            return e.getResponse();
        } catch (Exception e) {
            return ResponseException.createResponse(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    @DELETE
    @Path("/favorites")
    public Response deleteFavoriteRecipe(@Context HttpHeaders headers, @QueryParam("recipe_id") int recipeId) {
        try (Connection con = DatabaseManager.getConnection()) {
            User user = AuthorizationManager.authorizeUsingHeaders(con, headers);
            RecipeDao.removeFromUserFavorites(con, user, recipeId);
            return Response.ok().build();
        } catch (ResponseException e) {
            return e.getResponse();
        } catch (Exception e) {
            return ResponseException.createResponse(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

    public static Response responseFromRecipeList(List<Recipe> recipes) {
        if (recipes != null) {

            JSONObject data = new JSONObject();
            JSONArray array  = new JSONArray();
            for (Recipe recipe: recipes)
                array.put(recipe.toJSON());
            data.put("recipes", array);

            return Response.ok().entity(data.toString()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

//
//    @POST
//    @Path("/images")
//    public Response uploadImage(@Context HttpHeaders headers, @QueryParam("recipe_id") int recipeId, String encodedImageData) {
//        try (Connection con = DatabaseManager.getConnection()) {
//            User user = AuthorizationManager.authorizeUsingHeaders(con, headers);
//
//            if (RecipeDao.matchUserRecipe(con, user, recipeId)) {
//
//                try {
//                    PhotoDao.addPhoto(con, user, recipeId, encodedImageData);
//                } catch (Exception e) {
//                    // TODO: Handle exception
//                }
//
//                return Response.ok().build();
//
//            } else {
//                return Response.status(Response.Status.UNAUTHORIZED).build();
//            }
//        } catch (JSONException e) {
//            return Response.status(Response.Status.BAD_REQUEST).build();
//        } catch (ResponseException e) {
//            return e.getResponse();
//        } catch (Exception e) {
//            return Response.serverError().build();
//        }
//    }

}
