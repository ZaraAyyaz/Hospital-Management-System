Set shell = CreateObject("WScript.Shell")
shell.Run "cmd.exe /K cd /d D:\Desktop\OOP Project\server && npm install && node index.js", 1, False
WScript.Sleep 3000
shell.Run "cmd.exe /K cd /d D:\Desktop\OOP Project\client && npx vite --host", 1, False
