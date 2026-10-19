# Fieldnotes frontend

A React, TypeScript, and Vite frontend for the Spring Boot blog API in the provided backend source. It includes the public journal, searchable and filterable pagination, post detail, registration and login, draft listing, and authenticated post creation/editing/deletion. Tags can be selected or created from the editor.

## Run locally

1. Install Node.js 20 or newer.
2. Start the backend and its PostgreSQL database. The backend defaults to `http://localhost:8080` and PostgreSQL at `localhost:5433`.
3. Copy `.env.example` to `.env` and adjust `VITE_API_BASE_URL` if needed.
4. Run `npm install`, then `npm run dev` and open the local URL Vite prints.
5. For a production build, run `npm run build`; serve the `dist` directory with SPA fallback enabled.

The backend must allow browser requests from the frontend origin (CORS). The supplied backend source did not include a CORS configuration. Add the Vite origin, normally `http://localhost:5173`, to the backend's allowed origins before using the browser app.

## Authentication and Google

Email/password login calls `POST /api/v1/auth/login` and stores the returned JWT in browser local storage. Authenticated requests send `Authorization: Bearer <jwt>`. Registration uses `POST /api/v1/auth/register`; after registering, sign in normally.

To enable Google sign-in, configure `VITE_GOOGLE_OAUTH_URL` to the backend's OAuth entry route (default `/oauth2/authorization/google`). The supplied success handler writes the JWT as a raw response body after Google login; it does not redirect back to the frontend or issue a browser-readable callback URL. For a seamless SPA flow, update the backend success handler to redirect to a configured frontend callback with a short-lived, safely exchanged credential. The current frontend shows the Google link only when that URL is configured.

The backend returns no profile/current-user endpoint, and its JWT generator currently places no role claims in the token. Post DTOs expose author name but not author email. The frontend therefore shows editing/deletion actions to signed-in users and relies on the backend's owner/admin checks for the final authorization decision; a user without permission will receive the API's 403 response.

## API usage reflected in the UI

- `GET /api/v1/posts`: Spring `Page<PostDto>`; defaults to 5 items and newest first. The frontend requests 6 per page, supports `page`, `size`, `sort`, optional `keyword`, `categoryId`, and `tagId`. Keyword searches title and content; public results are published posts only.
- `GET /api/v1/posts/{id}`, `GET /api/v1/posts/drafts`.
- `POST /api/v1/auth/login`, `POST /api/v1/auth/register`.
- `POST /api/v1/posts`, `PUT /api/v1/posts/{id}`, `DELETE /api/v1/posts/{id}`.
- `GET /api/v1/categories`, `GET /api/v1/tags`, `POST /api/v1/tags`.

Create/update bodies use `title`, `content`, `categoryId`, `tagIds`, and `status` (`DRAFT` or `PUBLISHED`). Update also includes `id`. Post DTOs include nested author/category/tags, reading time, timestamps, and status. The backend has no category-creation control beyond `POST /api/v1/categories`; categories need to exist before writing. This app focuses category selection and tag creation.

No credentials or backend secrets are included. Google credentials and JWT signing secret must be provided to the backend via its environment/configuration.
