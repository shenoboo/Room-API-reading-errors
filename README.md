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
(`USE roomdb;` first). Check: 3 accounts, 3 rooms, 5 reservations (`reservation` has `owner_id`, a foreign key to `app_user`).

To go back to that state at any time, run `reset.sql` (repository root).
Reset before you test, and before you submit. Ids are not reused, so a second run of
`api.http` without a reset answers differently.

## Run

```
./gradlew bootRun          # macOS / Linux
gradlew.bat bootRun        # Windows
```

The server starts on http://localhost:8080. After `reset.sql`, run `api.http` from 1 to 82
in order (83 to 85 are the optional refresh-token and logout requests). Requests 1 to 41 cover
rooms and reservations; 42 to 49 test HTTP Basic, sign-up, and 401 error shapes; 50 to 55 test
JWT login, Bearer authentication, and broken tokens; 56 is Lab F; 57 to 65 test rule D (admins
change rooms); 66 to 71 test rule C (owners); 72 to 80 are Lab G (move a booking); 81 and 82 test CORS.
Swagger UI: http://localhost:8080/swagger-ui.html

### Settings from the environment
| Variable      | Used for                           | Class-only default                      |
|---------------|------------------------------------|-----------------------------------------|
| `DB_PASSWORD` | MySQL password of `roomapp`        | `roomapp1234`                           |
| `JWT_SECRET`  | HS256 key that signs tokens        | `week10-dev-only-secret-change-me-0123456789` |
| `CORS_ORIGINS`| browser pages allowed to call the API (comma-separated, never `*`) | `http://localhost:5500,http://127.0.0.1:5500` |

The environment wins; the defaults are for class only. In IntelliJ: Run configuration →
Environment variables. Windows cmd: `set JWT_SECRET=...`; PowerShell: `$env:JWT_SECRET="..."`;
macOS/Linux: `export JWT_SECRET=...`. Never commit a real secret, a `.env` file with real
values, or a token copied from `api.http`.

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
  - `/api/reservations` and `/api/reservations/**` (mine, one reservation, move, cancel)
  - `GET /api/auth/me` (current authenticated user info)
  - All write operations: `POST /api/rooms`, `PUT /api/rooms/{id}`, `DELETE /api/rooms/{id}` (these three also need the ADMIN role, see Authorization), `POST /api/rooms/{roomId}/reservations`.

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
   GET http://localhost:8080/api/reservations/mine
   Authorization: Bearer <accessToken>
   ```
   HTTP Basic authentication (`Authorization: Basic <base64(user:password)>`) is also supported alongside Bearer tokens.

### Sample accounts
Created in `reset.sql` with BCrypt hashes (passwords are never stored in plain text and never exposed in responses or logs):
- **mina**: password `mina1234`, role `USER` (Basic: `Basic bWluYTptaW5hMTIzNA==`)
- **omar**: password `omar1234`, role `USER` (Basic: `Basic b21hcjpvbWFyMTIzNA==`)
- **admin**: password `admin1234`, role `ADMIN` (Basic: `Basic YWRtaW46YWRtaW4xMjM0`)
- **lee** (registered during `api.http` #43): password `lee12345`, role `USER` (Basic: `Basic bGVlOmxlZTEyMzQ1`)
- **kim** (registered during `api.http` #57): role `USER`, although the request asked for `ADMIN`

### The Secret
`app.jwt.secret` is loaded from the environment variable `JWT_SECRET`, with a class-only default:
`week10-dev-only-secret-change-me-0123456789`.

### Refresh Token Challenge (Optional +1)
- Stored in the `refresh_token` table with user foreign key and expiration timestamp.
- Exchanged for a new access token via `POST /api/auth/refresh` (`{"refreshToken": "<token>"}`).
- Deleted from the database on logout via `POST /api/auth/logout`.

## Authorization

Authentication asks "who are you?" (401). Authorization asks "are you allowed?" (403). We know
who you are, and the answer is no: logging in again changes nothing.

Roles come from the token as well as from Basic: `AuthService` signs the account's role into the
`role` claim, and `SecurityConfig.jwtAuthenticationConverter()` turns `USER` into `ROLE_USER`
(and `ADMIN` into `ROLE_ADMIN`). One rule therefore works for Basic and for tokens
(`GET /api/auth/me` shows the authorities, requests 49, 52 and 59).

**Rule D, by role (URL rules in `SecurityConfig`):** only an `ADMIN` creates, changes or deletes
rooms. `POST /api/rooms`, `PUT /api/rooms/*` and `DELETE /api/rooms/*` use `hasRole("ADMIN")`.
No login gives 401; a logged-in `USER` gives 403 `admin role required`. Reading stays public.
`/api/rooms/*` is one segment, so booking (`/api/rooms/1/reservations`) is not admin-only. Sign-up
has no role field: a `role` sent by the client is ignored, so the client never chooses its own role.

**Rule C, by owner (checked in `ReservationService`):** a reservation belongs to an account
(`reservation.owner_id`). The owner is the logged-in account, never a name typed by the client
(`reservedBy` is gone). A URL rule sees `/api/reservations/9` but cannot see who owns reservation 9;
the service can, which is why ownership is checked there and not in `SecurityConfig`.

### Who may call what

| Endpoint / Action | Unauthenticated | Logged-in USER (non-owner) | Logged-in USER (owner) | ADMIN |
|---|---|---|---|---|
| `GET /api/rooms` | 200 | 200 | 200 | 200 |
| `GET /api/rooms/{id}` | 200 | 200 | 200 | 200 |
| `POST /api/rooms` | 401 | 403 (`admin role required`) | 403 (`admin role required`) | 201 |
| `PUT /api/rooms/{id}` | 401 | 403 (`admin role required`) | 403 (`admin role required`) | 200 |
| `DELETE /api/rooms/{id}` | 401 | 403 (`admin role required`) | 403 (`admin role required`) | 204 |
| `GET /api/stats` | 200 | 200 | 200 | 200 |
| `GET /api/rooms/{roomId}/reservations` | 200 | 200 | 200 | 200 |
| `POST /api/rooms/{roomId}/reservations` | 401 | 201 (owner = caller) | 201 (owner = caller) | 201 (owner = caller) |
| `GET /api/reservations/mine` | 401 | 200 (own bookings) | 200 (own bookings) | 200 (own bookings) |
| `GET /api/reservations/{id}` | 401 | 403 (`belongs to another user`) | 200 | 200 |
| `PUT /api/reservations/{id}` (move) | 401 | 403 (`belongs to another user`) | 200 | 403 (owner only) |
| `DELETE /api/reservations/{id}` | 401 | 403 (`belongs to another user`) | 204 | 204 |
| `GET /api/auth/me` | 401 | 200 | 200 | 200 |
| `POST /api/auth/signup`, `/login` | 201 / 200 | 201 / 200 | 201 / 200 | 201 / 200 |

**Order of refusal** for `PUT /api/reservations/{id}`: who are you (401, filter) → is the request
valid (400) → does it exist (404) → is it yours (403) → does it clash (409). We look the reservation
up before we can ask whose it is, so a logged-in user can learn that a reservation id exists. We
chose 403 ("belongs to another user") over answering 404, and we apply that choice everywhere.
A move never clashes with itself (`countOverlappingExcept`), and it uses dirty checking, no `save()`.

**Both 403s have the `ApiError` body**: `ForbiddenException` (thrown by the service, rule C) and
Spring's `AccessDeniedException` (from a URL rule, rule D, handed over by `ApiAccessDeniedHandler`).

**CORS:** `CorsConfigurationSource` allows only the origins in `CORS_ORIGINS` for `/api/**`
(methods GET, POST, PUT, DELETE; headers `Authorization` and `Content-Type`; `Location` exposed).
CORS is checked by browsers, not by the server, curl or `api.http`; it is not a wall.
A preflight from an unlisted origin (request 82) is answered by the CORS filter before any controller
runs, so its 403 body is the plain text `Invalid CORS request`, not an `ApiError`.

**Safer errors:** 500 never shows a stack trace (it goes to the log); an unknown username and a
wrong password give the same 401 message; responses are records, never entities (an `AppUser`
entity would send its password hash); no password, token or `Authorization` header is logged.

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
| POST   | `/api/rooms`                        | 201 / 400 / 401 / 403   | Create a room (ADMIN only)                               |
| PUT    | `/api/rooms/{id}`                   | 200 / 400 / 401 / 403 / 404 | Replace a room (ADMIN only)                          |
| DELETE | `/api/rooms/{id}`                   | 204 / 401 / 403 / 404 / 409 | Delete a room (ADMIN only)                           |
| GET    | `/api/stats`                        | 200                     | Number of rooms                                          |
| GET    | `/api/rooms/{roomId}/reservations`  | 200 / 404               | Reservations of a room, ordered by start time            |
| POST   | `/api/rooms/{roomId}/reservations`  | 201 / 400 / 401 / 404 / 409 | Book a room; the owner is the logged-in account      |
| GET    | `/api/reservations/mine`            | 200 / 401               | The caller's own reservations (one JOIN query)           |
| GET    | `/api/reservations/{id}`            | 200 / 401 / 403 / 404   | One reservation (owner or admin)                         |
| PUT    | `/api/reservations/{id}`            | 200 / 400 / 401 / 403 / 404 / 409 | Move a reservation (owner only)                |
| DELETE | `/api/reservations/{id}`            | 204 / 401 / 403 / 404   | Cancel a reservation (owner or admin)                    |

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
| 403    | We know who you are, and the answer is no: `admin role required` (a USER changes rooms, rule D) or `reservation N belongs to another user` (rule C). |
| 404    | The requested room or reservation does not exist, or the URL has no endpoint. |
| 409    | The request conflicts with current data: duplicate username, booking overlap, or room still has reservations. |
| 500    | An unexpected server error. Details are logged; the response message is `unexpected server error`. |

A 403 has the same shape:

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "reservation 9 belongs to another user",
  "path": "/api/reservations/9",
  "fieldErrors": []
}
```

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

AI tools were used to help with implementing Spring Security, BCrypt hashing, JWT resource server configuration, refresh token challenge, roles and ownership (403), CORS, error handling, and documentation.
