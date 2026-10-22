# Architecture

This document describes the current structure and runtime behavior of the Blog application.

## 1. System context

The application is a single-page React client backed by a Spring Boot REST API and PostgreSQL database.

```text
┌──────────────────────┐
│        Browser       │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│ React + TypeScript   │
│ Vite (development)   │
│ Nginx (production)   │
└──────────┬───────────┘
           │ /api/v1/** and OAuth routes
           ▼
┌──────────────────────┐
│ Spring Boot REST API │
│ Spring Security      │
│ JWT + Google OAuth2  │
└──────────┬───────────┘
           │ Spring Data JPA
           ▼
┌──────────────────────┐
│     PostgreSQL       │
└──────────────────────┘
```

In production, Nginx serves the compiled frontend and proxies `/api/`, `/oauth2/`, and `/login/oauth2/` requests to the backend. During local development, Vite serves the frontend and proxies `/api` requests to `http://localhost:8080`.

## 2. Main components

### Frontend

- **Framework:** React 18 and TypeScript.
- **Build/development server:** Vite.
- **Routing:** React Router.
- **HTTP client:** Axios.
- **UI and editor:** Tailwind CSS, NextUI, and Tiptap.
- **API base path:** `/api/v1`.

The frontend communicates with the backend over HTTP. Authenticated API requests send the JWT using the `Authorization: Bearer <JWT>` header.

### Backend

- **Runtime:** Java 17 and Spring Boot 4.1.1.
- **HTTP layer:** Spring MVC REST controllers.
- **Security:** Spring Security, JWT authentication, and Google OAuth2 login.
- **Persistence:** Spring Data JPA.
- **Validation and API documentation:** Jakarta Validation and Springdoc OpenAPI.
- **Supporting libraries:** JJWT, MapStruct, and Lombok.

Backend source code lives under `src/main/java/com/hanqiu/blog/`. The application configuration is in `src/main/resources/application.yaml`.

### Database

- PostgreSQL 17 is used for the containerized application.
- Automated backend tests use an in-memory H2 database.
- The production Compose configuration persists PostgreSQL data in the named Docker volume `blog_data`.

## 3. Request flows

### Public post browsing

1. The browser requests the React application.
2. The frontend requests `GET /api/v1/posts`, optionally supplying category, tag, keyword, pagination, or sorting parameters.
3. The backend returns published posts.
4. The frontend renders the returned page of posts.

The documented default page size is 5, sorted by `createdAt` descending.

### Email/password authentication

1. The client submits credentials to `POST /api/v1/auth/login`.
2. The backend authenticates the user.
3. A successful login returns a JWT response.
4. The client sends the token in the `Authorization: Bearer <JWT>` header for protected API calls.

Registration is available through `POST /api/v1/auth/register`.

### Google OAuth2 sign-in

1. The user starts sign-in at `/oauth2/authorization/google`.
2. Spring Security handles the OAuth2 flow with Google.
3. On success, the backend redirects to `OAUTH2_SUCCESS_REDIRECT_URL` and appends the JWT as the URL fragment `#token=...`.
4. The frontend callback route can process the redirect.

The OAuth client ID, client secret, and redirect URL must be configured for the target environment.

### Post management and authorization

- Public users can list published posts and read a post.
- Authenticated users can retrieve their own drafts and create posts.
- Updating a post requires authentication and ownership.
- Deleting a post requires authentication and ownership or admin privileges.
- Category and tag listing is public; creation requires authentication, and deletion requires admin privileges.

Post create/update payloads include a title, content, category ID, tag IDs, and `DRAFT` or `PUBLISHED` status. The backend validates title/content lengths and limits a post to 10 tags.

## 4. API surface

All REST endpoints are rooted at `/api/v1`; resource IDs are UUIDs.

| Method | Endpoint | Purpose | Access |
| --- | --- | --- | --- |
| `POST` | `/auth/register` | Register a user | Public |
| `POST` | `/auth/login` | Authenticate and return JWT response | Public |
| `GET` | `/posts` | List published posts with filters and pagination | Public |
| `GET` | `/posts/{id}` | Read a post | Public |
| `GET` | `/posts/drafts` | List current user's drafts | JWT required |
| `POST` | `/posts` | Create a post | JWT required |
| `PUT` | `/posts/{id}` | Update a post | JWT required; owner only |
| `DELETE` | `/posts/{id}` | Delete a post | JWT required; owner or admin |
| `GET` | `/categories` | List categories | Public |
| `POST` | `/categories` | Create a category | Authenticated |
| `DELETE` | `/categories/{id}` | Delete a category | Admin |
| `GET` | `/tags` | List tags | Public |
| `POST` | `/tags` | Create tags from names | Authenticated |
| `DELETE` | `/tags/{id}` | Delete a tag | Admin |

The API is documented by Springdoc. When the backend is running, Swagger UI is at `/swagger-ui/index.html`, and the OpenAPI document is at `/v3/api-docs`.

## 5. Deployment topology

### Production-style Compose stack

`docker-compose.prod.yml` defines the database, backend, and frontend services.

```text
Host :80
   │
   ▼
Frontend container (Nginx)
   ├── serves compiled React assets
   └── proxies API/OAuth paths
             │
             ▼
       Backend container
             │
             ▼
       PostgreSQL container
             │
             ▼
       Named volume: blog_data
```

According to the current configuration:

- Frontend is published on host port `80`.
- Backend is bound to `127.0.0.1:8080`.
- PostgreSQL is bound to `127.0.0.1:5433`.
- PostgreSQL data is stored in `blog_data`.
- `.env.prod` supplies the database password, JWT secret, Google OAuth credentials, and OAuth success redirect URL.

The production-style stack can be started with:

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml up --build -d
```

It can be stopped with:

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml down
```

Stopping the stack this way leaves the named database volume intact.

### Development database stack

The root `docker-compose.yml` starts PostgreSQL and Adminer only. It does not start the backend or frontend. Its database settings differ from the backend's default configuration, so local environment and datasource values must be aligned before using this stack with the backend.

## 6. Testing and CI

### Backend tests

Run from the repository root:

```bash
./mvnw test
```

On Windows PowerShell:

```powershell
./mvnw.cmd test
```

The test configuration uses H2 and covers application startup, controllers, services, repositories, post specifications, and authentication.

### GitHub Actions

The CI workflow runs on pushes and pull requests and has three jobs:

1. **Backend test:** sets up Java 17 and runs `./mvnw -B test`.
2. **Frontend build:** sets up Node.js 22, installs dependencies with `npm ci`, and runs `npm run build`.
3. **Docker build:** waits for the backend and frontend jobs, then builds the backend image for validation.

The current workflow does not publish the image to a registry or deploy the application.

## 7. Configuration and secret handling

| Purpose | Location / variable |
| --- | --- |
| Backend runtime configuration | `src/main/resources/application.yaml` |
| Backend test configuration | `src/test/resources/application.yaml` |
| Production Compose configuration | `docker-compose.prod.yml` |
| Database password | `POSTGRES_PASSWORD` |
| JWT signing secret | `JWT_SECRET` |
| Google OAuth client ID | `GOOGLE_CLIENT_ID` |
| Google OAuth client secret | `GOOGLE_CLIENT_SECRET` |
| OAuth success redirect | `OAUTH2_SUCCESS_REDIRECT_URL` |
| Optional frontend OAuth URL | `VITE_GOOGLE_OAUTH_URL` |

Keep `.env.prod`, database credentials, OAuth secrets, JWT signing keys, and access tokens out of version control. Use environment-specific values rather than copying secrets into tracked configuration files.

