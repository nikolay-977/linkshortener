CREATE DATABASE linkshortener;

\c linkshortener;

CREATE USER "user" WITH PASSWORD 'password';

GRANT ALL PRIVILEGES ON DATABASE linkshortener TO "user";

CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

GRANT ALL PRIVILEGES ON TABLE users TO "user";

CREATE TABLE links (
                       short_url VARCHAR(6) PRIMARY KEY,
                       original_url TEXT NOT NULL,
                       user_id UUID REFERENCES users(id),
                       click_count INT DEFAULT 0,
                       click_limit INT NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       lifetime INT NOT NULL
);

GRANT ALL PRIVILEGES ON TABLE links TO "user";
