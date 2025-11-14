@echo off
REM Script to copy frontend files to Spring Boot static resources
echo Copying frontend files to Spring Boot static folder...
xcopy /E /I /Y "front-end\client\*" "ev\src\main\resources\static\"
echo Done! Frontend files copied successfully.
pause
