### data.sql

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
