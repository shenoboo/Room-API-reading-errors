### schema.sql

CREATE TABLE room (
    id        BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(100) NOT NULL,
    capacity  INT          NOT NULL,
    CONSTRAINT chk_room_capacity CHECK (capacity BETWEEN 1 AND 20)
);

-- Week 10: accounts. password_hash holds a BCrypt hash (60 characters), never the password.
CREATE TABLE app_user (
    id             BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(20)  NOT NULL,
    password_hash  VARCHAR(100) NOT NULL,
    role           VARCHAR(20)  NOT NULL,
    CONSTRAINT uq_app_user_username UNIQUE (username)
);

-- Optional challenge: refresh tokens. Needed because ddl-auto=validate checks the RefreshToken entity.
CREATE TABLE refresh_token (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    token       VARCHAR(255) NOT NULL,
    user_id     BIGINT       NOT NULL,
    expiry_date DATETIME     NOT NULL,
    CONSTRAINT uq_refresh_token_token UNIQUE (token),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
);

-- Week 11: reserved_by (a name anyone could type) is replaced by owner_id (an account).
CREATE TABLE reservation (
    id          BIGINT    AUTO_INCREMENT PRIMARY KEY,
    room_id     BIGINT    NOT NULL,
    owner_id    BIGINT    NOT NULL,
    start_time  DATETIME  NOT NULL,
    end_time    DATETIME  NOT NULL,
    CONSTRAINT fk_reservation_room  FOREIGN KEY (room_id)  REFERENCES room (id),
    CONSTRAINT fk_reservation_owner FOREIGN KEY (owner_id) REFERENCES app_user (id)
);
