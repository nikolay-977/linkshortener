package ru.skillfactory.linkshortener;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;

    private DatabaseConnection() {
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        Config config = Config.getInstance();
        return DriverManager.getConnection(config.getDbUrl(), config.getDbUser(), config.getDbPassword());
    }
}