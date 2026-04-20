# ⚡ QUICK FIX - Start Backend in 30 Seconds

## The Problem
Login/Signup shows "Failed to fetch" → Backend isn't running

## The Solution (2 Simple Steps)

### Step 1: Open PowerShell in Backend Folder
```powershell
# Navigate to backend
cd backend
```

### Step 2: Run This One Command
```powershell
$env:JAVA_HOME="C:\Users\Admin\.jdks\openjdk-17"; & ".\.mvn\wrapper\apache-maven-3.9.6\bin\mvn.cmd" spring-boot:run "-Dspring-boot.run.profiles=dev"
```

**That's it!** 

Wait 30-60 seconds, you should see:
```
Started AllInOneShopApplication in X.XXX seconds
```

## ✅ Verify It's Working

Open browser: http://localhost:8080/api/swagger-ui.html

You should see the API documentation page.

## 🎯 Now Test Login/Signup

1. Keep backend running (DON'T close the PowerShell window)
2. Open NEW PowerShell window
3. Start frontend:
   ```powershell
   pnpm dev
   ```
4. Go to http://localhost:5173
5. Try signing up or logging in → Should work!

## 🛑 Stop Backend

Press `Ctrl+C` in the PowerShell window where backend is running

## 📝 What Was Fixed

✅ Removed duplicate `UserDetailsServiceImpl`  
✅ Removed duplicate `CatalogController`  
✅ Added H2 in-memory database (no Supabase setup needed)  
✅ Added Maven Wrapper (no Maven installation needed)  
✅ Fixed Spring Boot configuration conflicts  

All fixes are committed to **main branch**! 🎉

## ❓ TroubleShooting

**Error: Port 8080 already in use**
```powershell
# Kill the process and try again
Stop-Process -Id (Get-NetTCPConnection -LocalPort 8080).OwningProcess -Force
```

**Error: Java not found**
Check Java 17 is installed:
```powershell
"C:\Users\Admin\.jdks\openjdk-17\bin\java.exe" -version
```

Should show: `openjdk version "17"`
