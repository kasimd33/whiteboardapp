# AntiGravityBoard

Production-ready real-time collaborative whiteboard application with JWT authentication, WebSocket (STOMP), and PostgreSQL.

## Tech Stack

- **Backend:** Java 21, Spring Boot 3.2
- **Database:** PostgreSQL
- **Realtime:** WebSocket (STOMP over SockJS)
- **Auth:** JWT
- **ORM:** Spring Data JPA (Hibernate)

## Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL 14+ (or use Docker)

## Quick Start

### Option 1: Run without Docker (easiest - uses H2 in-memory DB)

1. Ensure **Java 21** and **JAVA_HOME** are set.
2. Run:
   ```powershell
   cd D:\WhiteBoardApp
   .\run-dev.bat
   ```
   Or manually:
   ```powershell
   .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
   ```
3. Open **http://localhost:8080**

### Option 2: With PostgreSQL

1. **Start PostgreSQL** (or use Docker):
   ```powershell
   docker run -d --name pg -e POSTGRES_DB=antigravityboard -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres:16-alpine
   ```

2. **Run the application**:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

3. Open http://localhost:8080

### Option 2: Docker Compose

```bash
docker-compose up -d
```

Application: http://localhost:8080

## Setup Instructions

### 1. Configure Database

Create `application-local.yml` (optional) or use environment variables:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/antigravityboard
    username: postgres
    password: postgres
```

### 2. JWT Secret (Production)

Set a strong secret (min 32 characters):

```
JWT_SECRET=your-256-bit-secret-key-for-production
```

### 3. Create PostgreSQL Database

```sql
CREATE DATABASE antigravityboard;
```

## API Reference

### Auth

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/register | Register new user |
| POST | /api/auth/login | Login and get JWT |

### Boards

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/boards | Create board |
| GET | /api/boards/{id} | Get board |
| DELETE | /api/boards/{id} | Delete board (host only) |
| POST | /api/boards/{id}/join | Join board |

### WebSocket

- **Endpoint:** `/ws` (SockJS)
- **Topic:** `/topic/board/{boardId}`
- **Send:** `/app/board/{boardId}/draw`

## Curl Examples

### Register

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","email":"alice@example.com","password":"secret123","role":"HOST"}'
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"secret123"}'
```

### Create Board (use token from login)

```bash
curl -X POST http://localhost:8080/api/boards \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"name":"My Whiteboard"}'
```

### Get Board

```bash
curl -X GET http://localhost:8080/api/boards/BOARD_UUID \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Join Board

```bash
curl -X POST http://localhost:8080/api/boards/BOARD_UUID/join \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Delete Board

```bash
curl -X DELETE http://localhost:8080/api/boards/BOARD_UUID \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Project Structure

```
src/main/java/com/antigravity/board/
├── AntiGravityBoardApplication.java
├── config/          # Security, WebSocket
├── controller/      # REST controllers
├── dto/             # Data transfer objects
├── entity/          # JPA entities
├── repository/      # Spring Data repositories
├── security/        # JWT, filters
├── service/         # Business logic
├── ai/              # AI module (mock)
└── websocket/       # STOMP handlers
```

## Drawing Event Format

```json
{
  "boardId": "uuid",
  "userId": "uuid",
  "drawingType": "line|rectangle|circle|text|erase|clear",
  "coordinates": { ... },
  "color": "#000000",
  "strokeWidth": 3
}
```

## AI Module (Mock)

- `POST /api/ai/suggest-shape` - Suggest shape from points
- `POST /api/ai/straighten-line` - Auto-straighten line
- `POST /api/ai/detect-circle` - Detect circle from points
- `POST /api/ai/generate-diagram` - Generate diagram from text prompt

## GitHub & Deployment

### Push to GitHub

1. **Create a new repository** on [GitHub](https://github.com/new)
   - Name: `AntiGravityBoard` (or your choice)
   - Don't initialize with README (you already have one)

2. **Initialize and push** from your project folder:
   ```powershell
   cd D:\WhiteBoardApp
   git init
   git add .
   git commit -m "Initial commit - AntiGravityBoard whiteboard app"
   git branch -M main
   git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
   git push -u origin main
   ```

3. **CI** – On every push, GitHub Actions will build the project with PostgreSQL.

### Deploy Options

#### Option A: Railway (recommended – free tier)

1. Go to [railway.app](https://railway.app) and sign up with GitHub
2. **New Project** → **Deploy from GitHub repo** → Select your repo
3. Add **PostgreSQL** from the "+ New" menu
4. Click your **Spring Boot service** → **Variables** tab:
   - `DB_HOST` = (from PostgreSQL service)
   - `DB_NAME` = `railway`
   - `DB_USER` = (from PostgreSQL)
   - `DB_PASSWORD` = (from PostgreSQL)
   - `JWT_SECRET` = (generate a long random string)
5. Railway will detect the `Dockerfile` and deploy. Your app URL will be shown in the service.

#### Option B: Render

1. Go to [render.com](https://render.com) → **New** → **Web Service**
2. Connect your GitHub repo
3. **Environment**: Docker
4. Add **PostgreSQL** database in Render and note its Internal Database URL
5. **Environment Variables**:
   - `DB_HOST`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` (from Render PostgreSQL)
   - `JWT_SECRET` = (generate a long random string)
6. Deploy

#### Option C: Your own VPS (DigitalOcean, AWS EC2, etc.)

```bash
# On your server
git clone https://github.com/YOUR_USERNAME/YOUR_REPO.git
cd YOUR_REPO
docker compose up -d
```

Ensure PostgreSQL env vars and `JWT_SECRET` are set (e.g. in `docker-compose.yml` or `.env`).

## License

MIT
