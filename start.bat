@echo off
title Smart Portal - Hospital Management System
cd /d "%~dp0"

:: ─── Step 1: Install dependencies ──────────────────────────
if not exist server\node_modules (
    echo Installing server dependencies...
    cd server && call npm install && cd ..
)
if not exist client\node_modules (
    echo Installing client dependencies...
    cd client && call npm install && cd ..
)

:: ─── Step 2: Seed database ─────────────────────────────────
echo Seeding database...
cd server
call node seed.js
cd ..

:: ─── Step 3: Start backend (in this window) ───────────────
echo.
echo ╔══════════════════════════════════════════════════════════╗
echo ║     Backend starting on http://localhost:5000           ║
echo ║     Frontend starting on http://localhost:3000           ║
echo ║                                                          ║
echo ║  Open http://localhost:3000 in your browser              ║
echo ║  Login: patient1 / doctor1 / receptionist1 / admin1      ║
echo ║  Password: pass123                                       ║
echo ╚══════════════════════════════════════════════════════════╝
echo.

:: Start backend in background
start "Smart Portal Backend" /min node server\index.js
timeout /t 2 /nobreak >nul

:: Start frontend (visible so user can see port)
start "Smart Portal Frontend" cmd /k "cd /d %~dp0client && npx vite --host"
timeout /t 2 /nobreak >nul

echo Both servers started!
echo.
echo  Backend API : http://localhost:5000/api/health
echo  Frontend    : http://localhost:3000
echo.
pause
