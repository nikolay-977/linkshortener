package ru.skillfactory.linkshortener.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static Config instance; // Единственный экземпляр класса
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private String serviceUrl;
    private int lifeTime;
    private int clickLimit;
    private int cleanSchedulerInterval;

    private Config() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.out.println("Извините, не удалось найти application.properties");
                return;
            }
            properties.load(input);
            dbUrl = properties.getProperty("db.url");
            dbUser = properties.getProperty("db.user");
            dbPassword = properties.getProperty("db.password");
            serviceUrl = properties.getProperty("service.url");
            lifeTime = Integer.parseInt(properties.getProperty("life.time.hours"));
            lifeTime = Integer.parseInt(properties.getProperty("click.limit"));
            cleanSchedulerInterval = Integer.parseInt(properties.getProperty("clean.scheduler.interval"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public int getLifeTime() {
        return lifeTime;
    }

    public int getClickLimit() {
        return clickLimit;
    }

    public int getCleanSchedulerInterval() {
        return cleanSchedulerInterval;
    }
}