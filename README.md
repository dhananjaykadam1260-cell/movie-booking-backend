# Movie Booking: Backend

Spring Boot 4 REST API for the movie booking system. It uses MySQL for storage, JWT for authentication, JavaMail for emails, and OpenPDF for ticket PDFs.

## Stack

- Java 17, Spring Boot 4.1 (Web MVC, Data JPA, Security, Mail)
- MySQL 8
- JWT (jjwt 0.12), BCrypt password hashing
- OpenPDF for tickets

## Configuration

No secrets are stored in the repository. Set these environment variables before starting the app:

| Variable | Required | Default | Purpose |
|---|---|---|---|
| `DB_URL` | no | `jdbc:mysql://localhost:3306/movie_booking` | JDBC URL |
| `DB_USERNAME` | **yes** | none | Database user |
| `DB_PASSWORD` | **yes** | none | Database password |
| `MAIL_USERNAME` | **yes** | none | Gmail address used to send emails |
| `MAIL_PASSWORD` | **yes** | none | Gmail app password (not your normal password) |
| `JWT_SECRET` | **yes** | none | Signing key, at least 32 characters. Generate with `openssl rand -base64 48` |
| `JWT_EXPIRATION_MS` | no | `86400000` (24 h) | Token lifetime |
| `CORS_ALLOWED_ORIGINS` | no | `http://localhost:5173` | Comma-separated frontend origins |
| `PORT` | no | `8080` | HTTP port |

Example (Linux/macOS):

```bash
export DB_USERNAME=movie
export DB_PASSWORD='change-me'
export MAIL_USERNAME=you@gmail.com
export MAIL_PASSWORD='xxxx xxxx xxxx xxxx'
export JWT_SECRET="$(openssl rand -base64 48)"
./mvnw spring-boot:run
```

The app refuses to start if a required variable is missing.

## Database

Create the database first:

```sql
CREATE DATABASE movie_booking;
```

Tables are created automatically (`spring.jpa.hibernate.ddl-auto=update`).

## Creating the first admin

Public sign-up always creates `USER` accounts. Promote an account to admin directly in the database:

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'admin@example.com';
```

## Running

```bash
./mvnw spring-boot:run        # http://localhost:8080
```

## Tests

```bash
./mvnw test                   # unit tests, no database needed
./mvnw test -Dtest.excludedGroups=   # also runs integration tests; needs MySQL and the variables above
```

## API overview

| Method | Path | Access | Notes |
|---|---|---|---|
| POST | `/api/auth/register` | public | Creates a USER account |
| POST | `/api/auth/login` | public | Returns a JWT |
| GET | `/api/auth/users` | admin | |
| GET | `/api/movies`, `/api/theatres`, `/api/screens`, `/api/shows`, `/api/seats/show/{id}` | public | |
| POST/PUT/DELETE | `/api/movies`, `/api/theatres`, `/api/screens`, `/api/shows`, `/api/seats` | admin | |
| POST | `/api/bookings` | user | Body: `{ "showId", "seatIds" }` |
| GET | `/api/bookings/user/{userId}` | owner or admin | |
| GET | `/api/bookings` | admin | |
| GET | `/api/bookings/{id}/ticket` | owner or admin | PDF |
| POST | `/api/bookings/{id}/cancel` | owner or admin | Before the show starts |

Send the token as `Authorization: Bearer <token>`.

Errors return JSON: `{ "message": "..." }`.

## Security notes

- Passwords are stored as BCrypt hashes.
- Sessions are stateless. Each request is authenticated by the JWT, and the role is read from the database.
- Seat booking locks the seat rows, so two users cannot book the same seat.
- A booking, ticket or cancellation for another user's booking returns `404`, so IDs cannot be probed.
