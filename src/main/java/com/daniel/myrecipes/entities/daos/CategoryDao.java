package com.daniel.myrecipes.entities.daos;

import com.daniel.myrecipes.entities.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {

    public static List<Category> getAllCategories(Connection con) throws Exception {

        List<Category> result = new ArrayList<>();

        try (PreparedStatement stmt = con.prepareStatement(
                "SELECT * FROM categories")) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Category category = new Category();
                    category.setId(rs.getInt(1));
                    category.setName(rs.getString(2));
                    category.setDescription(rs.getString(3));
                    result.add(category);
                }
            }

        }

        return result;

    }

    public static Category getCategoryById(Connection con, int id) throws Exception {
        try (PreparedStatement stmt = con.prepareStatement(
                "SELECT * FROM categories WHERE id = ? LIMIT 1")) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Category category = new Category();
                    category.setId(rs.getInt(1));
                    category.setName(rs.getString(2));
                    category.setDescription(rs.getString(3));
                    return category;
                }
            }

        }
        return null;
    }

}
