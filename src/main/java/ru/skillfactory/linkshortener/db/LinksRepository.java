package ru.skillfactory.linkshortener.db;

import ru.skillfactory.linkshortener.config.Config;

import java.sql.*;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static ru.skillfactory.linkshortener.service.LinkShortenerService.SERVICE_URL;

public class LinksRepository {
    private Connection connection;

    public LinksRepository(Connection connection) {
        this.connection = connection;
    }

    public String createShortLink(String originalUrl, int clickLimit, int userLifeTime, String userId) {
        String shortUrl = UUID.randomUUID().toString().substring(0, 6);
        int configLifeTime = Config.getInstance().getLifeTime();
        long lifeTimeHours = TimeUnit.HOURS.toMillis(Collections.min(Arrays.asList(userLifeTime, configLifeTime)));
        String sql = "INSERT INTO links (short_url, original_url, user_id, click_limit, life_time, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt;
        try {
            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, shortUrl);
            pstmt.setString(2, originalUrl);
            pstmt.setObject(3, UUID.fromString(userId));
            pstmt.setInt(4, clickLimit);
            pstmt.setLong(5, lifeTimeHours);
            pstmt.setTimestamp(6, new Timestamp(System.currentTimeMillis())); // Устанавливаем текущее время
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Ошибка при создании коркоткой ссылки.");
            throw new RuntimeException(e);
        }
        return SERVICE_URL + shortUrl;
    }

    public String redirect(String fullShortUrl, String userId) {
        String shortUrl = fullShortUrl.replace(SERVICE_URL, "");
        String sql = "SELECT original_url, click_count, click_limit, created_at, life_time FROM links WHERE short_url = ? AND user_id = ?";
        PreparedStatement pstmt;
        try {
            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, shortUrl);
            pstmt.setObject(2, UUID.fromString(userId));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String originalUrl = rs.getString("original_url");
                int clickCount = rs.getInt("click_count");
                int clickLimit = rs.getInt("click_limit");
                long createdAt = rs.getTimestamp("created_at").getTime();
                long lifetime = rs.getLong("life_time");

                // Проверяем, достигнут ли лимит переходов
                if (clickCount >= clickLimit) {
                    System.out.println("Уведомление для пользователя: Лимит достигнут, ссылка недоступна.");
                    return null;
                }

                // Проверяем, истекло ли время жизни ссылки
                if (System.currentTimeMillis() > createdAt + lifetime) {
                    System.out.println(MessageFormat.format("Уведомление для пользователя: Время жизни ссылки {0}{1} истекло, ссылка недоступна.", SERVICE_URL, shortUrl));
                    return null;
                }

                // Увеличиваем счетчик переходов
                String updateSql = "UPDATE links SET click_count = click_count + 1 WHERE short_url = ?";
                PreparedStatement updatePstmt = connection.prepareStatement(updateSql);
                updatePstmt.setString(1, shortUrl);
                updatePstmt.executeUpdate();

                return originalUrl;
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при перенаправлении по короткой ссылки.");
            throw new RuntimeException(e);
        }

        System.out.println("Ссылка не найдена.");
        return null;
    }

    public void deleteLink(String fullShortUrl, String userId) {
        String shortUrl = fullShortUrl.replace(SERVICE_URL, "");
        String sql = "DELETE FROM links WHERE short_url = ? AND user_id = ?";
        PreparedStatement pstmt;
        try {
            pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, shortUrl);
            pstmt.setObject(2, UUID.fromString(userId));
            pstmt.executeUpdate();
            System.out.println("Короткая ссылка удалена.");
        } catch (SQLException e) {
            System.out.println("Ошибка при удалени короткой ссылки.");
            throw new RuntimeException(e);
        }
    }

    public void deleteExpiredLinks(String currentUserId) {
        String selectSql = "SELECT short_url, user_id FROM links WHERE created_at + (life_time * INTERVAL '1 millisecond') < CURRENT_TIMESTAMP";
        String deleteSql = "DELETE FROM links WHERE created_at + (life_time * INTERVAL '1 millisecond') < CURRENT_TIMESTAMP";

        try (PreparedStatement selectPstmt = connection.prepareStatement(selectSql);
             PreparedStatement deletePstmt = connection.prepareStatement(deleteSql)) {

            // Сначала выбираем истекшие ссылки
            ResultSet rs = selectPstmt.executeQuery();
            while (rs.next()) {
                String shortUrl = rs.getString("short_url");
                String userId = rs.getString("user_id");
                if (userId.equals(currentUserId)) {
                    // Уведомляем пользователя об удалении ссылки
                    System.out.println(MessageFormat.format("\nУведомление для пользователя: Время жизни ссылки {0}{1} истекло, ссылка недоступна.\n", SERVICE_URL, shortUrl));
                }
            }

            // Затем удаляем истекшие ссылки
            deletePstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Ошибка при удалении истекших ссылок.");
            throw new RuntimeException(e);
        }
    }

    public boolean updateClickLimit(String fullShortUrl, int newClickLimit, String userId) {
        String shortUrl = fullShortUrl.replace(SERVICE_URL, "");
        String query = "UPDATE links SET click_limit = ? WHERE short_url = ? AND user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, newClickLimit);
            statement.setString(2, shortUrl);
            statement.setObject(3, UUID.fromString(userId));
            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0; // Возвращаем true, если обновление прошло успешно
        } catch (SQLException e) {
            System.out.println("Ошибка при обновлении лимита переходов: " + e.getMessage());
            return false;
        }
    }
}