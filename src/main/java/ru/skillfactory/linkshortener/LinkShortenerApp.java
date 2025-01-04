package ru.skillfactory.linkshortener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.skillfactory.linkshortener.db.DatabaseConnection;
import ru.skillfactory.linkshortener.db.LinksRepository;
import ru.skillfactory.linkshortener.db.UsersRepository;
import ru.skillfactory.linkshortener.model.Link;
import ru.skillfactory.linkshortener.service.LinkShortenerService;
import ru.skillfactory.linkshortener.service.UserService;
import ru.skillfactory.linkshortener.utils.DeletingExpiredLinksScheduler;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.util.Optional;
import java.util.Scanner;

import static ru.skillfactory.linkshortener.service.LinkShortenerService.SERVICE_URL;

public class LinkShortenerApp {
    private static final Logger logger = LoggerFactory.getLogger(LinkShortenerApp.class);
    private LinkShortenerService linkShortenerService;
    private Scanner scanner;
    private String userId;
    private UserService userService;
    private DeletingExpiredLinksScheduler deletingExpiredLinksScheduler;

    public LinkShortenerApp() {
        this.scanner = new Scanner(System.in);
        Connection connection = DatabaseConnection.getInstance().getConnection();
        UsersRepository usersRepository = new UsersRepository(connection);
        LinksRepository linksRepository = new LinksRepository(connection);
        userService = new UserService(usersRepository);
        linkShortenerService = new LinkShortenerService(linksRepository);
        deletingExpiredLinksScheduler = new DeletingExpiredLinksScheduler(linksRepository);
    }

    private static void printMenu() {
        System.out.println("1. Сократить ссылку");
        System.out.println("2. Перейти по короткой ссылке");
        System.out.println("3. Изменить лимит переходов по короткой ссылке");
        System.out.println("4. Удалить короткую ссылку");
        System.out.println("5. Сменить пользователя");
        System.out.println("6. Выйти из программы");
        System.out.print("Выберите действие: ");
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

    private static boolean isValidUrl(String url) {
        // Простейшая проверка URL
        String urlRegex = "^(http://|https://)?([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})(:[0-9]{1,5})?(/.*)?$";
        return url != null && url.matches(urlRegex);
    }

    private void start() {
        userId = getUser();
        deletingExpiredLinksScheduler.startCleanup(userId);
        while (true) {
            printMenu();
            int choice = getIntInput();

            switch (choice) {
                case 1:
                    createShortLink();
                    break;
                case 2:
                    goToLink();
                    break;
                case 3:
                    changeClickLimit();
                    break;
                case 4:
                    deleteShortLink();
                    break;
                case 5:
                    changeUser();
                    break;
                case 6:
                    exit();
                    return;
                default:
                    System.out.println("Неверный выбор. Пожалуйста, попробуйте снова.");
            }
        }
    }

    private String getUser() {
        System.out.print("Введите ваше имя пользователя: ");
        String username = getValidUsername();
        return userService.getUserByName(username);
    }

    public void createShortLink() {
        String originalUrl = getOriginalUrl();
        System.out.print("Введите лимит переходов: ");
        int clickLimit = getIntInput();
        System.out.print("Введите время существования ссылки: ");
        int lifeTime = getIntInput();
        String shortUrl = linkShortenerService.createShortLink(originalUrl, clickLimit, lifeTime, userId);
        System.out.println("Создана короткая ссылка: " + shortUrl);
    }

    public void changeClickLimit() {
        String shortLink = getShortLink(); // Получаем короткую ссылку
        System.out.print("Введите новый лимит переходов: ");
        int newClickLimit = getIntInput(); // Получаем новый лимит
        boolean success = linkShortenerService.changeClickLimit(shortLink, newClickLimit, userId); // Обновляем лимит в базе данных
        if (success) {
            System.out.println("Лимит переходов для короткой ссылки обновлен.");
        } else {
            System.out.println("Не удалось обновить лимит переходов. Проверьте, существует ли короткая ссылка.");
        }
    }

    public void goToLink() {
        String shortLink = getShortLink(); // Проверка короткой ссылки
        Optional<Link> redirectUrlOpt = linkShortenerService.redirect(shortLink, userId);
        if (redirectUrlOpt.isEmpty()) {
            System.out.println("Ссылка не найдена.");
        } else {
            try {
                Desktop.getDesktop().browse(new URI(redirectUrlOpt.get().getOriginalUrl()));
            } catch (URISyntaxException e) {
                logger.error("Ошибка при парсинге URI.", e);
            } catch (IOException e) {
                logger.error("Ошибка при попытке открыть браузер.", e);
            }
        }
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

    private void changeUser() {
        deletingExpiredLinksScheduler.stopCleanup(); // Остановка текущего потока
        userId = getUser(); // Смена пользователя
        deletingExpiredLinksScheduler.startCleanup(userId); // Запуск нового потока
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

    public void exit() {
        deletingExpiredLinksScheduler.stopCleanup(); // Остановка текущего потока
        System.out.println("Выход из программы.");
    }

    public void deleteShortLink() {
        String linkToDelete = getShortLink(); // Проверка короткой ссылки
        linkShortenerService.deleteShortLink(linkToDelete, userId);
    }

    public static void main(String[] args) {
        LinkShortenerApp app = new LinkShortenerApp();
        app.start();
    }
}
