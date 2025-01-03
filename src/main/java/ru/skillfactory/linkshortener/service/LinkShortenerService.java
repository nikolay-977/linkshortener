package ru.skillfactory.linkshortener.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.skillfactory.linkshortener.config.Config;
import ru.skillfactory.linkshortener.db.LinksRepository;
import ru.skillfactory.linkshortener.model.Link;

import java.sql.Connection;
import java.util.Optional;

public class LinkShortenerService {
    public static final String SERVICE_URL = Config.getInstance().getServiceUrl();
    private static final Logger logger = LoggerFactory.getLogger(LinkShortenerService.class);
    private Connection connection;
    private LinksRepository linksRepository;

    public LinkShortenerService(LinksRepository linksRepository) {
        this.linksRepository = linksRepository;
    }

    public String createShortLink(String originalUrl, int clickLimit, int lifeTime, String userId) {
        return linksRepository.createShortLink(originalUrl, clickLimit, lifeTime, userId);
    }

    public Optional<Link> redirect(String shortLink, String userId) {
        return linksRepository.redirect(shortLink, userId);
    }

    public boolean changeClickLimit(String shortLink, int newClickLimit, String userId) {
        return linksRepository.updateClickLimit(shortLink, newClickLimit, userId); // Обновляем лимит в базе данных
    }

    public void deleteShortLink(String linkToDelete, String userId) {
        linksRepository.deleteLink(linkToDelete, userId);
    }
}
