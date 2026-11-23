### data.sql

INSERT INTO room (name, capacity) VALUES ('Seminar A', 8);
INSERT INTO room (name, capacity) VALUES ('Study Pod', 4);
INSERT INTO room (name, capacity) VALUES ('Rooftop Room', 12);

-- Three accounts (ids 1, 2, 3). Passwords (for class only): mina1234, omar1234, admin1234
INSERT INTO app_user (username, password_hash, role) VALUES ('mina',  '$2a$10$ix/.xals1cT5fBy84xYGd./rBgU.YjsOFZ0hS649iIxeUqTiiOLzK', 'USER');
INSERT INTO app_user (username, password_hash, role) VALUES ('omar',  '$2a$10$50tuC8ry/VwJ7KOPT6EDb.1ncKve70rMAeSz9td9eL0Ow6UGeVUX6', 'USER');
INSERT INTO app_user (username, password_hash, role) VALUES ('admin', '$2a$10$8GIuHc2cp55HDtQXKnmAIeCe96sDjVCRtFkrfR2LbwFzvt5k507ze', 'ADMIN');

-- Same times as before. Owners: mina 1 and 3; omar 2, 4 and 5 (4 and 5 were Lucas and Aiko).
INSERT INTO reservation (room_id, owner_id, start_time, end_time) VALUES (1, 1, '2026-10-06 10:00:00', '2026-10-06 11:00:00');
INSERT INTO reservation (room_id, owner_id, start_time, end_time) VALUES (1, 2, '2026-10-06 13:00:00', '2026-10-06 15:00:00');
INSERT INTO reservation (room_id, owner_id, start_time, end_time) VALUES (2, 1, '2026-10-06 09:00:00', '2026-10-06 10:00:00');
INSERT INTO reservation (room_id, owner_id, start_time, end_time) VALUES (1, 2, '2026-10-07 10:00:00', '2026-10-07 12:00:00');
INSERT INTO reservation (room_id, owner_id, start_time, end_time) VALUES (2, 2, '2026-10-07 14:00:00', '2026-10-07 15:00:00');
