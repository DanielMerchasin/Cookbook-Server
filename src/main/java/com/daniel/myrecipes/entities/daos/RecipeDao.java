package com.daniel.myrecipes.entities.daos;

import com.daniel.myrecipes.entities.Ingredient;
import com.daniel.myrecipes.entities.MeasurementUnit;
import com.daniel.myrecipes.entities.Recipe;
import com.daniel.myrecipes.entities.User;
import com.daniel.myrecipes.utils.Logger;
import com.daniel.myrecipes.networking.ResponseException;
import com.daniel.myrecipes.utils.Utils;

import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RecipeDao {

    public static void updateRecipe(Connection con, Recipe recipe) throws Exception {

        // Update recipe
        try (PreparedStatement stmt = con.prepareStatement(
                "UPDATE recipes SET edit_time = ?, visible = ?" +
                        (recipe.getName() != null ? ", name = ?" : "") +
                        (recipe.getCategoryId() != 0 ? ", cat_id = ?" : "") +
                        (recipe.getNumServings() >= 0 ? ", num_servings = ?" : "") +
                        (recipe.getPrepTime() >= 0 ? ", prep_time = ?" : "") +
                        (recipe.getCookTime() >= 0 ? ", cook_time = ?" : "") +
                        (recipe.getNotes() != null ? ", notes = ?" : "") +
                        (recipe.getUploadTime() != 0 ? ", upload_time = ?" : "") +
                        (recipe.getSteps() != null ? ", steps = ?" : "") +
                        " WHERE id = ? LIMIT 1")) {

            stmt.setLong(1, recipe.getEditTime());
            stmt.setBoolean(2, recipe.isVisible());

            int i = 3;
            if (recipe.getName() != null)
                stmt.setString(i++, recipe.getName());

            if (recipe.getCategoryId() != 0)
                stmt.setInt(i++, recipe.getCategoryId());

            if (recipe.getNumServings() >= 0)
                stmt.setInt(i++, recipe.getNumServings());

            if (recipe.getPrepTime() >= 0)
                stmt.setInt(i++, recipe.getPrepTime());

            if (recipe.getCookTime() >= 0)
                stmt.setInt(i++, recipe.getCookTime());

            if (recipe.getNotes() != null)
                stmt.setString(i++, recipe.getNotes());

            if (recipe.getUploadTime() != 0)
                stmt.setLong(i++, recipe.getUploadTime());

            if (recipe.getSteps() != null)
                stmt.setBytes(i++, Utils.serialize(recipe.getSteps()));

            stmt.setInt(i, recipe.getId());

            stmt.executeUpdate();
        }

        if (recipe.getIngredients() != null) {
            // Delete all ingredients
            try (PreparedStatement stmt = con.prepareStatement(
                    "DELETE FROM recipe_ingredients WHERE recipe_id = ?")) {
                stmt.setInt(1, recipe.getId());
                stmt.executeUpdate();
            }

            // Insert ingredients
            for (Ingredient ingredient : recipe.getIngredients()) {
                try (PreparedStatement stmt = con.prepareStatement(
                        "INSERT INTO recipe_ingredients (recipe_id, name, quantity, unit, mandatory) " +
                                "VALUES (?, ?, ?, ?, ?)")) {
                    stmt.setInt(1, recipe.getId());
                    stmt.setString(2, ingredient.getName());
                    stmt.setDouble(3, ingredient.getQuantity());
                    stmt.setString(4, ingredient.getUnit().name());
                    stmt.setBoolean(5, ingredient.isMandatory());
                    stmt.executeUpdate();
                }
            }
        }
    }

    public static void deleteRecipe(Connection con, int recipeId, User user) throws Exception {

        Recipe recipe = getRecipeById(con, recipeId, user);
        if (recipe == null)
            throw new ResponseException(Response.Status.NOT_FOUND, "Recipe not found.");

        if (recipe.getUserId() != user.getId())
            throw new ResponseException(Response.Status.UNAUTHORIZED,
                    "You don't have the permissions to delete this recipe.");

        // Delete all ingredients
        try (PreparedStatement stmt = con.prepareStatement(
                "DELETE FROM recipe_ingredients WHERE recipe_id = ?")) {
            stmt.setInt(1, recipe.getId());
            stmt.executeUpdate();
        }

        // Delete all photos
        PhotoDao.deleteAllPhotos(con, user, recipe.getId());

        // Delete all ratings
        try (PreparedStatement stmt = con.prepareStatement(
                "DELETE FROM recipe_rating WHERE recipe_id = ?")) {
            stmt.setInt(1, recipe.getId());
            stmt.executeUpdate();
        }

        // Delete recipe
        try (PreparedStatement stmt = con.prepareStatement(
                "DELETE FROM recipes WHERE id = ?")) {
            stmt.setInt(1, recipe.getId());
            stmt.executeUpdate();
        }

    }

    public static int insertRecipe(Connection con, Recipe recipe) throws Exception {

        Logger.info("Inserting recipe: %s", recipe);

        int id = -1;

        // Insert the recipe
        try (PreparedStatement stmt = con.prepareStatement(
                "INSERT INTO recipes " +
                        "(name, cat_id, num_servings, prep_time, cook_time, notes, " +
                        "user_id, upload_time, edit_time, visible, steps) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, recipe.getName());
            stmt.setInt(2, recipe.getCategoryId());
            stmt.setInt(3, recipe.getNumServings());
            stmt.setInt(4, recipe.getPrepTime());
            stmt.setInt(5, recipe.getCookTime());
            stmt.setString(6, recipe.getNotes());
            stmt.setLong(7, recipe.getUserId());
            stmt.setLong(8, recipe.getUploadTime());
            stmt.setLong(9, recipe.getEditTime());
            stmt.setBoolean(10, recipe.isVisible());
            stmt.setBytes(11, Utils.serialize(recipe.getSteps()));

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next())
                    id = rs.getInt(1);
            }

        }

        Logger.info("Recipe ID: %d", id);

        Logger.info("Inserting ingredients: {%s}", Utils.stringJoin(", ", recipe.getIngredients()));

        // Insert ingredients
        for (Ingredient ingredient: recipe.getIngredients()) {
            try (PreparedStatement stmt = con.prepareStatement(
                    "INSERT INTO recipe_ingredients (recipe_id, name, quantity, unit, mandatory) " +
                            "VALUES (?, ?, ?, ?, ?)")) {
                stmt.setInt(1, id);
                stmt.setString(2, ingredient.getName());
                stmt.setDouble(3, ingredient.getQuantity());
                stmt.setString(4, ingredient.getUnit().name());
                stmt.setBoolean(5, ingredient.isMandatory());
                stmt.executeUpdate();
            }
        }

//            Logger.info("Inserting to user_recipes table: User ID = %d, Recipe ID = %d",
//                    recipe.getUserId(), recipe.getId());
//
//            // Add to user_recipes table
//            try (PreparedStatement stmt = con.prepareStatement(
//                    "INSERT INTO user_recipes (user_id, recipe_id) VALUES (?, ?)")) {
//                stmt.setInt(1, recipe.getUserId());
//                stmt.setInt(2, recipe.getId());
//                stmt.executeUpdate();
//            }

        Logger.info("Insert executed successfully!");

        return id;
    }

    private static Recipe buildRecipe(Connection con, ResultSet rs, User user) throws Exception {

        Recipe recipe = new Recipe();

        int recipeId = rs.getInt(1);

        recipe.setId(recipeId);
        recipe.setName(rs.getString(2));
        recipe.setCategoryId(rs.getInt(3));
        recipe.setNumServings(rs.getInt(4));
        recipe.setPrepTime(rs.getInt(5));
        recipe.setCookTime(rs.getInt(6));
        recipe.setNotes(rs.getString(7));
        recipe.setUserId(rs.getInt(8));
        recipe.setUploadTime(rs.getLong(9));
        recipe.setEditTime(rs.getLong(10));
        recipe.setVisible(rs.getBoolean(11));
        recipe.setSteps(Utils.deserialize(rs.getBytes(12)));
        recipe.setUsername(rs.getString(13));
        recipe.setCategoryName(rs.getString(14));

        // Get ingredients
        try (PreparedStatement stmtIngredients = con.prepareStatement(
                "SELECT * FROM recipe_ingredients WHERE recipe_id = ?")) {

            stmtIngredients.setInt(1, recipeId);

            ArrayList<Ingredient> ingredients = new ArrayList<>();

            try (ResultSet rsIngredients = stmtIngredients.executeQuery()) {
                while (rsIngredients.next()) {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setId(rsIngredients.getInt(1));
                    ingredient.setRecipeId(rsIngredients.getInt(2));
                    ingredient.setName(rsIngredients.getString(3));
                    ingredient.setQuantity(rsIngredients.getDouble(4));
                    ingredient.setUnit(MeasurementUnit.valueOf(rsIngredients.getString(5)));
                    ingredient.setMandatory(rsIngredients.getBoolean(6));
                    ingredients.add(ingredient);
                }
            }

            recipe.setIngredients(ingredients);
        }

        // Get photos
        recipe.setPhotos(PhotoDao.getPhotos(con, recipeId));

        // Get rating
        try (PreparedStatement stmtRating = con.prepareStatement(
                "SELECT AVG(rating) FROM recipe_rating WHERE recipe_id = ? GROUP BY recipe_id")) {
            stmtRating.setInt(1, recipeId);
            try (ResultSet rsRating = stmtRating.executeQuery()) {
                if (rsRating.next()) {
                    float rating = rsRating.getFloat(1);
                    recipe.setRating(rating);
                }
            }
        }

        // Check if the recipe is in the user's favorites
        if (user != null) {
            try (PreparedStatement stmtFavorites = con.prepareStatement(
                    "SELECT * FROM user_favorites WHERE user_id = ? AND recipe_id = ? LIMIT 1")) {
                stmtFavorites.setInt(1, user.getId());
                stmtFavorites.setInt(2, recipeId);
                try (ResultSet rsFavorites = stmtFavorites.executeQuery()) {
                    if (rsFavorites.next()) {
                        recipe.setFavorite(true);
                    }
                }
            }
        }

        return recipe;
    }

    /**
     * Get recipe by ID
     * @param id
     * @return
     */
    public static Recipe getRecipeById(Connection con, int id, User user) throws Exception {

        // Get recipe
        try (PreparedStatement stmt = con.prepareStatement(
                "SELECT r.*, u.username, c.name AS cat_name FROM recipes r " +
                        "INNER JOIN users u ON r.user_id = u.id " +
                        "INNER JOIN categories c ON r.cat_id = c.id " +
                        "WHERE r.id = ? AND (visible = 1" + (user != null ? " OR r.user_id = ?" : "") + ") " +
                        "LIMIT 1")) {

            stmt.setInt(1, id);

            if (user != null)
                stmt.setInt(2, user.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return buildRecipe(con, rs, user);
                }
            }
        }

        return null;

    }

    public static List<Recipe> getLatestRecipes(Connection con, User user, int categoryId, int pageNum,
                                                int recipesPerPage, long maxUploadTime) throws Exception {

        List<Recipe> recipes = new ArrayList<>();

        try (PreparedStatement stmt = con.prepareStatement(
                "SELECT r.*, u.username, c.name AS cat_name FROM recipes r " +
                        "INNER JOIN users u ON r.user_id = u.id " +
                        "INNER JOIN categories c ON r.cat_id = c.id " +
                        "WHERE (visible = 1" + (user != null ? " OR r.user_id = ?" : "") + ") " +
                        (maxUploadTime > 0 ? "AND r.upload_time < ? " : "") +
                        (categoryId > 0 ? "AND r.cat_id = ? " : "") +
                        "ORDER BY r.id DESC LIMIT ? OFFSET ?")) {

            int i = 1;
            if (user != null)
                stmt.setInt(i++, user.getId());

            if (maxUploadTime > 0)
                stmt.setLong(i++, maxUploadTime);

            if (categoryId > 0)
                stmt.setInt(i++, categoryId);

            stmt.setInt(i++, recipesPerPage);
            stmt.setInt(i, pageNum * recipesPerPage);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    recipes.add(buildRecipe(con, rs, user));
                }
            }
        }

        return recipes;
    }

    public static List<Recipe> getAllUserRecipes(Connection con, User user) throws Exception {

        List<Recipe> recipes = new ArrayList<>();

        try (PreparedStatement stmt = con.prepareStatement(
                "SELECT r.*, u.username, c.name AS cat_name FROM recipes r " +
                        "INNER JOIN users u ON r.user_id = u.id " +
                        "INNER JOIN categories c ON r.cat_id = c.id " +
                        "WHERE r.user_id = ?")) {

            stmt.setInt(1, user.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    recipes.add(buildRecipe(con, rs, user));
                }
            }
        }

        return recipes;

    }

    public static List<Recipe> getUserFavoriteRecipes(Connection con, User user) throws Exception {

        List<Recipe> recipes = new ArrayList<>();

        try (PreparedStatement stmt = con.prepareStatement(
                "SELECT r.*, u.username, c.name AS cat_name, uf.user_id FROM recipes r " +
                        "INNER JOIN users u ON r.user_id = u.id " +
                        "INNER JOIN categories c ON r.cat_id = c.id " +
                        "INNER JOIN user_favorites uf ON r.id = uf.recipe_id " +
                        "WHERE uf.user_id = ?")) {

            stmt.setInt(1, user.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    recipes.add(buildRecipe(con, rs, user));
                }
            }
        }

        return recipes;
    }

    public static void addToUserFavorites(Connection con, User user, int recipeId) throws Exception {
        try (PreparedStatement stmt = con.prepareStatement(
                "INSERT INTO user_favorites (user_id, recipe_id) VALUES (?, ?)")) {
            stmt.setInt(1, user.getId());
            stmt.setInt(2, recipeId);
            stmt.executeUpdate();
        }
    }

    public static void removeFromUserFavorites(Connection con, User user, int recipeId) throws Exception {
        try (PreparedStatement stmt = con.prepareStatement(
                "DELETE FROM user_favorites WHERE user_id = ? AND recipe_id = ?")) {
            stmt.setInt(1, user.getId());
            stmt.setInt(2, recipeId);
            stmt.executeUpdate();
        }
    }

    public static List<Recipe> performSearch(Connection con, String q, int[] categoryIds,
                                             int maxTime, User user, int pageNum,
                                             int recipesPerPage, String sort, long maxUploadTime) throws Exception {

//        Set<Recipe> recipeSet = new HashSet<>();
        List<Recipe> recipes = new ArrayList<>();

        // Remove all non alphanumerical characters from search query
        String searchQuery = q != null ? q.trim().replaceAll("[^a-zA-Z0-9\\s]", "") : "";

        int userId = user != null ? user.getId() : 0;

        try (PreparedStatement stmt = con.prepareStatement(
                "SELECT r.*, u.username, c.name AS cat_name FROM recipes r " +
                        "INNER JOIN users u ON r.user_id = u.id " +
                        "INNER JOIN categories c ON r.cat_id = c.id " +
                        "WHERE r.name REGEXP ? "
                        + (categoryIds != null && categoryIds.length > 0
                        ? "AND r.cat_id IN ("
                        + String.join(", ", Collections.nCopies(categoryIds.length, "?")) + ") " : "")
                        + (maxTime > 0 ? "AND (r.cook_time+r.prep_time) <= ? " : "")
                        + (userId != 0 ? "AND (r.visible = 1 OR r.user_id = ?) " : "AND r.visible = 1 ")
                        + (maxUploadTime > 0 ? "AND r.upload_time <= ? " : "")
                        + "LIMIT ? OFFSET ?")) {

            stmt.setString(1, searchQuery);

            int i = 2;

            if (categoryIds != null && categoryIds.length > 0) {
                for (int categoryId: categoryIds)
                    stmt.setInt(i++, categoryId);
            }

            if (maxTime > 0)
                stmt.setInt(i++, maxTime);

            if (userId != 0)
                stmt.setInt(i++, userId);

            if (maxUploadTime > 0)
                stmt.setLong(i++, maxUploadTime);

            stmt.setInt(i++, recipesPerPage);
            stmt.setInt(i, pageNum * recipesPerPage);

            Logger.info("Search by name statement: %s", stmt);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    if (recipes.size() == recipesPerPage)
                        break;

                    Recipe recipe = buildRecipe(con, rs, user);
//                        recipeSet.add(recipe);
                    recipes.add(recipe);
                }
            }

        }

        // Sort the recipes by the requested parameter
        if (sort.equalsIgnoreCase("category")) {
            // Category ID ASC
            recipes.sort(Comparator.comparingInt(Recipe::getCategoryId));
        } else if (sort.equalsIgnoreCase("cook_time")) {
            // Cook time ASC
            recipes.sort(Comparator.comparingInt(Recipe::getCookTime));
        } else if (sort.equalsIgnoreCase("prep_time")) {
            // Prep time ASC
            recipes.sort(Comparator.comparingInt(Recipe::getPrepTime));
        } else if (sort.equalsIgnoreCase("total_time")) {
            // Prep time + cook time ASC
            recipes.sort(Comparator.comparingInt(o -> (o.getPrepTime() + o.getCookTime())));
        } else if (sort.equalsIgnoreCase("latest")) {
            // Upload time DESC
            recipes.sort((o1, o2) -> Long.compare(o2.getUploadTime(), o1.getUploadTime()));
        } else {
            // Rating DESC - default
            recipes.sort(((o1, o2) -> Float.compare(o2.getRating(), o1.getRating())));
        }

        // If there are enough recipes already, return the current list
//            if (recipes.size() == recipesPerPage)
//                return recipes;

        // Search by ingredients
//            try (PreparedStatement stmt = con.prepareStatement(
//                    "SELECT r.*, u.username, c.name AS cat_name, " +
//                            "(SELECT COUNT(*) FROM recipe_ingredients " +
//                            "WHERE recipe_ingredients.name REGEXP '(" +
//                            searchQuery.replaceAll("\\s+", "|") +
//                            ")' GROUP BY recipe_id) AS icnt FROM recipes r " +
//                            "INNER JOIN users u ON r.user_id = u.id " +
//                            "INNER JOIN categories c ON r.cat_id = c.id " +
//                            (categoryId != 0 ? "AND r.cat_id = ? " : "") +
//                            (maxTime > 0 ? "AND (r.prep_time+r.cook_time) <= ? " : "") +
//                            (userId != 0 ? "AND (r.visible = 1 OR r.user_id = ?) " : "AND r.visible = 1 ") +
//                            (maxUploadTime > 0 ? "AND r.upload_time <= ? " : "") +
//                            "HAVING icnt > 0 ORDER BY icnt DESC LIMIT ? OFFSET ?")) {
//
//                int i = 1;
//                if (categoryId != 0)
//                    stmt.setInt(i++, categoryId);
//
//                if (maxTime > 0)
//                    stmt.setInt(i++, maxTime);
//
//                if (userId != 0)
//                    stmt.setInt(i++, userId);
//
//                if (maxUploadTime > 0)
//                    stmt.setLong(i++, maxUploadTime);
//
//                stmt.setInt(i++, recipesPerPage - recipes.size());
//                stmt.setInt(i, );
//
//                Logger.info("Search by ingredients statement: %s", stmt);
//
//                try (ResultSet rs = stmt.executeQuery()) {
//                    while (rs.next()) {
//                        if (recipes.size() == recipesPerPage)
//                            break;
//
//                        Recipe recipe = buildRecipe(con, rs);
//                        if (!recipeSet.contains(recipe))
//                            recipes.add(recipe);
//                    }
//                }
//
//            }


        return recipes;

    }

    public static void rateRecipe(Connection con, User user, int recipeId, float rating) throws Exception {

        // Check if the recipe exists and if the user is trying to rate their own recipe
        try (PreparedStatement stmt = con.prepareStatement(
                "SELECT user_id FROM recipes WHERE id = ? LIMIT 1")) {
            stmt.setInt(1, recipeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    if (rs.getInt(1) == user.getId())
                        throw new ResponseException(Response.Status.BAD_REQUEST, "You can't rate your own recipe.");
                } else {
                    throw new ResponseException(Response.Status.NOT_FOUND, "Recipe not found.");
                }
            }
        }

        // Check if the rating already exists
        try (PreparedStatement stmt = con.prepareStatement(
                "SELECT recipe_id FROM recipe_rating WHERE recipe_id = ? AND user_id = ? LIMIT 1")) {
            stmt.setInt(1, recipeId);
            stmt.setInt(2, user.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Exists - Update
                    try (PreparedStatement stmtUpdate = con.prepareStatement(
                            "UPDATE recipe_rating SET rating = ? WHERE recipe_id = ? AND user_id = ? LIMIT 1")) {
                        stmtUpdate.setFloat(1, rating);
                        stmtUpdate.setInt(2, recipeId);
                        stmtUpdate.setInt(3, user.getId());
                        stmtUpdate.executeUpdate();
                    }
                } else {
                    // Doesn't exist - Insert
                    try (PreparedStatement stmtInsert = con.prepareStatement(
                            "INSERT INTO recipe_rating (recipe_id, user_id, rating) " +
                                    "VALUES (?, ?, ?)")) {
                        stmtInsert.setInt(1, recipeId);
                        stmtInsert.setInt(2, user.getId());
                        stmtInsert.setFloat(3, rating);
                        stmtInsert.executeUpdate();
                    }
                }
            }
        }

    }

    public static boolean matchUserRecipe(Connection con, User user, int recipeId) throws Exception {
        try (PreparedStatement stmt = con.prepareStatement(
                     "SELECT user_id FROM recipes WHERE id = ? LIMIT 1")) {
            stmt.setInt(1, recipeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return user.getId() == rs.getInt(1);
                }
            }
        }
        return false;
    }

}
