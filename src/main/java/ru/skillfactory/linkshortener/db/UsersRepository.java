package ru.skillfactory.linkshortener.db;

import ru.skillfactory.linkshortener.model.User;

import java.sql.*;
import java.util.UUID;

public class UsersRepository {
    private Connection connection;

    public UsersRepository(Connection connection) {
        this.connection = connection;
    }

    public void addUser(User user) {
        String sql = "INSERT INTO users (id, name) VALUES (?, ?)";
        PreparedStatement pstmt = null;
        try {
            pstmt = connection.prepareStatement(sql);
            pstmt.setObject(1, UUID.fromString(user.getId()));
            pstmt.setString(2, user.getName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Ошибка при добавлении пользователя.");
            throw new RuntimeException(e);
        }
    }

    public User getUserByName(String name) {
        String sql = "SELECT * FROM users WHERE name = ?";
        PreparedStatement pstmt;
        try {
            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getString("id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении пользователя.");
            throw new RuntimeException(e);
        }

        return null;
    }
}