@echo off
setlocal
REM Script to copy frontend files (client + fonts) to Spring Boot static resources
echo Copying frontend files to Spring Boot static folder...

REM Copy client assets into static (use trailing . to avoid path issues)
xcopy /E /I /Y "front-end\client\*" "ev\src\main\resources\static\." >nul

REM Copy fonts into static/fonts
xcopy /E /I /Y "front-end\fonts\*" "ev\src\main\resources\static\fonts\." >nul

echo Done! Frontend files copied successfully.
endlocal
pause
