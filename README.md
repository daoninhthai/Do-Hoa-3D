# OAuth2 Authorization Server

A centralized OAuth2/OIDC authorization server for Single Sign-On (SSO) across multiple applications, built with Spring Authorization Server 1.1 and Angular 16.

## Tech Stack

### Backend
- **Java 17** with **Spring Boot 3.1.0**
- **Spring Authorization Server 1.1.0** for OAuth2/OIDC
- **Spring Security** for authentication and authorization
- **Spring Data JPA** with **PostgreSQL** for persistence
- **Nimbus JOSE JWT** for JWK management

### Frontend
- **Angular 16** with TypeScript 5.1
- OAuth2 Authorization Code flow with PKCE
- Route guards for protected pages

## Features

- OAuth2 Authorization Code Grant with PKCE
- Client Credentials Grant
- Refresh Token Grant
- OpenID Connect (OIDC) with UserInfo endpoint
- Dynamic client registration
- RSA key pair generation and rotation
- JWK Set endpoint for token verification
- Form-based login with CORS support
- Role-based access control (ADMIN, USER, CLIENT)

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Node.js 18+ and npm
- PostgreSQL 15+

### Backend

```bash
# Start PostgreSQL and create database
createdb auth_server_db

# Run the authorization server
mvn spring-boot:run
```

The server starts at `http://localhost:9000`.

### Frontend

```bash
cd frontend
npm install
npm start
```

The Angular app starts at `http://localhost:4200`.

### API Endpoints

| Method | Endpoint               | Description               |
|--------|------------------------|---------------------------|
| GET    | `/api/users/me`        | Get current user profile  |
| GET    | `/api/users`           | List all users            |
| POST   | `/api/users/register`  | Register a new user       |
| POST   | `/api/clients/register`| Register an OAuth2 client |
| GET    | `/api/clients`         | List all clients          |

### OAuth2 Endpoints

| Endpoint                | Description           |
|-------------------------|-----------------------|
| `/oauth2/authorize`     | Authorization         |
| `/oauth2/token`         | Token                 |
| `/oauth2/jwks`          | JWK Set               |
| `/oauth2/revoke`        | Token Revocation      |
| `/oauth2/introspect`    | Token Introspection   |
| `/userinfo`             | OIDC UserInfo         |

## Default Clients

| Client ID      | Redirect URI                      | Grant Types                        |
|----------------|-----------------------------------|------------------------------------|
| web-client     | http://localhost:4200/callback    | authorization_code, refresh_token  |
| mobile-client  | com.auth.mobile://callback       | authorization_code, refresh_token  |

## License

MIT
