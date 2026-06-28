@echo off
title Smart Portal Backend
cd /d "D:\Desktop\OOP Project\server"
echo Installing bcryptjs...
call npm install
echo Starting backend on port 4000...
node index.js
pause
