# How to Run the Project

> **Note:** This guide is derived directly from the actual source code and configuration as of
> the current version. Ignore any older `.bat` scripts or `.md` files in the repo — they may
> be outdated from previous versions.

---

## Prerequisites

| Requirement | Minimum Version | How to Check |
|---|---|---|
| **Java (JDK)** | **17** | `java -version` |
| **Node.js** | 18+ | `node -v` |
| **npm** | 9+ | `npm -v` |

- The project uses **Java 17** (set in `pom.xml` → `<java.version>17</java.version>`).
- The system default Java may be a different version. You need JDK 17 available — it does **not** have to be the system default (see instructions below).
- **No database is needed.** All data is stored in RAM.

---

## Project Structure

```
All-In-One_Shop-App-main/
├── backend/               ← Spring Boot API (Java 17 + Maven)
│   ├── pom.xml
│   ├── mvnw.cmd           ← Maven wrapper (no global Maven needed)
│   └── src/
├── src/                   ← React frontend (TypeScript + Vite)
│   ├── app/
│   └── main.tsx
├── package.json           ← Frontend dependencies
└── vite.config.ts         ← Vite configuration
```

---

## Step 1 — Start the Backend

Open a terminal in the project root.

### On Windows (CMD or PowerShell)

```powershell
# Point JAVA_HOME to JDK 17 for this session
$env:JAVA_HOME = "C:\Users\Admin\.jdks\openjdk-17"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Verify it's Java 17
java -version

# Navigate into the backend directory and start
cd backend
.\mvnw.cmd spring-boot:run
```

Or as a one-liner:
```powershell
$env:JAVA_HOME="C:\Users\Admin\.jdks\openjdk-17"; $env:PATH="$env:JAVA_HOME\bin;$env:PATH"; cd backend; .\mvnw.cmd spring-boot:run
```

### On macOS / Linux

```bash
# Set JAVA_HOME to your JDK 17 installation
export JAVA_HOME=/path/to/jdk-17
export PATH="$JAVA_HOME/bin:$PATH"

cd backend
./mvnw spring-boot:run
```

### What happens

- Maven downloads all dependencies on first run (may take 1–2 minutes)
- The API starts on **`http://localhost:8080/api`**
- Swagger UI is available at **`http://localhost:8080/api/swagger-ui.html`**
- All data lives in memory — the server starts with a clean slate every time
- Press `Ctrl+C` to stop

### Configuration (application.yml)

These are the active settings — no environment variables are required for local development:

| Setting | Default Value | Notes |
|---|---|---|
| Server port | `8080` | API served at `/api` context path |
| JWT secret | Embedded default | Fine for development, set `JWT_SECRET` env var for production |
| CORS origins | `http://localhost:5173, :3000, :3001` | Matches Vite dev server |
| Database | **None** | All data in `ConcurrentHashMap` |

---

## Step 2 — Start the Frontend

Open a **second terminal** in the project root (keep the backend running).

```bash
# Install dependencies (first time only)
npm install

# Start the Vite dev server
npm run dev
```

### What happens

- Vite starts the dev server on **`http://localhost:5173`**
- The frontend automatically proxies API calls to `http://localhost:8080/api`
- Hot Module Replacement (HMR) is active — code changes appear instantly
- Press `Ctrl+C` to stop

---

## Step 3 — Verify Everything Works

Once both servers are running:

| What | URL | Expected |
|---|---|---|
| Frontend | http://localhost:5173 | Landing page loads |
| API health check | http://localhost:8080/api/products | JSON response (`success: true`) |
| Swagger docs | http://localhost:8080/api/swagger-ui.html | Interactive API documentation |
| Live Feed (Silver) | http://localhost:5173/live | WebSocket + Faker page |

### Quick smoke test

1. Open **http://localhost:5173**
2. Click **Sign Up** → register a new account (any email/password with 8+ chars)
3. Search for products (empty initially — use Faker to populate)
4. Go to **http://localhost:5173/live** → click **Start** to generate fake products via WebSocket
5. Go back to the main page → search to see the generated products

---

## Running Tests

### Backend (JUnit 5)

```powershell
$env:JAVA_HOME = "C:\Users\Admin\.jdks\openjdk-17"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

cd backend
.\mvnw.cmd test
```

- Runs 9 test suites (~90+ tests)
- JaCoCo coverage report generated at: `backend/target/site/jacoco/index.html`

### Frontend (Playwright E2E)

```bash
npx playwright test
```

---

## Available API Endpoints (Quick Reference)

All endpoints are under `http://localhost:8080/api`.

| Endpoint | Method | Auth | Description |
|---|---|---|---|
| `/auth/register` | POST | — | Register (returns JWT) |
| `/auth/login` | POST | — | Login (returns JWT) |
| `/auth/me` | GET | Bearer | Current user profile |
| `/products` | GET | — | List all products |
| `/products/search` | POST | — | Search with filters (paginated) |
| `/products/{id}` | GET | — | Product detail |
| `/brands` | GET | — | All brands |
| `/categories` | GET | — | All categories |
| `/stores` | GET | — | All stores |
| `/favorites` | GET | Bearer | User's favorites |
| `/favorites/{id}` | POST | Bearer | Add to favorites |
| `/faker/start` | POST | — | Start fake data generation |
| `/faker/stop` | POST | — | Stop generation |
| `/faker/status` | GET | — | Check if running |

See Swagger UI for the full interactive reference.

---

## Troubleshooting

### `java.lang.UnsupportedClassVersionError` or compilation fails
Your Java version isn't 17. Make sure `JAVA_HOME` points to JDK 17 and that `java -version` prints `17.x.x` **in the same terminal** you're running Maven from.

### `Port 8080 already in use`
Another process is using port 8080. Either stop it or change the port in `backend/src/main/resources/application.yml`:
```yaml
server:
  port: 8081
```

### Frontend can't reach the API (Network Error)
- Make sure the backend is running first
- Check that `http://localhost:8080/api/products` responds in your browser
- If you changed the backend port, create a `.env` file in the project root:
  ```
  VITE_API_URL=http://localhost:8081/api
  ```

### `npm install` fails
Try deleting `node_modules` and `package-lock.json`, then re-running:
```bash
rm -rf node_modules package-lock.json
npm install
```
