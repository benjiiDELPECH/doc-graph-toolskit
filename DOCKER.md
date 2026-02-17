# Docker Setup Guide

This guide explains the Docker and Docker Compose setup for the DocGraph POC application.

## Architecture

The application consists of three containerized services:

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│             │     │             │     │             │
│   Frontend  │────▶│   Backend   │────▶│    Neo4j    │
│  (Angular)  │     │  (Kotlin)   │     │  (Database) │
│             │     │             │     │             │
│   Port 80   │     │  Port 8080  │     │ Port 7687   │
│ (→4200)     │     │             │     │ Port 7474   │
└─────────────┘     └─────────────┘     └─────────────┘
```

## Services

### 1. Neo4j Database
- **Image**: `neo4j:5.15.0`
- **Ports**: 
  - 7474 (HTTP - Web UI)
  - 7687 (Bolt - Database protocol)
- **Credentials**: neo4j / password
- **Volumes**: 
  - `neo4j_data` - Database storage
  - `neo4j_logs` - Log files

### 2. Backend (Spring Boot + Kotlin)
- **Build**: Multi-stage Dockerfile
  - Stage 1: Build with Gradle
  - Stage 2: Run with JRE
- **Port**: 8080
- **Dependencies**: Neo4j (with health check)

### 3. Frontend (Angular + Nginx)
- **Build**: Multi-stage Dockerfile
  - Stage 1: Build with Node.js
  - Stage 2: Serve with Nginx
- **Port**: 4200 (mapped from internal 80)
- **Dependencies**: Backend (with health check)

## Docker Compose Commands

### Start All Services

```bash
# Build and start in foreground
docker-compose up --build

# Build and start in background (detached)
docker-compose up --build -d

# View logs
docker-compose logs -f

# View logs for specific service
docker-compose logs -f backend
```

### Stop Services

```bash
# Stop services (preserves data)
docker-compose stop

# Stop and remove containers (preserves volumes)
docker-compose down

# Stop, remove containers, and delete volumes (clean slate)
docker-compose down -v
```

### Rebuild Services

```bash
# Rebuild specific service
docker-compose build backend

# Rebuild without cache
docker-compose build --no-cache

# Rebuild and restart
docker-compose up --build --force-recreate
```

### Service Management

```bash
# Start specific service
docker-compose start backend

# Restart specific service
docker-compose restart frontend

# View service status
docker-compose ps

# Execute command in running container
docker-compose exec backend /bin/bash
```

## Dockerfile Details

### Backend Dockerfile

```dockerfile
# Build stage with Gradle
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY src ./src
RUN gradle build --no-daemon -x test

# Runtime stage with JRE
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Benefits:**
- Multi-stage build reduces final image size
- Uses Gradle cache for faster rebuilds
- Alpine base for minimal footprint

### Frontend Dockerfile

```dockerfile
# Build stage with Node.js
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Runtime stage with Nginx
FROM nginx:alpine
COPY --from=build /app/dist/docgraph-ui/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

**Benefits:**
- Optimized build with npm ci
- Production build for Angular
- Nginx for efficient static file serving
- API proxy configuration

## Health Checks

All services include health checks for proper startup ordering:

### Neo4j
```yaml
healthcheck:
  test: ["CMD-SHELL", "wget --no-verbose --tries=1 --spider localhost:7474 || exit 1"]
  interval: 10s
  timeout: 5s
  retries: 5
```

### Backend
```yaml
healthcheck:
  test: ["CMD-SHELL", "wget --no-verbose --tries=1 --spider localhost:8080/api/health || exit 1"]
  interval: 10s
  timeout: 5s
  retries: 5
```

### Service Dependencies
```yaml
backend:
  depends_on:
    neo4j:
      condition: service_healthy

frontend:
  depends_on:
    backend:
      condition: service_healthy
```

## Network Configuration

All services communicate on a custom bridge network `docgraph-network`:

```yaml
networks:
  docgraph-network:
    driver: bridge
```

**Service Discovery:**
- Services can reference each other by name
- Backend connects to Neo4j at `bolt://neo4j:7687`
- Frontend proxies API requests to `http://backend:8080`

## Volume Management

### View Volumes
```bash
docker volume ls | grep docgraph
```

### Inspect Volume
```bash
docker volume inspect doc-graph-toolskit_neo4j_data
```

### Backup Neo4j Data
```bash
# Create backup
docker-compose exec neo4j neo4j-admin database dump neo4j \
  --to-path=/backups

# Copy from container
docker cp docgraph-neo4j:/backups/neo4j.dump ./backup.dump
```

### Restore Neo4j Data
```bash
# Copy to container
docker cp ./backup.dump docgraph-neo4j:/backups/

# Restore
docker-compose exec neo4j neo4j-admin database load neo4j \
  --from-path=/backups/neo4j.dump
```

## Troubleshooting

### Container Won't Start

```bash
# Check container logs
docker-compose logs [service-name]

# Check if ports are in use
netstat -tulpn | grep -E '4200|8080|7474|7687'

# Remove and recreate
docker-compose down -v
docker-compose up --build
```

### Out of Disk Space

```bash
# Remove unused images
docker image prune -a

# Remove unused volumes
docker volume prune

# Remove everything (careful!)
docker system prune -a --volumes
```

### Slow Build Times

```bash
# Use Docker BuildKit
export DOCKER_BUILDKIT=1
docker-compose build

# Build specific service only
docker-compose build backend

# Use build cache
docker-compose build --parallel
```

### Network Issues

```bash
# Inspect network
docker network inspect doc-graph-toolskit_docgraph-network

# Test connectivity between containers
docker-compose exec backend ping neo4j
docker-compose exec frontend ping backend
```

## Production Deployment

### Environment Variables

Create `.env` file:
```env
NEO4J_PASSWORD=change-this-password
BACKEND_PORT=8080
FRONTEND_PORT=80
```

Use in docker-compose:
```yaml
environment:
  - NEO4J_AUTH=neo4j/${NEO4J_PASSWORD}
```

### SSL/TLS Configuration

For production, add SSL certificates:

```yaml
frontend:
  volumes:
    - ./ssl/cert.pem:/etc/nginx/ssl/cert.pem:ro
    - ./ssl/key.pem:/etc/nginx/ssl/key.pem:ro
```

Update nginx.conf:
```nginx
server {
    listen 443 ssl;
    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;
    # ... rest of config
}
```

### Resource Limits

Add resource constraints:

```yaml
backend:
  deploy:
    resources:
      limits:
        cpus: '1.0'
        memory: 2G
      reservations:
        cpus: '0.5'
        memory: 1G
```

### Logging

Configure log drivers:

```yaml
services:
  backend:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
```

## Monitoring

### Container Stats

```bash
# Real-time resource usage
docker stats

# Specific container
docker stats docgraph-backend
```

### Health Status

```bash
# Check all containers health
docker-compose ps

# Inspect specific service
docker inspect --format='{{.State.Health.Status}}' docgraph-backend
```

## Scaling (Docker Swarm)

For horizontal scaling with Docker Swarm:

```yaml
services:
  backend:
    deploy:
      replicas: 3
      update_config:
        parallelism: 1
        delay: 10s
      restart_policy:
        condition: on-failure
```

Deploy:
```bash
docker stack deploy -c docker-compose.yml docgraph
```

## Kubernetes Alternative

For Kubernetes deployment, generate manifests:

```bash
# Using kompose
kompose convert -f docker-compose.yml

# Or manually create k8s manifests
kubectl create deployment backend --image=docgraph-backend:latest
kubectl create service loadbalancer backend --tcp=8080:8080
```

## Best Practices

1. **Use specific image tags** instead of `latest`
2. **Implement health checks** for all services
3. **Use multi-stage builds** to reduce image size
4. **Run as non-root user** in production
5. **Use secrets management** for sensitive data
6. **Enable log rotation** to prevent disk fill
7. **Monitor resource usage** and set limits
8. **Regular security updates** of base images
9. **Use .dockerignore** to exclude unnecessary files
10. **Test locally** before deploying to production

## Additional Resources

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Reference](https://docs.docker.com/compose/compose-file/)
- [Neo4j Docker Guide](https://neo4j.com/docs/operations-manual/current/docker/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [Angular Docker Guide](https://angular.io/guide/deployment)
