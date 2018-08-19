package com.daniel.myrecipes.utils;

import org.json.JSONObject;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Config {

    private static final String CONFIG_FILE = "config.json";

    private static final String CONFIG_FILE_SERVER_PATH = "/home/archnovaelite/cookbook/config.json";

    private static Config instance;

    public static Config getInstance() throws IOException {
        synchronized (Config.class) {
            if (instance == null) {
                synchronized (Config.class) {
                    instance = new Config();
                }
            }
        }
        return instance;
    }

    private String imagesPath;
    private String thumbsPath;
    private String databaseURL;
    private Map<String, String> connectionProperties;
    private String clientVersion;
    private String serverVersion;
    private int thumbSize;

    private Config() throws IOException {

//        File file = new File(System.getProperty("user.dir"), CONFIG_FILE);
        File file = new File(CONFIG_FILE_SERVER_PATH);

        JSONObject data;

        try (FileInputStream input = new FileInputStream(file);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) != -1)
                output.write(buffer, 0, len);

            data = new JSONObject(new String(output.toByteArray(), "UTF-8"));
        }

        // Parse the data

        if (data.has("paths")) {
            JSONObject paths = data.getJSONObject("paths");

            if (paths.has("images"))
                imagesPath = paths.getString("images");

        }

        if (data.has("thumb_size"))
            thumbSize = data.getInt("thumb_size");

        if (data.has("database")) {
            JSONObject database = data.getJSONObject("database");

            if (database.has("url"))
                databaseURL = database.getString("url");

            if (database.has("connection_properties")) {
                JSONObject properties = database.getJSONObject("connection_properties");

                connectionProperties = new HashMap<>();

                Iterator<?> keys = properties.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    connectionProperties.put(key, properties.getString(key));
                }
            }
        }

        if (data.has("version")) {
            JSONObject version = data.getJSONObject("version");

            if (version.has("client"))
                clientVersion = version.getString("client");

            if (version.has("server"))
                serverVersion = version.getString("server");
        }

    }

    public String getImagesPath() {
        return imagesPath;
    }

    public void setImagesPath(String imagesPath) {
        this.imagesPath = imagesPath;
    }

    public String getDatabaseURL() {
        return databaseURL;
    }

    public void setDatabaseURL(String databaseURL) {
        this.databaseURL = databaseURL;
    }

    public Map<String, String> getConnectionProperties() {
        return connectionProperties;
    }

    public void setConnectionProperties(Map<String, String> connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    public int getThumbSize() {
        return thumbSize;
    }

    public void setThumbSize(int thumbSize) {
        this.thumbSize = thumbSize;
    }
}
