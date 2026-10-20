# Blog

A full-stack blog application with a Spring Boot REST API and a React web client. It supports publishing and managing posts, organizing posts with categories and tags, email/password authentication, Google OAuth2 sign-in, and JWT-protected operations.

## Architecture

```text
Browser
  └─ React + TypeScript client (Vite; Nginx in Docker)
       ├─ /api/v1/** ──────────────┐
       └─ OAuth2 routes ───────────┤
                                   v
                         Spring Boot REST API
                                   │ Spring Data JPA
                                   v
                              PostgreSQL
```

The frontend is a single-page application. In the production Compose setup, Nginx serves the built frontend and proxies `/api/`, `/oauth2/`, and `/login/oauth2/` requests to the backend. The backend uses Spring Security, JWT authentication, and Google OAuth2 login.

## Technology stack

- **Backend:** Java 17, Spring Boot 4.1.1, Spring MVC, Spring Security, Spring Data JPA, Jakarta Validation, Springdoc OpenAPI, JJWT, MapStruct, and Lombok.
- **Data stores:** PostgreSQL for deployment; H2 for automated tests.
- **Frontend:** React 18, TypeScript, Vite, React Router, Axios, Tailwind CSS, NextUI, and Tiptap.
- **Deployment:** Docker, Docker Compose, Nginx, and PostgreSQL 17.

## Repository layout

```text
src/main/java/com/hanqiu/blog/   Backend controllers, services, persistence, security, and DTOs
src/main/resources/              Backend application configuration
src/test/java/                   Backend tests
src/test/resources/               H2 test configuration
frontend/src/                    React application
Dockerfile                       Backend container build
frontend/Dockerfile              Frontend build and Nginx runtime
docker-compose.yml               PostgreSQL and Adminer services
docker-compose.prod.yml          Production database, backend, and frontend services
```

## API

The REST API is rooted at `/api/v1`. UUIDs are used for resource IDs. Unless otherwise stated, create endpoints return `201 Created`, successful reads and updates return `200 OK`, and deletes return `204 No Content`.

| Method | Path | Description | Access |
| --- | --- | --- | --- |
| `POST` | `/api/v1/auth/register` | Register with `username`, `email`, and `password` | Public |
| `POST` | `/api/v1/auth/login` | Log in with `email` and `password`; returns a JWT response | Public |
| `GET` | `/api/v1/posts` | List published posts; optional `categoryId`, `tagId`, `keyword`, and Spring pageable parameters (`page`, `size`, `sort`). Default page size is 5, sorted by `createdAt` descending. | Public |
| `GET` | `/api/v1/posts/{id}` | Read a post | Public |
| `GET` | `/api/v1/posts/drafts` | List the authenticated user's drafts | JWT required |
| `POST` | `/api/v1/posts` | Create a post | JWT required |
| `PUT` | `/api/v1/posts/{id}` | Update a post | JWT required; owner only |
| `DELETE` | `/api/v1/posts/{id}` | Delete a post | JWT required; owner or admin |
| `GET` | `/api/v1/categories` | List categories | Public |
| `POST` | `/api/v1/categories` | Create a category | Authenticated |
| `DELETE` | `/api/v1/categories/{id}` | Delete a category | Admin |
| `GET` | `/api/v1/tags` | List tags | Public |
| `POST` | `/api/v1/tags` | Create tags from a list of names | Authenticated |
| `DELETE` | `/api/v1/tags/{id}` | Delete a tag | Admin |

Authenticated API requests use `Authorization: Bearer <JWT>`. Post creation and update accept a title, content, category ID, tag IDs, and a `DRAFT` or `PUBLISHED` status. The implementation validates title/content lengths and limits posts to 10 tags.

Google sign-in starts at `/oauth2/authorization/google`. On success, the backend redirects to the configured `OAUTH2_SUCCESS_REDIRECT_URL` and appends the JWT as a URL fragment (`#token=...`).

Springdoc endpoints are available at `/swagger-ui/index.html` and `/v3/api-docs` when the backend is running.

## Prerequisites

- Java 17 (the Maven Wrapper is included).
- Node.js 22 and npm for running the frontend outside Docker.
- Docker Engine with the Docker Compose plugin for the containerized deployment.
- A Google OAuth client for Google sign-in.

## Local development

### Backend

The default backend configuration expects PostgreSQL at `localhost:5433`, database `blog`, and database user `bloguser`. Set the corresponding Spring datasource environment variables if your local database uses different values. Google OAuth client credentials and a JWT signing secret must also be supplied through your IDE run configuration or shell environment; do not commit them.

From the repository root, run:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell, use `./mvnw.cmd spring-boot:run`.

### Frontend

```bash
cd frontend
npm ci
npm run dev
```

Vite serves the frontend on its default development port and proxies `/api` requests to `http://localhost:8080`. The frontend's optional `.env.example` documents `VITE_GOOGLE_OAUTH_URL`; set it to the backend OAuth authorization URL if the Google sign-in link should be shown.

## Tests

Run the backend test suite from the repository root:

```bash
./mvnw test
```

The test configuration uses an in-memory H2 database with schema creation and cleanup for the test run. The test sources cover application startup, controllers, services, repositories, post specifications, and authentication. No frontend test script is defined in `frontend/package.json`.

## Docker deployment

The production Compose file starts PostgreSQL 17, the Spring Boot backend, and the Nginx frontend. Create a local `.env.prod` file in the repository root and provide these values using your own credentials and secrets:

```dotenv
POSTGRES_PASSWORD=<strong-database-password>
JWT_SECRET=<random-secret-at-least-32-bytes>
GOOGLE_CLIENT_ID=<your-google-oauth-client-id>
GOOGLE_CLIENT_SECRET=<your-google-oauth-client-secret>
OAUTH2_SUCCESS_REDIRECT_URL=<public-frontend-url>/oauth2/callback
```

Keep `.env.prod` private and out of version control. Enter your own values in the local file; do not copy credentials from other files into this README or commit them.

Build and start the services:

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml up --build -d
```

The frontend is published on port `80`. The backend is bound to `127.0.0.1:8080`, and PostgreSQL is bound to `127.0.0.1:5433`. The Compose file stores PostgreSQL data in the named `blog_data` volume. Stop the stack with:

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml down
```

The root `docker-compose.yml` is a separate development utility that starts PostgreSQL and Adminer only; it does not start the application containers. Its database settings are not aligned with the backend's default `application.yaml`, so configure matching credentials and database settings before using it for local backend development.

## Configuration notes

- Backend settings are in `src/main/resources/application.yaml`; test settings are in `src/test/resources/application.yaml`.
- Production container settings and required variables are defined in `docker-compose.prod.yml`.
- Frontend build-time variables are defined in `frontend/Dockerfile`; the client API uses the relative `/api/v1` base path.
- Never put real database credentials, OAuth client secrets, JWT signing keys, or access tokens in this README or other tracked files.
