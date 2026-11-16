-- reset.sql: back to 3 accounts, 3 rooms, and 5 reservations. Run as roomapp (or root) in roomdb.
USE roomdb;
DROP TABLE IF EXISTS refresh_token;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS room;
DROP TABLE IF EXISTS app_user;

-- schema
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

-- data
INSERT INTO app_user (username, password_hash, role) VALUES ('mina',  '$2a$10$ix/.xals1cT5fBy84xYGd./rBgU.YjsOFZ0hS649iIxeUqTiiOLzK', 'USER');
INSERT INTO app_user (username, password_hash, role) VALUES ('omar',  '$2a$10$Skdch9b59aEX3NRWiV5YcOyPJV0Yj.iwMUZnglgQ0z2UBzFRYNGce', 'USER');
INSERT INTO app_user (username, password_hash, role) VALUES ('admin', '$2a$10$0fP8ztxDnZHZ9LCfAUC6IOJZm2euVGTIf0pIYoyJPDsxfDtQ2cHoW', 'ADMIN');

INSERT INTO room (name, capacity) VALUES ('Seminar A', 8);
INSERT INTO room (name, capacity) VALUES ('Study Pod', 4);
INSERT INTO room (name, capacity) VALUES ('Rooftop Room', 12);

INSERT INTO reservation (room_id, reserved_by, start_time, end_time) VALUES (1, 'Mina',  '2026-10-06 10:00:00', '2026-10-06 11:00:00');
INSERT INTO reservation (room_id, reserved_by, start_time, end_time) VALUES (1, 'Omar',  '2026-10-06 13:00:00', '2026-10-06 15:00:00');
INSERT INTO reservation (room_id, reserved_by, start_time, end_time) VALUES (2, 'Mina',  '2026-10-06 09:00:00', '2026-10-06 10:00:00');
INSERT INTO reservation (room_id, reserved_by, start_time, end_time) VALUES (1, 'Lucas', '2026-10-07 10:00:00', '2026-10-07 12:00:00');
INSERT INTO reservation (room_id, reserved_by, start_time, end_time) VALUES (2, 'Aiko',  '2026-10-07 14:00:00', '2026-10-07 15:00:00');
