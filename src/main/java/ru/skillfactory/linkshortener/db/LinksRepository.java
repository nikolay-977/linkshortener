package ru.skillfactory.linkshortener.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.skillfactory.linkshortener.config.Config;
import ru.skillfactory.linkshortener.model.Link;

import java.sql.*;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static ru.skillfactory.linkshortener.service.LinkShortenerService.SERVICE_URL;

public class LinksRepository {
    private static final Logger logger = LoggerFactory.getLogger(LinksRepository.class);

    private Connection connection;

    public LinksRepository(Connection connection) {
        this.connection = connection;
    }

    public String createShortLink(String originalUrl, int userClickLimit, int userLifeTime, String userId) {
        String shortUrl = UUID.randomUUID().toString().substring(0, 6);
        int configLifeTime = Config.getInstance().getLifeTime();
        int configClickLimit = Config.getInstance().getClickLimit();
        long lifeTimeHours = TimeUnit.HOURS.toMillis(Math.min(userLifeTime, configLifeTime));
        int clickLimit = Math.max(userClickLimit, configClickLimit);

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
            logger.error("Ошибка при создании короткой ссылки.", e);
        }
        return SERVICE_URL + shortUrl;
    }

    public Optional<Link> redirect(String fullShortUrl, String userId) {
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
                return Optional.of(new Link(shortUrl, originalUrl, userId));
            }
        } catch (SQLException e) {
            logger.error("Ошибка при перенаправлении по короткой ссылки.", e);
        }
        return Optional.empty();
    }

    public Optional<Link> getLinkByShortUrlAndUserId(String fullShortUrl, String userId) {
        String shortUrl = fullShortUrl.replace(SERVICE_URL, "");
        String sql = "SELECT short_url, user_id FROM links WHERE short_url = ? AND user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, shortUrl);
            pstmt.setObject(2, UUID.fromString(userId));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(new Link(shortUrl, userId));
            }
        } catch (SQLException e) {
            logger.error("Ошибка при получении ссылки: {}", e.getMessage());
        }
        System.out.println("Ссылка не найдена.");
        return Optional.empty();
    }

    public void deleteLink(String fullShortUrl, String userId) {
        String shortUrl = fullShortUrl.replace(SERVICE_URL, "");
        Optional<Link> linkOpt = getLinkByShortUrlAndUserId(fullShortUrl, userId);
        if (linkOpt.isPresent()) {
            String sql = "DELETE FROM links WHERE short_url = ? AND user_id = ?";
            PreparedStatement pstmt;
            try {
                pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, shortUrl);
                pstmt.setObject(2, UUID.fromString(userId));
                pstmt.executeUpdate();
                System.out.println("Короткая ссылка удалена.");
            } catch (SQLException e) {
                logger.error("Ошибка при удалени короткой ссылки.", e);
            }
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
            logger.error("Ошибка при удалении истекших ссылок.", e);
        }
    }

    public boolean updateClickLimit(String fullShortUrl, int userClickLimit, String userId) {
        String shortUrl = fullShortUrl.replace(SERVICE_URL, "");
        getLinkByShortUrlAndUserId(fullShortUrl, userId);
        int configClickLimit = Config.getInstance().getClickLimit();
        int clickLimit = Math.max(userClickLimit, configClickLimit);
        String query = "UPDATE links SET click_limit = ? WHERE short_url = ? AND user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, clickLimit);
            statement.setString(2, shortUrl);
            statement.setObject(3, UUID.fromString(userId));
            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0; // Возвращаем true, если обновление прошло успешно
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении лимита переходов.", e);
            return false;
        }
    }
}