package ru.skillfactory.linkshortener.utils;

import ru.skillfactory.linkshortener.db.LinksRepository;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DeletingExpiredLinksScheduler {
    private ScheduledExecutorService scheduler;
    private final long interval; // Интервал проверки в миллисекундах

    public DeletingExpiredLinksScheduler(long intervalInMinutes) {
        this.interval = intervalInMinutes; // Устанавливаем интервал
    }

    public void startCleanup(LinksRepository linksRepository, String userId) {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                linksRepository.deleteExpiredLinks(userId);
            } catch (Exception e) {
            }
        }, 0, interval, TimeUnit.MINUTES); // Запуск сразу, затем с заданным интервалом
    }

    public void stopCleanup() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown(); // Останавливаем планировщик
            try {
                if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow(); // Принудительное завершение
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
