package ru.skillfactory.linkshortener.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.skillfactory.linkshortener.config.Config;
import ru.skillfactory.linkshortener.exception.DatabaseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(LinksRepository.class);
    private static DatabaseConnection instance;

    private DatabaseConnection() {
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        Config config = Config.getInstance();
        try {
            return DriverManager.getConnection(config.getDbUrl(), config.getDbUser(), config.getDbPassword());
        } catch (SQLException e) {
            logger.error("Ошибка при подключении к базе данных пользователя.", e);
            throw new DatabaseException("Ошибка при подключении к базе данных пользователя.", e);
        }
    }
}