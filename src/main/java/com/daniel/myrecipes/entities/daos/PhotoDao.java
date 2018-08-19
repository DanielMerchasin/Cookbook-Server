package com.daniel.myrecipes.entities.daos;

import com.daniel.myrecipes.entities.Photo;
import com.daniel.myrecipes.entities.User;
import com.daniel.myrecipes.utils.Config;
import com.daniel.myrecipes.networking.ResponseException;
import com.daniel.myrecipes.utils.Utils;
import org.json.JSONArray;

import javax.imageio.ImageIO;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.List;

public class PhotoDao {

    public static Photo addPhoto(Connection con, User user, int recipeId, String encodedImageData) throws Exception {

        if (!RecipeDao.matchUserRecipe(con, user, recipeId))
            throw new ResponseException(Response.Status.UNAUTHORIZED, "You can't add photos to this recipe!");

        Photo photo = saveImage(recipeId, encodedImageData);

        int id = -1;

        try (PreparedStatement stmt = con.prepareStatement(
                "INSERT INTO recipe_photos (recipe_id, location, thumbnail) " +
                        "VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, recipeId);
            stmt.setString(2, photo.getLocation());
            stmt.setBytes(3, photo.getThumbnail());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next())
                    photo.setId(rs.getInt(1));
            }
        }

        return photo;
    }

    public static ArrayList<Photo> getPhotos(Connection con, int recipeId) throws Exception {

        ArrayList<Photo> photos = new ArrayList<>();

        try (PreparedStatement stmt = con.prepareStatement(
                "SELECT * FROM recipe_photos WHERE recipe_id = ?")) {
            stmt.setInt(1, recipeId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    photos.add(new Photo(rs.getInt(1), rs.getInt(2),
                            rs.getString(3), rs.getBytes(4)));
                }
            }
        }

        return photos;

    }

    public static JSONArray getPhotosAsJSON(Connection con, int recipeId) throws Exception {

        List<Photo> photos = getPhotos(con, recipeId);
        JSONArray result = new JSONArray();

        for (Photo photo: photos)
            result.put(photo.toJSON());

        return result;
    }

    public static void deletePhoto(Connection con, User user, String location) throws Exception {

        Photo photo;

        try (PreparedStatement stmt = con.prepareStatement(
                     "SELECT * FROM recipe_photos WHERE location = ? LIMIT 1")) {

            stmt.setString(1, location);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    photo = new Photo();
                    photo.setId(rs.getInt(1));
                    photo.setRecipeId(rs.getInt(2));
                    photo.setLocation(rs.getString(3));
                    photo.setThumbnail(rs.getBytes(4));

                    if (!RecipeDao.matchUserRecipe(con, user, photo.getRecipeId()))
                        throw new ResponseException(Response.Status.UNAUTHORIZED,
                                "You can't delete photos from this recipe!");

                } else {
                    throw new Exception("Photo not found");
                }
            }

        }

        deleteImages(con, Collections.singletonList(photo));

    }

    public static void deleteAllPhotos(Connection con, User user, int recipeId) throws Exception {
        if (!RecipeDao.matchUserRecipe(con, user, recipeId))
            throw new ResponseException(Response.Status.UNAUTHORIZED, "You can't delete photos from this recipe!");

        List<Photo> photos = getPhotos(con, recipeId);

        deleteImages(con, photos);
    }

    private static void deleteImages(Connection con, List<Photo> photos) throws Exception {

        if (photos == null || photos.isEmpty())
            return;

        // Delete files
        for (Photo photo : photos) {
            File file = getImageFile(photo.getLocation());
            if (file.exists())
                file.delete();
        }

        // Delete from database
        try (PreparedStatement stmt = con.prepareStatement(
                     "DELETE FROM recipe_photos WHERE id IN (" +
                             String.join(",", Collections.nCopies(photos.size(), "?")) + ")")) {

            for (int i = 0; i < photos.size(); i++)
                stmt.setInt(i + 1, photos.get(i).getId());

            stmt.executeUpdate();
        }
    }

    public static Photo saveImage(int recipeId, String encodedImageData) throws IOException, ResponseException {

        // Get current time
        long now = System.currentTimeMillis();

        // Create a unique file name and make sure it doesn't exist already
        String filename;
        File file;
        do {
            filename = "img_" + recipeId + "_" + now + "_" + Utils.generateRandomString(16);
            file = getImageFile(filename);
        } while (file.exists());

        // Decode the image data and prepare a stream
        byte[] decodedBytes = Base64.getDecoder().decode(encodedImageData);

        // Write to file
        try (ByteArrayInputStream input = new ByteArrayInputStream(decodedBytes);
             FileOutputStream output = new FileOutputStream(file)) {
            Utils.writeToStream(input, output);
        }

        // Save thumbnail
        byte[] thumbnail = generateThumbnail(decodedBytes);

        // Save photo location in database
        return new Photo(recipeId, filename, thumbnail);

    }

    private static byte[] generateThumbnail(byte[] data) throws IOException {

        BufferedImage inputImage;
        try (ByteArrayInputStream input = new ByteArrayInputStream(data)) {
            inputImage = ImageIO.read(input);
        }

        int desiredSize = Config.getInstance().getThumbSize();

        // If the image is smaller than the desired size, just save it as it is
        if (inputImage.getWidth() < desiredSize && inputImage.getHeight() < desiredSize) {
            byte[] result;
            try (InputStream input = new ByteArrayInputStream(data);
                 ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                Utils.writeToStream(input, output);
                result = output.toByteArray();
            }
            return result;
        }

        // Calculate the width and height
        int width;
        int height;
        if (inputImage.getWidth() > inputImage.getHeight()) {
            float ratio = (float) inputImage.getHeight() / inputImage.getWidth();
            width = desiredSize;
            height = (int) (desiredSize * ratio);
        } else {
            float ratio = (float) inputImage.getWidth() / inputImage.getHeight();
            height = desiredSize;
            width = (int) (desiredSize * ratio);
        }

        BufferedImage outputImage = new BufferedImage(width, height, inputImage.getType());

        Graphics2D g = outputImage.createGraphics();
        g.drawImage(inputImage, 0, 0, width, height, null);
        g.dispose();

        byte[] result;
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(outputImage, "png", output);
            result = output.toByteArray();
        }
        return result;

    }

    public static byte[] loadImage(String filename) throws ResponseException, IOException {

        File file = getImageFile(filename);

        if (!file.exists())
            throw new ResponseException(Response.Status.NOT_FOUND, "Image file not found: " + filename);

        // File exists, load the image into a byte array
        byte[] result;
        try (InputStream input = new FileInputStream(file);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Utils.writeToStream(input, output);
            result = output.toByteArray();
        }
        return result;

    }

    private static File getImageFile(String location) throws ResponseException, IOException {

        // Get instance of the project configuration data
        Config config = Config.getInstance();

        String imagesPath = config.getImagesPath();

        if (imagesPath == null)
            throw new ResponseException(Response.Status.INTERNAL_SERVER_ERROR);

        // Images directory
        File imagesDirectory = new File(imagesPath);

        if (!imagesDirectory.exists()) {
            if (!imagesDirectory.mkdirs()) {
                throw new ResponseException(Response.Status.INTERNAL_SERVER_ERROR);
            }
        }

        return new File(imagesDirectory, location + ".png");

    }

}