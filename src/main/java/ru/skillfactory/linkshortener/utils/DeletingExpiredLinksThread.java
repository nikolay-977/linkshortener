package ru.skillfactory.linkshortener.utils;

import ru.skillfactory.linkshortener.db.LinksRepository;

import java.util.concurrent.TimeUnit;

public class DeletingExpiredLinksThread {
    private Thread cleanupThread; // Поток для удаления истекших ссылок

    public void startCleanup(LinksRepository linksRepository, String userId) {
        cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(TimeUnit.MINUTES.toMillis(1)); // Проверка каждые 1 минуту
                    linksRepository.deleteExpiredLinks(userId); // Передаем текущий userId
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break; // Завершение потока при прерывании
                }
            }
        });
        cleanupThread.start();
    }

    public void stopCleanup() {
        if (cleanupThread != null && cleanupThread.isAlive()) {
            cleanupThread.interrupt(); // Прерываем поток
            try {
                cleanupThread.join(); // Ждем завершения потока
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
