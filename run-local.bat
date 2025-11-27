@echo off
echo Starting LMS application locally...

echo Stopping any running containers...
docker-compose -f docker-compose.local.yml down

echo Building and starting containers...
docker-compose -f docker-compose.local.yml up -d --build

echo Application started!
echo Frontend: http://localhost:8090
echo Backend API: http://localhost:12345/api
echo.
echo Press any key to stop the application...
pause > nul

echo Stopping application...
docker-compose -f docker-compose.local.yml down
