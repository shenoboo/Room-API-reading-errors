### schema.sql

CREATE TABLE app_user (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(20) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(20) NOT NULL,
    CONSTRAINT uq_app_user_username UNIQUE (username)
);

CREATE TABLE room (
    id        BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(100) NOT NULL,
    capacity  INT          NOT NULL,
    CONSTRAINT chk_room_capacity CHECK (capacity BETWEEN 1 AND 20)
);

CREATE TABLE reservation (
    id           BIGINT      AUTO_INCREMENT PRIMARY KEY,
    room_id      BIGINT      NOT NULL,
    reserved_by  VARCHAR(50) NOT NULL,
    start_time   DATETIME    NOT NULL,
    end_time     DATETIME    NOT NULL,
    CONSTRAINT fk_reservation_room FOREIGN KEY (room_id) REFERENCES room (id)
);

CREATE TABLE refresh_token (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    token       VARCHAR(255) NOT NULL,
    user_id     BIGINT NOT NULL,
    expiry_date DATETIME NOT NULL,
    CONSTRAINT uq_refresh_token_token UNIQUE (token),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
);
