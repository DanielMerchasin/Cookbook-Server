package com.daniel.myrecipes.entities.daos;

import com.daniel.myrecipes.entities.Ingredient;
import com.daniel.myrecipes.entities.MeasurementUnit;
import com.daniel.myrecipes.entities.Recipe;
import com.daniel.myrecipes.entities.User;
import org.junit.Test;

import java.util.List;

public class RecipeDaoTest {

    @Test
    public void testAddUser() {

        try {

            //User user = UserDao.addUser("admin", "1234");

            //System.out.println(user);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testRecipeSelect() {
        try {

//            User user = UserDao.getUser("yo", "wassup");
//
//            if (user == null)
//                throw new Exception("User doesn't exist");
//
//            long now = System.currentTimeMillis();
//
//            List<Recipe> recipes = RecipeDao.getLatestRecipes(user, 0, 0, 30, now);
//
//            for (Recipe recipe : recipes) {
//                System.out.println(recipe.description());
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRecipeInsert() {

        try {

//            long now = System.currentTimeMillis();
//
//            Recipe recipe = new Recipe.Builder("Cooked Rice")
//                    .setEditTime(now)
//                    .setUploadTime(now)
//                    .setTime(5, 20)
//                    .setNumServings(4)
//                    .setUserId(1)
//                    .setCategory(11, "Main Dishes: Vegetarian")
//                    .setVisible(true)
//                    .addIngredients(
//                            new Ingredient("Oil", 1, MeasurementUnit.TBSP),
//                            new Ingredient("Rice", 3, MeasurementUnit.CUP),
//                            new Ingredient("Water", 5, MeasurementUnit.LITER),
//                            new Ingredient("Salt", 0.5d, MeasurementUnit.TSP))
//                    .addSteps(
//                            "Pour oil in pan",
//                            "Add rice and stir",
//                            "Add salt",
//                            "Add water, boil, close cap and wait 15 minutes",
//                            "Turn off the gas and wait 5 minutes",
//                            "Ready!")
//                    .build();
//
//            RecipeDao.insertRecipe(recipe);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testRecipeSearch() {

        try {

//            List<Recipe> recipeList = RecipeDao.performSearch("cool cake", 0, 0, 0, 0, 30, 0);
//
//            for (Recipe recipe : recipeList) {
//                System.out.println(recipe.description());
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}