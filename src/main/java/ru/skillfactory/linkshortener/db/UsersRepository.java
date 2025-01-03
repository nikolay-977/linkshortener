package ru.skillfactory.linkshortener.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.skillfactory.linkshortener.exception.DatabaseException;
import ru.skillfactory.linkshortener.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class UsersRepository {
    private static final Logger logger = LoggerFactory.getLogger(UsersRepository.class);

    private Connection connection;

    public UsersRepository(Connection connection) {
        this.connection = connection;
    }

    public User addUser(User user) {
        String sql = "INSERT INTO users (id, name) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setObject(1, UUID.fromString(user.getId()));
            pstmt.setString(2, user.getName());
            pstmt.executeUpdate();
            return user;
        } catch (SQLException e) {
            logger.error("Ошибка при добавлении пользователя.");
            throw new DatabaseException("Ошибка при добавлении пользователя.", e);
        }
    }

    public Optional<User> getUserByName(String name) {
        String sql = "SELECT * FROM users WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new User(rs.getString("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            logger.error("Ошибка при получении пользователя.", e);
        }
        return Optional.empty();
    }
}