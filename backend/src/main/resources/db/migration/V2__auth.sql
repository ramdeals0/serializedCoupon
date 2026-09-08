CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    display_name VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_app_user_username UNIQUE (username)
);
