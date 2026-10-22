# Blog

A full-stack blog application built with **Spring Boot**, **React**, and **PostgreSQL**. It supports post creation and management, categories and tags, email/password authentication, Google OAuth 2.0 sign-in, and JWT-protected operations.

## Features

- Browse published posts with pagination, sorting, category/tag filters, and keyword search.
- Create, update, and delete posts according to authentication and ownership rules.
- Save posts as drafts and retrieve the authenticated user's drafts.
- Organize posts with categories and tags.
- Register and sign in with email and password.
- Sign in with Google OAuth 2.0.
- Explore the REST API through Swagger UI / OpenAPI.
- Run the backend test suite with an in-memory H2 database.
- Build and run the application with Docker Compose.

## Architecture at a glance

```text
Browser
  │
  ▼
React + TypeScript (Vite in development; Nginx in production)
  │  /api/v1/**, /oauth2/**, /login/oauth2/**
  ▼
Spring Boot REST API
  │  Spring Data JPA
  ▼
PostgreSQL
```

In production, Nginx serves the built React application and proxies API and OAuth-related requests to the Spring Boot backend. See [ARCHITECTURE.md](ARCHITECTURE.md) for the request flow, security model, and deployment layout.

## Technology stack

| Area | Technologies |
| --- | --- |
| Backend | Java 17, Spring Boot 4.1.1, Spring MVC, Spring Security, Spring Data JPA, Jakarta Validation, Springdoc OpenAPI, JJWT, MapStruct, Lombok |
| Database | PostgreSQL 17; H2 for automated tests |
| Frontend | React 18, TypeScript, Vite, React Router, Axios, Tailwind CSS, NextUI, Tiptap |
| Runtime / deployment | Docker, Docker Compose, Nginx |
| CI | GitHub Actions: backend tests, frontend production build, and Docker image build |

## Repository layout

```text
.
├── src/
│   ├── main/java/com/hanqiu/blog/   # Backend application code
│   ├── main/resources/              # Runtime configuration
│   └── test/                        # Backend tests and H2 configuration
├── frontend/
│   ├── src/                         # React application
│   └── Dockerfile                   # Frontend build and Nginx runtime
├── .github/workflows/ci.yml         # CI workflow
├── Dockerfile                       # Backend multi-stage image build
├── docker-compose.yml               # Development database and Adminer
├── docker-compose.prod.yml          # Production database, backend, frontend
└── ARCHITECTURE.md                  # Architecture and request flows
```

## Getting started

### Prerequisites

- Java 17 (the Maven Wrapper is included).
- Node.js 22 and npm, if running the frontend outside Docker.
- Docker Engine and the Docker Compose plugin, if using containers.
- A Google OAuth client, if you want Google sign-in.

### Backend

The backend's default configuration expects PostgreSQL at `localhost:5433`, with database `blog` and user `bloguser`. If your database uses different settings, provide matching Spring datasource environment variables. The backend also needs a JWT signing secret and Google OAuth credentials for the corresponding authentication features. Supply secrets through environment variables or your IDE run configuration; do not commit them.

From the repository root:

```bash
# Windows PowerShell
./mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

### Frontend

In a second terminal:

```bash
cd frontend
npm ci
npm run dev
```

Vite runs the frontend on its default development port and proxies `/api` requests to `http://localhost:8080`. The optional `frontend/.env.example` documents `VITE_GOOGLE_OAUTH_URL`; set it to the backend's Google authorization URL if the Google sign-in link should be displayed.

## Tests

Run the backend tests from the repository root:

```bash
# Windows PowerShell
./mvnw.cmd test

# macOS / Linux
./mvnw test
```

Tests use an in-memory H2 database. The suite covers application startup, controllers, services, repositories, post specifications, and authentication. There is currently no frontend test script defined in `frontend/package.json`.

## API overview

The REST API is rooted at `/api/v1`. Resource IDs are UUIDs. Typical response codes are `201 Created` for creates, `200 OK` for successful reads and updates, and `204 No Content` for deletes.

| Method | Endpoint | Purpose | Access |
| --- | --- | --- | --- |
| `POST` | `/api/v1/auth/register` | Register with username, email, and password | Public |
| `POST` | `/api/v1/auth/login` | Sign in with email and password; returns a JWT response | Public |
| `GET` | `/api/v1/posts` | List published posts; supports `categoryId`, `tagId`, `keyword`, `page`, `size`, and `sort` | Public |
| `GET` | `/api/v1/posts/{id}` | Read a post | Public |
| `GET` | `/api/v1/posts/drafts` | List the current user's drafts | JWT required |
| `POST` | `/api/v1/posts` | Create a post | JWT required |
| `PUT` | `/api/v1/posts/{id}` | Update a post | JWT required; owner only |
| `DELETE` | `/api/v1/posts/{id}` | Delete a post | JWT required; owner or admin |
| `GET` | `/api/v1/categories` | List categories | Public |
| `POST` | `/api/v1/categories` | Create a category | Authenticated |
| `DELETE` | `/api/v1/categories/{id}` | Delete a category | Admin |
| `GET` | `/api/v1/tags` | List tags | Public |
| `POST` | `/api/v1/tags` | Create tags from a list of names | Authenticated |
| `DELETE` | `/api/v1/tags/{id}` | Delete a tag | Admin |

Authenticated API requests use `Authorization: Bearer <JWT>`. Creating or updating a post accepts its title, content, category ID, tag IDs, and `DRAFT` or `PUBLISHED` status. The backend validates title/content lengths and limits posts to 10 tags.

Google sign-in starts at `/oauth2/authorization/google`. On success, the backend redirects to `OAUTH2_SUCCESS_REDIRECT_URL` and appends the JWT as a URL fragment (`#token=...`).

When the backend is running, Swagger UI is available at `/swagger-ui/index.html`, and the OpenAPI document is available at `/v3/api-docs`.

## Docker Compose

### Production-style stack

Create a local `.env.prod` file in the repository root with your own values:

```dotenv
POSTGRES_PASSWORD=<strong-database-password>
JWT_SECRET=<random-secret-at-least-32-bytes>
GOOGLE_CLIENT_ID=<your-google-oauth-client-id>
GOOGLE_CLIENT_SECRET=<your-google-oauth-client-secret>
OAUTH2_SUCCESS_REDIRECT_URL=<public-frontend-url>/oauth2/callback
```

Keep `.env.prod` private and out of version control. Then build and start the production-style stack:

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml up --build -d
```

The frontend is published on port `80`. The backend is bound to `127.0.0.1:8080`, and PostgreSQL is bound to `127.0.0.1:5433`. PostgreSQL data is persisted in the named `blog_data` volume. Stop the stack with:

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml down
```

This command does not remove the named database volume. To remove persistent database data, you would need to remove the volume separately; do so only if you intend to delete that data.

### Development database tools

The root `docker-compose.yml` starts PostgreSQL and Adminer only; it does not start the application containers. Its database settings differ from the backend's default `application.yaml`, so configure matching credentials and datasource settings before using it for local backend development.

## Configuration and security notes

- Backend runtime settings: `src/main/resources/application.yaml`.
- Test settings: `src/test/resources/application.yaml`.
- Production container settings: `docker-compose.prod.yml`.
- Frontend build-time settings: `frontend/Dockerfile`; the client uses the relative `/api/v1` API base path.
- Never commit database passwords, OAuth client secrets, JWT signing keys, access tokens, or `.env.prod`.

## Continuous integration

GitHub Actions runs on pushes and pull requests. The workflow checks the backend with Maven tests, builds the frontend, and builds the backend Docker image. The Docker image is built on the GitHub-hosted runner for CI validation; the workflow does not publish it to a container registry.
