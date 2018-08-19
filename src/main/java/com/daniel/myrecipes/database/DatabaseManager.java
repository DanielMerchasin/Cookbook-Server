package com.daniel.myrecipes.database;

import com.daniel.myrecipes.utils.Config;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

public class DatabaseManager {

//    private static final String DATABASE_URL = "jdbc:mysql://35.204.80.14:3306/myrecipes_db";
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/myrecipes_db";

    private static final String PROPERTIES_FILE = "connection_properties.txt";

    private static Properties properties;

    public static Connection getConnection() throws ClassNotFoundException, SQLException, IOException {
        Class.forName("com.mysql.jdbc.Driver");
        return DriverManager.getConnection(DATABASE_URL, readProperties());
    }

    private synchronized static Properties readProperties() throws IOException {
        if (DatabaseManager.properties == null) {
            synchronized (DatabaseManager.class) {
                Properties properties = new Properties();

                Map<String, String> propertiesMap = Config.getInstance().getConnectionProperties();

                for (Map.Entry<String, String> entry : propertiesMap.entrySet())
                    properties.setProperty(entry.getKey(), entry.getValue());

                // Read the properties from the properties file
//                File propertiesFile = new File(System.getProperty("user.dir"), PROPERTIES_FILE);
//                try (Scanner sc = new Scanner(propertiesFile)) {
//                    while (sc.hasNextLine()) {
//                        String line = sc.nextLine();
//                        if (line.isEmpty() || line.startsWith("//"))
//                            continue;
//
//                        int divIdx = line.indexOf('=');
//                        if (divIdx <= 0 || divIdx == line.length() - 1)
//                            continue;
//
//                        String key = line.substring(0, divIdx).trim();
//                        String value = line.substring(divIdx + 1).trim();
//
//                        properties.setProperty(key, value);
//                    }
//                }

                DatabaseManager.properties = properties;
            }
        }
        return properties;
    }

}
