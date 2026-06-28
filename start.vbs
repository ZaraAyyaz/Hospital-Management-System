Set shell = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")

basePath = fso.GetParentFolderName(WScript.ScriptFullName)

' Open backend window
shell.Run "cmd.exe /K cd /d " & basePath & "\server && echo Installing dependencies... && call npm install --silent && echo. && echo Starting backend on port 5000... && node index.js", 2, False

WScript.Sleep 3000

' Open frontend window
shell.Run "cmd.exe /K cd /d " & basePath & "\client && echo Starting frontend on port 3000... && npx vite --host", 2, False

WScript.Sleep 2000

' Show instructions
shell.Popup "Smart Portal started!" & vbCrLf & vbCrLf & _
  "Backend:  http://localhost:5000" & vbCrLf & _
  "Frontend: http://localhost:3000" & vbCrLf & vbCrLf & _
  "Passwords: pass123", 5, "Smart Portal", 64
