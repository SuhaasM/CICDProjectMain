#!/bin/bash

# Quick deployment script for updating the production environment
echo "Starting deployment process..."

# Build Docker images
echo "Building Docker images..."
docker build -t cicdproject-backend:latest ./backend
docker build -t cicdproject-frontend:latest ./frontend

# Tag images for Docker Hub
echo "Tagging images..."
docker tag cicdproject-backend:latest $DOCKER_HUB_USERNAME/cicdproject-backend:latest
docker tag cicdproject-frontend:latest $DOCKER_HUB_USERNAME/cicdproject-frontend:latest

# Push images to Docker Hub
echo "Pushing images to Docker Hub..."
docker push $DOCKER_HUB_USERNAME/cicdproject-backend:latest
docker push $DOCKER_HUB_USERNAME/cicdproject-frontend:latest

# Deploy using docker-compose
echo "Deploying with docker-compose..."
docker-compose down
docker-compose up -d

echo "Deployment completed successfully!"