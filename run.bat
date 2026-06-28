@echo off
title Smart Portal - Hospital Management System
cd /d "%~dp0"

cls
echo.
echo ╔══════════════════════════════════════════════════════════╗
echo ║        Smart Portal - Hospital Management System        ║
echo ╚══════════════════════════════════════════════════════════╝
echo.

:: Install dependencies
if not exist server\node_modules (
  echo [1/3] Installing server dependencies...
  cd server
  call npm install --silent
  cd ..
)

:: Seed database (fresh)
echo [2/3] Seeding database with demo data...
if exist server\hospital.db del server\hospital.db
cd server
node seed.js
cd ..

:: Build frontend (always fresh)
echo [3/3] Building frontend...
cd client
call npx vite build --silent
cd ..

:: Start server
cls
echo.
echo ╔══════════════════════════════════════════════════════════╗
echo ║                    READY TO GO                          ║
echo ║                                                          ║
echo ║  Open:  http://localhost:5000                            ║
echo ║                                                          ║
echo ║  Credentials (password: pass123)                         ║
echo ║    Patient      : patient1                               ║
echo ║    Doctor       : doctor1                                ║
echo ║    Receptionist : receptionist1                          ║
echo ║    Admin        : admin1                                 ║
echo ╚══════════════════════════════════════════════════════════╝
echo.
start "" http://localhost:5000
node server\index.js
pause
