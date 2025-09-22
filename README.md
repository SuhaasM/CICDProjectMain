# Full-Stack CI/CD Project

A full-stack application with React frontend, Spring Boot backend, and MySQL database, deployed using Docker Compose with CI/CD pipeline using GitHub Actions and AWS deployment.

## Project Structure

- `frontend/`: React application
- `backend/`: Spring Boot application
- `docker-compose.yml`: Docker Compose configuration
- `.github/workflows/`: CI/CD pipeline configuration

## Local Development

### Prerequisites
- Docker and Docker Compose
- Java 21
- Node.js 18

### Running Locally with Docker Compose

```bash
docker-compose up -d
```

Access the application at http://localhost

## CI/CD Pipeline

This project uses GitHub Actions for CI/CD:
1. Builds and tests the application
2. Creates Docker images
3. Pushes images to Docker Hub
4. Deploys to AWS EC2

## AWS Deployment

See [aws-setup.md](aws-setup.md) for detailed AWS deployment instructions.

## GitHub Repository Setup

1. Add the following secrets to your GitHub repository:
   - `AWS_ACCESS_KEY_ID`
   - `AWS_SECRET_ACCESS_KEY`
   - `AWS_REGION`
   - `EC2_HOST`
   - `EC2_USERNAME`
   - `SSH_PRIVATE_KEY`
   - `DOCKER_HUB_USERNAME`
   - `DOCKER_HUB_TOKEN`