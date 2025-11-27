# Kubernetes Deployment for LMS

## Prerequisites
- A Kubernetes cluster (minikube, kind, or cloud)
- NGINX Ingress Controller installed for ingress routing
- Docker images pushed to Docker Hub (see CI/CD workflow)

## Quick Start
1. Replace `DOCKER_HUB_USERNAME_PLACEHOLDER` in `backend-deployment.yaml` and `frontend-deployment.yaml` with your Docker Hub username (or use `kubectl kustomize` overlays).
2. Apply all manifests:
   ```
   kubectl apply -f k8s/namespace.yaml
   kubectl apply -f k8s/secret.yaml
   kubectl apply -f k8s/mysql-deployment.yaml
   kubectl apply -f k8s/mysql-service.yaml
   kubectl apply -f k8s/backend-deployment.yaml
   kubectl apply -f k8s/backend-service.yaml
   kubectl apply -f k8s/frontend-deployment.yaml
   kubectl apply -f k8s/frontend-service.yaml
   kubectl apply -f k8s/ingress.yaml
   ```

3. Access the app via the Ingress controller. If using minikube:
   ```
   minikube addons enable ingress
   minikube ip
   # then open http://<minikube-ip>/
   ```

## Notes
- MySQL uses a 1Gi `PersistentVolumeClaim` named `mysql-pvc`; ensure your cluster has a default StorageClass.
- Backend service is available at `http://<ingress-host>/api`.
- Frontend service is served at `/`.
- DB credentials are stored in the `lms-db-secret`.