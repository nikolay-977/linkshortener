package ru.skillfactory.linkshortener;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Main {

    public static final String SERVICE_URL = Config.getInstance().getServiceUrl();
    private static final Scanner scanner = new Scanner(System.in);
    private static Thread cleanupThread; // Поток для удаления истекших ссылок

    public static void main(String[] args) {
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        try (Connection connection = dbConnection.getConnection()) {
            LinkShortener linkShortener = new LinkShortener(connection);
            String userId = getUser (linkShortener);

            // Запуск потока для удаления истекших ссылок
            startCleanupThread(linkShortener, userId);

            while (true) {
                printMenu();

                int choice = getIntInput();

                switch (choice) {
                    case 1:
                        String originalUrl = getOriginalUrl(); // Проверка оригинальной ссылки
                        System.out.print("Введите лимит переходов: ");
                        int clickLimit = getIntInput(); // Используем новый метод
                        String shortUrl = linkShortener.createShortLink(originalUrl, clickLimit, userId);
                        System.out.println("Создана короткая ссылка: " + shortUrl);
                        break;
                    case 2:
                        String shortLink = getShortLink(); // Проверка короткой ссылки
                        String redirectUrl = linkShortener.redirect(shortLink, userId);
                        if (redirectUrl == null) {
                            System.out.println("Переход невозможен.");
                        } else {
                            try {
                                Desktop.getDesktop().browse(new URI(redirectUrl));
                            } catch (IOException e) {
                                System.out.println("Ошибка при попытке открыть браузер.");
                                throw new RuntimeException(e);
                            } catch (URISyntaxException e) {
                                System.out.println("Ошибка при парсинге URI.");
                                throw new RuntimeException(e);
                            }
                        }
                        break;
                    case 3:
                        String linkToDelete = getShortLink(); // Проверка короткой ссылки
                        linkShortener.deleteLink(linkToDelete, userId);
                        System.out.println("Короткая ссылка удалена.");
                        break;
                    case 4:
                        stopCleanupThread(); // Остановка текущего потока
                        userId = getUser (linkShortener); // Смена пользователя
                        startCleanupThread(linkShortener, userId); // Запуск нового потока
                        break;
                    case 5:
                        stopCleanupThread(); // Остановка потока перед выходом
                        System.out.println("Выход из программы .");
                        return;
                    default:
                        System.out.println("Неверный выбор. Пожалуйста, попробуйте снова.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при подключении к базе данных: " + e.getMessage());
        }
    }

    private static void startCleanupThread(LinkShortener linkShortener, String userId) {
        cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(TimeUnit.MINUTES.toMillis(1)); // Проверка каждые 1 минуту
                    linkShortener.deleteExpiredLinks(userId); // Передаем текущий userId
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break; // Завершение потока при прерывании
                }
            }
        });
        cleanupThread.start();
    }

    private static void stopCleanupThread() {
        if (cleanupThread != null && cleanupThread.isAlive()) {
            cleanupThread.interrupt(); // Прерываем поток
            try {
                cleanupThread.join(); // Ждем завершения потока
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static String getUser (LinkShortener linkShortener) throws SQLException {
        System.out.print("Введите ваше имя пользователя: ");
        String username = getValidUsername(); // Проверка имени пользователя

        // Проверяем, существует ли уже UUID для данного имени пользователя
        String userId;
        User user = linkShortener.getUserByName(username);
        if (user == null) {
            // Если нет, создаем нового пользователя и сохраняем
            User newUser  = new User(username);
            linkShortener.addUser (newUser );
            userId = newUser .getId();
            System.out.println("Ваш новый ID пользователя: " + userId);
        } else {
            userId = user.getId();
            // Если существует, используем UUID
            System.out.println("Ваш ID пользователя: " + userId);
        }

        return userId;
    }

    private static String getValidUsername() {
        String usernameRegex = "^[а-яА-Яa-zA-Z0-9_\\-\\s]+$"; // Регулярное выражение для проверки имени пользователя
        while (true) {
            String username = scanner.nextLine();
            if (username != null && !username.trim().isEmpty() && username.matches(usernameRegex)) {
                return username;
            } else {
                System.out.println("Имя пользователя не может быть пустым и должно содержать только буквы, цифры, тире, нижние подчеркивания и пробелы. Пожалуйста, введите имя пользователя:");
            }
        }
    }

    private static String getOriginalUrl() {
        System.out.print("Введите оригинальную ссылку: ");
        while (true) {
            String originalUrl = scanner.nextLine();
            if (isValidUrl(originalUrl)) {
                return originalUrl;
            } else {
                System.out.println("Некорректный URL. Пожалуйста, введите действительную ссылку:");
            }
        }
    }

    private static String getShortLink() {
        System.out.print("Введите короткую ссылку: ");
        while (true) {
            String shortLink = scanner.nextLine();

            if (isValidShortLink(shortLink)) {
                return shortLink;
            } else {
                System.out.println("Некорректная короткая ссылка. Пожалуйста, введите действительную короткую ссылку:");
            }
        }
    }

    private static boolean isValidUrl(String url) {
        // Простейшая проверка URL
        String urlRegex = "^(http://|https://)?([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})(:[0-9]{1,5})?(/.*)?$";
        return url != null && url.matches(urlRegex);
    }

    private static boolean isValidShortLink(String shortLink) {
        // Проверяем, что короткая ссылка не null, не пустая и соответствует формату
        int serviceUrlLength = SERVICE_URL.length(); // Длина SERVICE_URL
        int fullShortLinkLength = serviceUrlLength + 6; // Длина SERVICE_URL + 6 символов
        return shortLink != null &&
                shortLink.startsWith(SERVICE_URL) &&
                shortLink.length() == fullShortLinkLength &&
                shortLink.substring(serviceUrlLength).matches("^[a-zA-Z0-9]+$"); // Проверка на наличие только букв и цифр после SERVICE_URL
    }

    private static int getIntInput() {
        while (true) {
            if (scanner.hasNextInt()) {
                int value = scanner.nextInt();
                scanner.nextLine(); // Очистка буфера
                return value;
            } else {
                System.out.println("Пожалуйста, введите число.");
                scanner.nextLine(); // Очистка некорректного ввода
            }
        }
    }

    public static void printMenu() {
        System.out.println("1. Сократить ссылку");
        System.out.println("2. Перейти по короткой ссылке");
        System.out.println("3. Удалить короткую ссылку");
        System.out.println("4. Сменить пользователя");
        System.out.println("5. Выйти");
        System.out.print("Выберите действие: ");
    }
}