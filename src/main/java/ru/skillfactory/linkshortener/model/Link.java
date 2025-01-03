package ru.skillfactory.linkshortener.model;

public class Link {
    private String shortUrl;
    private String originalUrl;
    private String userId;

    public Link(String shortUrl, String userId) {
        this.shortUrl = shortUrl;
        this.userId = userId;
    }

    public Link(String shortUrl, String originalUrl, String userId) {
        this.shortUrl = shortUrl;
        this.originalUrl = originalUrl;
        this.userId = userId;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }
}