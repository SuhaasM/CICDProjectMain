# Running the Application in Both Local and AWS Environments

This guide explains how to run the LMS application in both local and AWS environments simultaneously.

## Local Environment Setup

1. Run the application locally using the provided script:
   ```
   run-local.bat
   ```

2. Access the application at:
   - Frontend: http://localhost
   - Backend API: http://localhost:12345/api

## AWS Environment

The application is already configured to run on AWS EC2 using the main Docker Compose file.

## How It Works

- The frontend automatically detects whether it's running locally or on AWS and adjusts the API endpoint accordingly.
- CORS is configured to allow requests from both environments.
- The nginx configuration includes headers to support cross-origin requests.

## Troubleshooting

If you encounter issues:
1. Make sure ports 80 and 12345 are not in use by other applications
2. Check Docker logs with `docker-compose -f docker-compose.local.yml logs`
3. Ensure your firewall allows connections to these ports