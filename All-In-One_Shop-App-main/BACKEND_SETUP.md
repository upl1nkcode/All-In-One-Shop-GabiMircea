# Backend Setup Guide - Quick Start

## 🚨 Current Issue
**Login/Signup fails with "Failed to fetch"** because the backend server is not running.

## ✅ Quick Fix (2 Steps)

### Step 1: Install Maven (if not already installed)

**Option A: Using Chocolatey (Easiest)**
```powershell
# In PowerShell as Administrator
choco install maven
```

**Option B: Manual Installation**
1. Download Maven from: https://maven.apache.org/download.cgi
2. Extract to `C:\Program Files\Apache\maven`
3. Add to System PATH: `C:\Program Files\Apache\maven\bin`
4. Restart PowerShell

**Verify Installation:**
```powershell
mvn --version
```

### Step 2: Start the Backend

**From project root directory:**
```powershell
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**You should see:**
```
Started AllInOneShopApplication in X.XXX seconds
```

**Backend will be available at:** `http://localhost:8080/api`

---

## 🎯 What Was Fixed

### 1. **Added H2 In-Memory Database for Development**
- No longer requires Supabase setup for local development
- Database auto-creates on startup
- Perfect for testing login/signup

**File:** `backend/src/main/resources/application-dev.yml`

### 2. **Added H2 Dependency**
**File:** `backend/pom.xml`
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 3. **Development vs Production Profiles**

**Development** (uses H2 in-memory):
```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Production** (uses Supabase PostgreSQL):
```powershell
mvn spring-boot:run
# Requires SUPABASE_DB_* environment variables
```

---

## 🧪 Testing the Backend

### 1. Check if Backend is Running
Open browser: http://localhost:8080/api/swagger-ui.html

### 2. Test Registration
```powershell
# Using PowerShell
$body = @{
    email = "test@example.com"
    password = "password123"
    firstName = "Test"
    lastName = "User"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" `
    -Method POST `
    -Body $body `
    -ContentType "application/json"
```

### 3. Test Login
```powershell
$body = @{
    email = "test@example.com"
    password = "password123"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
    -Method POST `
    -Body $body `
    -ContentType "application/json"
```

You should get a response with a JWT token:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGci...",
    "user": { ... }
  }
}
```

---

## 📱 Frontend Setup

### 1. Install Dependencies
```powershell
cd ..  # Back to project root
pnpm install
# or npm install
```

### 2. Start Frontend
```powershell
pnpm dev
# or npm run dev
```

Frontend will run at: `http://localhost:5173`

---

## ✨ Now Try Login/Signup

1. Open `http://localhost:5173` in your browser
2. Click "Sign Up" or "Login"
3. It should now work! ✅

---

## 🔧 Troubleshooting

### Issue: "Port 8080 already in use"
```powershell
# Find process using port 8080
Get-Process -Id (Get-NetTCPConnection -LocalPort 8080).OwningProcess

# Kill the process (replace PID)
Stop-Process -Id <PID> -Force
```

### Issue: Maven command not found
Make sure Maven's bin directory is in your PATH:
```powershell
$env:Path
```

Should include: `C:\Program Files\Apache\maven\bin`

### Issue: Backend starts but frontend still fails
1. Check backend is running: http://localhost:8080/api/swagger-ui.html
2. Check frontend API URL in `src/app/api/client.ts` (should be `http://localhost:8080/api`)
3. Check browser console for CORS errors

### Issue: "Failed to configure a DataSource"
You're trying to run without the `-Dspring-boot.run.profiles=dev` flag.
The dev profile is required for H2 database.

---

## 🌐 For Production (Using Supabase)

### 1. Get Supabase Credentials
1. Go to Supabase dashboard
2. Settings > Database
3. Copy connection details

### 2. Set Environment Variables
```powershell
$env:SUPABASE_DB_HOST="db.xxx.supabase.co"
$env:SUPABASE_DB_PORT="5432"
$env:SUPABASE_DB_NAME="postgres"
$env:SUPABASE_DB_USER="postgres"
$env:SUPABASE_DB_PASSWORD="your-password"
$env:JWT_SECRET="your-long-secret-key-at-least-32-characters"
```

### 3. Run Database Schema
```powershell
# Connect to Supabase and run:
psql -h db.xxx.supabase.co -U postgres -d postgres -f scripts/001_create_schema.sql
```

### 4. Start Backend (Production Mode)
```powershell
cd backend
mvn spring-boot:run
```

---

## 📚 API Documentation

Once backend is running, visit:
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **OpenAPI Docs**: http://localhost:8080/api/api-docs

---

## 🎉 Success Checklist

- [ ] Maven installed and in PATH
- [ ] Backend starts without errors
- [ ] Can access Swagger UI at http://localhost:8080/api/swagger-ui.html
- [ ] Frontend starts at http://localhost:5173
- [ ] Can register new account
- [ ] Can login with credentials
- [ ] No "Failed to fetch" errors

---

## 💡 Tips

1. **Keep backend running in one terminal**
2. **Run frontend in another terminal**
3. **Use H2 Console** (when using dev profile):
   - URL: http://localhost:8080/api/h2-console
   - JDBC URL: `jdbc:h2:mem:testdb`
   - User: `sa`
   - Password: (leave empty)

---

Need help? Check the main [ARCHITECTURE.md](../ARCHITECTURE.md) for detailed system explanation.
