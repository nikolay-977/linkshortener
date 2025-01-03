package ru.skillfactory.linkshortener.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.skillfactory.linkshortener.config.Config;
import ru.skillfactory.linkshortener.db.DatabaseConnection;
import ru.skillfactory.linkshortener.db.LinksRepository;
import ru.skillfactory.linkshortener.db.UsersRepository;
import ru.skillfactory.linkshortener.model.User;
import ru.skillfactory.linkshortener.utils.DeletingExpiredLinksScheduler;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.Scanner;

public class LinkShortenerService {
    public static final String SERVICE_URL = Config.getInstance().getServiceUrl();
    private static final Logger logger = LoggerFactory.getLogger(LinkShortenerService.class);
    private Scanner scanner;
    private DeletingExpiredLinksScheduler deletingExpiredLinksScheduler;

    public LinkShortenerService() {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        LinksRepository linksRepository = new LinksRepository(connection);
        UsersRepository usersRepository = new UsersRepository(connection);
        deletingExpiredLinksScheduler = new DeletingExpiredLinksScheduler(Config.getInstance().getCleanSchedulerInterval());

        scanner = new Scanner(System.in);
        String userId = getUser(usersRepository);

        deletingExpiredLinksScheduler.startCleanup(linksRepository, userId);

        while (true) {
            printMenu();
            int choice = getIntInput();

            switch (choice) {
                case 1:
                    createShortLink(linksRepository, userId);
                    break;
                case 2:
                    goToLink(linksRepository, userId);
                    break;
                case 3:
                    changeClickLimit(linksRepository, userId);
                    break;
                case 4:
                    deleteShortLink(linksRepository, userId);
                    break;
                case 5:
                    userId = changeUser(linksRepository, usersRepository);
                    break;
                case 6:
                    exit();
                    return;
                default:
                    System.out.println("Неверный выбор. Пожалуйста, попробуйте снова.");
            }
        }
    }

    public static void printMenu() {
        System.out.println("1. Сократить ссылку");
        System.out.println("2. Перейти по короткой ссылке");
        System.out.println("3. Изменить лимит переходов по короткой ссылке");
        System.out.println("4. Удалить короткую ссылку");
        System.out.println("5. Сменить пользователя");
        System.out.println("6. Выйти из программы");
        System.out.print("Выберите действие: ");
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

    public void createShortLink(LinksRepository linksRepository, String userId) {
        String originalUrl = getOriginalUrl();
        System.out.print("Введите лимит переходов: ");
        int clickLimit = getIntInput();
        System.out.print("Введите время существования ссылки: ");
        int lifeTime = getIntInput();
        String shortUrl = linksRepository.createShortLink(originalUrl, clickLimit, lifeTime, userId);
        System.out.println("Создана короткая ссылка: " + shortUrl);
    }

    public void goToLink(LinksRepository linksRepository, String userId) {
        String shortLink = getShortLink(); // Проверка короткой ссылки
        String redirectUrl = linksRepository.redirect(shortLink, userId);
        if (redirectUrl == null) {
            System.out.println("Переход невозможен.");
        } else {
            try {
                Desktop.getDesktop().browse(new URI(redirectUrl));
            } catch (URISyntaxException e) {
                logger.error("Ошибка при парсинге URI.", e);
            } catch (IOException e) {
                logger.error("Ошибка при попытке открыть браузер.", e);
            }
        }
    }

    public void changeClickLimit(LinksRepository linksRepository, String userId) {
        String shortLink = getShortLink(); // Получаем короткую ссылку

        System.out.print("Введите новый лимит переходов: ");
        int newClickLimit = getIntInput(); // Получаем новый лимит

        boolean success = linksRepository.updateClickLimit(shortLink, newClickLimit, userId); // Обновляем лимит в базе данных
        if (success) {
            System.out.println("Лимит переходов для короткой ссылки обновлен.");
        } else {
            System.out.println("Не удалось обновить лимит переходов. Проверьте, существует ли короткая ссылка.");
        }
    }

    public void deleteShortLink(LinksRepository linksRepository, String userId) {
        String linkToDelete = getShortLink(); // Проверка короткой ссылки
        linksRepository.deleteLink(linkToDelete, userId);
    }

    public String changeUser(LinksRepository linksRepository, UsersRepository usersRepository) {
        deletingExpiredLinksScheduler.stopCleanup(); // Остановка текущего потока
        String userId = getUser(usersRepository); // Смена пользователя
        deletingExpiredLinksScheduler.startCleanup(linksRepository, userId); // Запуск нового потока
        return userId;
    }

    public void exit() {
        deletingExpiredLinksScheduler.stopCleanup(); // Остановка текущего потока
        System.out.println("Выход из программы.");
    }

    public String getUser(UsersRepository usersRepository) {
        System.out.print("Введите ваше имя пользователя: ");
        String username = getValidUsername();

        Optional<User> userOpt = usersRepository.getUserByName(username);
        String userId;
        if (userOpt.isPresent()) {
            userId = userOpt.get().getId();
            System.out.println(MessageFormat.format("Ваш ID пользователя: {0}", userId));
        } else {
            User newUser = new User(username);
            usersRepository.addUser(newUser);
            userId = newUser.getId();
            System.out.println(MessageFormat.format("Ваш новый ID пользователя: {0}", userId));
        }

        return userId;
    }

    private String getValidUsername() {
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

    private String getOriginalUrl() {
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

    private String getShortLink() {
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

    public int getIntInput() {
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
}
