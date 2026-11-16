# room-api

Room and reservation API: Controller → Service → Repository, Spring Data JPA, MySQL, Spring Security & JWT.

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
(`USE roomdb;` first). Check: 3 accounts, 3 rooms, 5 reservations.

To go back to that state at any time, run `reset.sql` (repository root).
Reset before you test, and before you submit. Ids are not reused, so a second run of
`api.http` without a reset answers differently.

## Run

```
./gradlew bootRun          # macOS / Linux
gradlew.bat bootRun        # Windows
```

The server starts on http://localhost:8080. After `reset.sql`, run `api.http` from 1 to 56
in order. Requests 1 to 41 cover rooms and reservations; 42 to 49 test HTTP Basic, sign-up,
and 401 error shapes; 50 to 55 test JWT token login, Bearer authentication, and broken token checks;
56 verifies that reading reservations requires authentication (returning 401).
Swagger UI: http://localhost:8080/swagger-ui.html

## Authentication

### Which endpoints are public
- **Public endpoints (no login required):**
  - `GET /api/rooms` and `GET /api/rooms/{id}` (reading rooms)
  - `GET /api/rooms/{roomId}/reservations` (reading room reservations)
  - `GET /api/stats` (room count)
  - `POST /api/auth/signup` (register a new account)
  - `POST /api/auth/login` (authenticate and receive JWT access token)
  - `POST /api/auth/refresh` (exchange refresh token for a new access token)
  - `POST /api/auth/logout` (revoke refresh token)
  - `/swagger-ui/**`, `/v3/api-docs/**`, `/swagger-ui.html`, `/error`
- **Protected endpoints (login required):**
  - `GET /api/reservations` and `GET /api/reservations/**` (reading personal reservations)
  - `GET /api/auth/me` (current authenticated user info)
  - All write operations: `POST /api/rooms`, `PUT /api/rooms/{id}`, `DELETE /api/rooms/{id}`, `POST /api/rooms/{roomId}/reservations`, `DELETE /api/reservations/{id}`.

### How to get a token and how to send it
1. **Get a token:** Send a POST request to `/api/auth/login` with your username and password:
   ```http
   POST http://localhost:8080/api/auth/login
   Content-Type: application/json

   {
     "username": "mina",
     "password": "mina1234"
   }
   ```
   Response (200 OK):
   ```json
   {
     "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
     "tokenType": "Bearer",
     "expiresIn": 1800,
     "refreshToken": "e0e85a53-4315-46aa-..."
   }
   ```
2. **Send the token:** Include the token in the `Authorization` header prefixed by `Bearer `:
   ```http
   GET http://localhost:8080/api/reservations
   Authorization: Bearer <accessToken>
   ```
   HTTP Basic authentication (`Authorization: Basic <base64(user:password)>`) is also supported alongside Bearer tokens.

### Sample accounts
Created in `reset.sql` with BCrypt hashes (passwords are never stored in plain text and never exposed in responses or logs):
- **mina**: password `mina1234`, role `USER` (Basic: `Basic bWluYTptaW5hMTIzNA==`)
- **omar**: password `Sunshine!2004`, role `USER`
- **admin**: password `admin1234`, role `ADMIN`
- **lee** (registered during `api.http` #43): password `lee12345`, role `USER` (Basic: `Basic bGVlOmxlZTEyMzQ1`)

### The Secret
`app.jwt.secret` is loaded from the environment variable `JWT_SECRET`, with a class-only default:
`week10-dev-only-secret-change-me-0123456789`.

### Refresh Token Challenge (Optional +1)
- Stored in the `refresh_token` table with user foreign key and expiration timestamp.
- Exchanged for a new access token via `POST /api/auth/refresh` (`{"refreshToken": "<token>"}`).
- Deleted from the database on logout via `POST /api/auth/logout`.

## Endpoints

| Method | Path                                | Status                  | Description                                              |
|--------|-------------------------------------|-------------------------|----------------------------------------------------------|
| POST   | `/api/auth/signup`                  | 201 / 400 / 409         | Register a new account (stores BCrypt hash only)         |
| POST   | `/api/auth/login`                   | 200 / 401               | Log in and get JWT access token (+ refresh token)        |
| GET    | `/api/auth/me`                      | 200 / 401               | Current authenticated user information                   |
| POST   | `/api/auth/refresh`                 | 200 / 401               | Exchange refresh token for new access token              |
| POST   | `/api/auth/logout`                  | 204                     | Invalidate refresh token on logout                       |
| GET    | `/api/rooms`                        | 200                     | All rooms; optional `minCapacity` and `keyword`          |
| GET    | `/api/rooms/{id}`                   | 200 / 404               | One room                                                 |
| POST   | `/api/rooms`                        | 201 / 400 / 401         | Create a room (requires login)                           |
| PUT    | `/api/rooms/{id}`                   | 200 / 400 / 401 / 404   | Replace a room (requires login)                          |
| DELETE | `/api/rooms/{id}`                   | 204 / 401 / 404 / 409   | Delete a room (requires login)                           |
| GET    | `/api/stats`                        | 200                     | Number of rooms                                          |
| GET    | `/api/rooms/{roomId}/reservations`  | 200 / 404               | Reservations of a room, ordered by start time            |
| POST   | `/api/rooms/{roomId}/reservations`  | 201 / 400 / 401 / 404 / 409 | Book a room (requires login)                         |
| GET    | `/api/reservations?reservedBy=Mina` | 200 / 401               | One person's reservations (requires login)               |
| GET    | `/api/reservations/{id}`            | 200 / 401 / 404         | One reservation (requires login)                         |
| DELETE | `/api/reservations/{id}`            | 204 / 401 / 404         | Cancel a reservation (requires login)                    |

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
| 401    | Authentication failure: `login required`, `wrong username or password`, or `token is invalid or expired`. |
| 404    | The requested room or reservation does not exist, or the URL has no endpoint. |
| 409    | The request conflicts with current data: duplicate username, booking overlap, or room still has reservations. |
| 500    | An unexpected server error. Details are logged; the response message is `unexpected server error`. |

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

AI tools (Google Antigravity with Gemini) were used to implement Spring Security, BCrypt hashing, JWT resource server configuration, refresh token challenge, error handling, and documentation.
