package com.daniel.myrecipes.entities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class Recipe implements Serializable {

    private static final long serialVersionUID = 2L;

    private int id;
    private String name;
    private int categoryId;
    private String categoryName;
    private int numServings;
    private int prepTime;
    private int cookTime;
    private String notes;
    private long uploadTime;
    private long editTime;
    private float rating;
    private boolean visible;
    private int userId;
    private String username;
    private ArrayList<Ingredient> ingredients;
    private ArrayList<String> steps;
    private ArrayList<Photo> photos;
    private boolean favorite;

    public Recipe() {
        ingredients = new ArrayList<>();
        steps = new ArrayList<>();
        photos = new ArrayList<>();
        rating = -1.0f;
    }

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

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getNumServings() {
        return numServings;
    }

    public void setNumServings(int numServings) {
        this.numServings = numServings;
    }

    public int getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(int prepTime) {
        this.prepTime = prepTime;
    }

    public int getCookTime() {
        return cookTime;
    }

    public void setCookTime(int cookTime) {
        this.cookTime = cookTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(long uploadTime) {
        this.uploadTime = uploadTime;
    }

    public long getEditTime() {
        return editTime;
    }

    public void setEditTime(long editTime) {
        this.editTime = editTime;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ArrayList<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(ArrayList<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public ArrayList<String> getSteps() {
        return steps;
    }

    public void setSteps(ArrayList<String> steps) {
        this.steps = steps;
    }

    public ArrayList<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(ArrayList<Photo> photos) {
        this.photos = photos;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj instanceof Recipe) {
            Recipe other = (Recipe) obj;
            return this.id == other.id;
        }
        return false;
    }

    @Override
    public String toString() {
        return name + " by " + username;
    }

    public String description() {
        StringBuilder sb = new StringBuilder();

        sb.append("[").append(id).append("] ").append(name).append(" ").append("\n")
                .append("Uploaded: ").append(uploadTime).append("\n")
                .append("Public: ").append(visible).append("\n")
                .append("Rating: ").append(rating).append("\n\n")
                .append("Number of servings: ").append(numServings).append("\n")
                .append("Preparation Time: ").append(prepTime).append(" minutes").append("\n")
                .append("Cooking Time: ").append(cookTime).append(" minutes").append("\n")
                .append("Total Time: ").append(prepTime + cookTime).append(" minutes").append("\n");

        if (ingredients != null) {
            sb.append("\nIngredients:\n");
            for (Ingredient ingredient : ingredients)
                sb.append(ingredient).append("\n");
        }

        if (steps != null) {
            sb.append("\nSteps:\n");
            for (int i = 0; i < steps.size(); i++) {
                sb.append(i + 1).append(". ").append(steps.get(i)).append("\n");
            }
        }

        sb.append("\nNotes:\n").append(notes);

        return sb.toString();
    }

    public JSONObject toJSON() {
        JSONObject data = new JSONObject();

        if (id != 0)
            data.put("id", id);

        if (name != null)
            data.put("name", name);

        if (categoryId != 0)
            data.put("category_id", categoryId);

        if (categoryName != null)
            data.put("cat_name", categoryName);

        if (numServings != 0)
            data.put("num_servings", numServings);

        if (notes != null)
            data.put("notes", notes);

        if (uploadTime != 0)
            data.put("upload_time", uploadTime);

        if (editTime != 0)
            data.put("edit_time", editTime);

        if (rating != -1)
            data.put("rating", rating);

        if (userId != 0)
            data.put("user_id", userId);

        if (username != null)
            data.put("username", username);

        data.put("prep_time", prepTime)
                .put("cook_time", cookTime)
                .put("favorite", favorite)
                .put("visible", visible);

        JSONArray ingredientsArray = new JSONArray();
        for (Ingredient ingredient: ingredients)
            ingredientsArray.put(ingredient.toJSON());
        data.put("ingredients", ingredientsArray);

        JSONArray stepsArray = new JSONArray();
        for (String step: steps)
            stepsArray.put(step);
        data.put("steps", stepsArray);

        JSONArray photosArray = new JSONArray();
        for (Photo photo: photos)
            photosArray.put(photo.toJSON());
        data.put("photos", photosArray);

        return data;
    }

    public static Recipe fromJSON(JSONObject data) throws JSONException {
        Recipe recipe = new Recipe();

        if (data.has("id"))
            recipe.setId(data.getInt("id"));

        recipe.setName(data.getString("name"));

        if (data.has("category_id"))
            recipe.setCategoryId(data.getInt("category_id"));

        if (data.has("num_servings"))
            recipe.setNumServings(data.getInt("num_servings"));

        if (data.has("prep_time"))
            recipe.setPrepTime(data.getInt("prep_time"));

        if (data.has("cook_time"))
            recipe.setCookTime(data.getInt("cook_time"));

        if (data.has("notes"))
            recipe.setNotes(data.getString("notes"));

        if (data.has("upload_time"))
            recipe.setUploadTime(data.getLong("upload_time"));

        if (data.has("edit_time"))
            recipe.setEditTime(data.getLong("edit_time"));

        if (data.has("rating"))
            recipe.setRating(data.getFloat("rating"));

        if (data.has("visible"))
            recipe.setVisible(data.getBoolean("visible"));

        if (data.has("user_id"))
            recipe.setUserId(data.getInt("user_id"));

        if (data.has("favorite"))
            recipe.setFavorite(data.getBoolean("favorite"));

        if (data.has("ingredients")) {
            ArrayList<Ingredient> ingredients = new ArrayList<>();
            JSONArray ingredientArray = data.getJSONArray("ingredients");
            for (int i = 0; i < ingredientArray.length(); i++)
                ingredients.add(Ingredient.fromJSON(ingredientArray.getJSONObject(i)));
            recipe.setIngredients(ingredients);
        }

        if (data.has("steps")) {
            ArrayList<String> steps = new ArrayList<>();
            JSONArray stepArray = data.getJSONArray("steps");
            for (int i = 0; i < stepArray.length(); i++)
                steps.add(stepArray.getString(i));
            recipe.setSteps(steps);
        }

//        if (data.has("photos")) {
//            ArrayList<Photo> photos = new ArrayList<>();
//            JSONArray photoArray = data.getJSONArray("photos");
//            for (int i = 0; i < photoArray.length(); i++)
//                photos.add(Photo.fromJSON(photoArray.getJSONObject(i)));
//            recipe.setPhotos(photos);
//        }

        return recipe;
    }

    public static class Builder {

        private Recipe recipe;

        public Builder(String name) {
            recipe = new Recipe();
            recipe.setName(name);
        }

        public Builder setId(int id) {
            recipe.setId(id);
            return this;
        }

        public Builder setCategory(int categoryId, String categoryName) {
            recipe.setCategoryId(categoryId);
            recipe.setCategoryName(categoryName);
            return this;
        }

        public Builder setCategory(int categoryId) {
            recipe.setCategoryId(categoryId);
            return this;
        }

        public Builder setNumServings(int numServings) {
            recipe.setNumServings(numServings);
            return this;
        }

        public Builder setTime(int prepTime, int cookTime) {
            recipe.setPrepTime(prepTime);
            recipe.setCookTime(cookTime);
            return this;
        }

        public Builder setPrepTime(int prepTime) {
            recipe.setPrepTime(prepTime);
            return this;
        }

        public Builder setCookTime(int cookTime) {
            recipe.setCookTime(cookTime);
            return this;
        }

        public Builder setNotes(String notes) {
            recipe.setNotes(notes);
            return this;
        }

        public Builder setUploadTime(long uploadTime) {
            recipe.setUploadTime(uploadTime);
            return this;
        }

        public Builder setEditTime(long editTime) {
            recipe.setEditTime(editTime);
            return this;
        }

        public Builder setRating(float rating) {
            recipe.setRating(rating);
            return this;
        }

        public Builder setVisible(boolean visible) {
            recipe.setVisible(visible);
            return this;
        }

        public Builder setUserId(int userId) {
            recipe.setUserId(userId);
            return this;
        }

        public Builder setUsername(String username) {
            recipe.setUsername(username);
            return this;
        }

        public Builder setUser(int userId, String username) {
            recipe.setUserId(userId);
            recipe.setUsername(username);
            return this;
        }

        public Builder setUser(User user) {
            return setUser(user.getId(), user.getUsername());
        }

        public Builder setIngredients(ArrayList<Ingredient> ingredients) {
            recipe.setIngredients(ingredients);
            return this;
        }

        public Builder addIngredients(Ingredient... ingredients) {
            for (Ingredient ingredient: ingredients)
                recipe.getIngredients().add(ingredient);
            return this;
        }

        public Builder setSteps(ArrayList<String> steps) {
            recipe.setSteps(steps);
            return this;
        }

        public Builder addSteps(String... steps) {
            for (String step: steps)
                recipe.getSteps().add(step);
            return this;
        }

        public Builder setPhotos(ArrayList<Photo> photos) {
            for (Photo photo: photos)
                recipe.getPhotos().add(photo);
            return this;
        }

        public Recipe build() {
            return recipe;
        }

    }

}
