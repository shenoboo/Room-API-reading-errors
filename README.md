# room-api

Room and reservation API: Controller → Service → Repository, Spring Data JPA, MySQL.

## Requirements

- JDK 21
- MySQL running on `localhost:3306`

## Database setup

Log in to MySQL as root and run:

```sql
CREATE DATABASE roomdb;
CREATE USER 'roomapp'@'localhost' IDENTIFIED BY 'roomapp1234';
GRANT ALL PRIVILEGES ON roomdb.* TO 'roomapp'@'localhost';
```

Then, once, run `schema.sql` and `data.sql` (from `src/main/resources`) in `roomdb`
(`USE roomdb;` first). Check: 3 rooms, 5 reservations.

To go back to that state at any time, run `reset.sql` (repository root).
Reset before you test, and before you submit. Ids are not reused, so a second run of
`api.http` without a reset answers differently.

## Run

```
./gradlew bootRun          # macOS / Linux
gradlew.bat bootRun        # Windows
```

The server starts on http://localhost:8080. After `reset.sql`, run `api.http` from 1 to 43
in order. Requests 1 to 41 cover the lab; 42 and 43 are two additional failure cases.
Swagger UI: http://localhost:8080/swagger-ui.html

## Endpoints

| Method | Path                                | Status                  | Description                                              |
|--------|-------------------------------------|-------------------------|----------------------------------------------------------|
| GET    | `/api/rooms`                        | 200                     | All rooms; optional `minCapacity` and `keyword`          |
| GET    | `/api/rooms/{id}`                   | 200 / 404               | One room                                                 |
| POST   | `/api/rooms`                        | 201 / 400               | Create a room                                            |
| PUT    | `/api/rooms/{id}`                   | 200 / 400 / 404         | Replace a room                                           |
| DELETE | `/api/rooms/{id}`                   | 204 / 404 / 409         | Delete a room; 409 if it still has reservations         |
| GET    | `/api/stats`                        | 200                     | Number of rooms                                          |
| GET    | `/api/rooms/{roomId}/reservations`  | 200 / 404               | Reservations of a room, ordered by start time            |
| POST   | `/api/rooms/{roomId}/reservations`  | 201 / 400 / 404 / 409   | Book a room; 409 if it overlaps an existing reservation |
| GET    | `/api/reservations?reservedBy=Mina` | 200                     | One person's reservations (one query, `join fetch`)      |
| GET    | `/api/reservations/{id}`            | 200 / 404               | One reservation                                          |
| DELETE | `/api/reservations/{id}`            | 204 / 404               | Cancel a reservation                                     |

Room and reservation endpoints return `RoomResponse` or `ReservationResponse` records;
the stats endpoint returns a room-count object. No endpoint returns a JPA entity.

## Errors

Every failure has the same `ApiError` response record, written by `ApiExceptionHandler`.
Its JSON shape is `status` (HTTP status code), `error` (reason phrase), `message`
(human-readable explanation), `path` (request path), and `fieldErrors` (a list of
`{field, message}` entries; empty unless request-field validation failed).

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "request has invalid fields",
  "path": "/api/rooms",
  "fieldErrors": [
    { "field": "capacity", "message": "capacity must be between 1 and 20" }
  ]
}
```

| Status | Meaning in this API |
|--------|---------------------|
| 400    | Invalid or incomplete input: field validation, invalid time range, malformed JSON, wrong parameter type, or missing required query parameter. |
| 404    | The requested room or reservation does not exist, or the URL has no endpoint. |
| 409    | The request conflicts with current data: a booking overlaps, or a room still has reservations. |
| 500    | An unexpected server error. Details are logged; the response message is `unexpected server error`. |

Where a rule lives: one field → the request record (`@Valid`); two fields or the data →
the service; the database (`NOT NULL`, `CHECK`, foreign key) is the last guard.

`PUT` uses dirty checking: `RoomService.update` loads a managed `Room`, calls `room.update(...)`
inside `@Transactional`, and Hibernate sends the UPDATE at commit only if something changed.

## Design notes

**Which layer answers 409 for DELETE /api/rooms/{id}?**
The service decides (`existsByRoomId` check, then `ConflictException`) and
`ApiExceptionHandler` turns it into 409, because "this room still has reservations" is a business rule about data,
not an HTTP detail. The foreign key stays as a second guard.

**Why does request 19 succeed?**
A booking from 11:00 to 12:00 only touches Mina's 10:00 to 11:00. The overlap rule uses
strict comparisons (`new.start < old.end` and `new.end > old.start`), and 11:00 < 11:00 is false.

**Known limit:** the overlap check reads, then writes. Two requests at the same moment can
both pass the check. It is a check, not a guarantee.

**SQL for a changed `PUT /api/rooms/{id}` (for example, request 27):**

Hibernate first selects the room so the service can update the managed entity. At
transaction commit, dirty checking detects the changed values and sends an update.
The exact aliases and formatting can vary by Hibernate version; the statements are
equivalent to:

```
select r1_0.id, r1_0.capacity, r1_0.name
from room r1_0
where r1_0.id = ?;

update room
set capacity = ?, name = ?
where id = ?;
```

For request 26, which sends the existing values, Hibernate still selects the room but
does not issue an update because dirty checking finds no changes. `RoomService.update`
has `@Transactional` and does not call `save()`.

## AI use

AI tools were used to help update the JPA implementation and documentation.
