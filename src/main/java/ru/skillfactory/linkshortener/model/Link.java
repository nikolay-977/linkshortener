package ru.skillfactory.linkshortener.model;

public class Link {
    private String shortUrl;
    private String userId;

    public Link(String shortUrl, String userId) {
        this.shortUrl = shortUrl;
        this.userId = userId;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}