# DocGraph POC - Document Graph Toolkit

[![CI Pipeline](https://github.com/benjiiDELPECH/doc-graph-toolskit/workflows/CI%20Pipeline/badge.svg)](https://github.com/benjiiDELPECH/doc-graph-toolskit/actions/workflows/ci.yml)
[![CodeQL](https://github.com/benjiiDELPECH/doc-graph-toolskit/workflows/CodeQL/badge.svg)](https://github.com/benjiiDELPECH/doc-graph-toolskit/actions/workflows/ci.yml)
[![Release](https://github.com/benjiiDELPECH/doc-graph-toolskit/workflows/Release/badge.svg)](https://github.com/benjiiDELPECH/doc-graph-toolskit/actions/workflows/release.yml)

A complete monorepo application for processing PDF documents with multi-pass workflow, extracting knowledge graphs, and visualizing them in an interactive UI.

## Architecture

### Frontend
- **Angular 19.2.18** with standalone components
- **vis-network** for interactive graph visualization
- File upload and real-time graph rendering
- Evidence display for entities and relations

### Backend
- **Spring Boot 3** with Kotlin
- **Apache Tika** for PDF parsing
- Dynamic ontology induction
- Entity and relation extraction with evidence (page + snippet)
- **Neo4j** graph database integration
- RESTful API endpoints

### Database
- **Neo4j 5.15** for graph storage and querying

## Multi-Pass Workflow (Koog-inspired)

1. **Pass 1: PDF Parsing** - Apache Tika extracts text and metadata from PDF
2. **Pass 2: Ontology Induction** - Dynamically identifies entity types and relation types
3. **Pass 3: Entity & Relation Extraction** - Extracts entities with evidence (page + snippet)
4. **Pass 4: Validation** - Validates graph structure and relationships
5. **Pass 5: Storage** - Stores graph in Neo4j database
6. **Pass 6: Retrieval** - Returns graph data for visualization

## Prerequisites

- Docker and Docker Compose
- OR:
  - Node.js 20+ and npm
  - Java 17+
  - Gradle 8.5+
  - Neo4j 5.x

## Quick Start with Docker

```bash
# Build and start all services
docker compose up --build

# Access the application
# Frontend: http://localhost:4200
# Backend API: http://localhost:8080
# Neo4j Browser: http://localhost:7474 (neo4j/password)
```

## Development Setup

### Backend

```bash
cd backend

# Build
./gradlew build

# Run
./gradlew bootRun

# API will be available at http://localhost:8080
```

### Frontend

```bash
cd frontend

# Install dependencies
npm install

# Run dev server
npm start

# Application will be available at http://localhost:4200
```

### Neo4j

```bash
# Run Neo4j with Docker
docker run -d \
  --name neo4j \
  -p 7474:7474 -p 7687:7687 \
  -e NEO4J_AUTH=neo4j/password \
  neo4j:5.15.0
```

## API Endpoints

### POST /api/process
Upload and process a PDF document
- **Content-Type:** multipart/form-data
- **Parameter:** file (PDF file)
- **Response:** ProcessingResult with ontology and graph

### GET /api/graph
Retrieve the current knowledge graph
- **Response:** GraphData with nodes and edges

### GET /api/health
Health check endpoint
- **Response:** Service status

## Project Structure

```
docgraph-poc/
├── backend/                 # Spring Boot Kotlin backend
│   ├── src/
│   │   └── main/
│   │       ├── kotlin/
│   │       │   └── com/docgraph/poc/
│   │       │       ├── config/      # Configuration
│   │       │       ├── controller/  # REST controllers
│   │       │       ├── model/       # Data models
│   │       │       ├── repository/  # Neo4j repositories
│   │       │       └── service/     # Business logic
│   │       └── resources/
│   ├── build.gradle.kts
│   └── Dockerfile
├── frontend/                # Angular frontend
│   ├── src/
│   │   └── app/
│   │       ├── graph-viewer/  # Graph visualization component
│   │       ├── api.service.ts # API service
│   │       └── app.component.*
│   ├── package.json
│   ├── nginx.conf
│   └── Dockerfile
└── docker compose.yml       # Docker orchestration
```

## Features

### Document Processing
- PDF upload and parsing with Apache Tika
- Page-by-page text extraction
- Metadata extraction

### Ontology Induction
- Dynamic entity type detection (Person, Organization, Location, Date, Event, Concept)
- Relation type identification
- JSON schema generation

### Entity Extraction
- Pattern-based entity recognition
- Page number tracking
- Context snippet extraction for evidence

### Relation Extraction
- Co-occurrence based relation detection
- Proximity analysis
- Evidence collection with page and snippet

### Graph Visualization
- Interactive node-edge graph
- Color-coded entity types
- Click to view evidence
- Pan, zoom, and drag functionality
- Detailed information panel

## Technologies

- **Frontend:** Angular 17, TypeScript, vis-network, RxJS
- **Backend:** Spring Boot 3, Kotlin, Apache Tika, Coroutines
- **Database:** Neo4j 5.15, Spring Data Neo4j
- **Containerization:** Docker, Docker Compose
- **Build Tools:** Gradle 8.5, npm

## Example Usage

1. Start the application with `docker compose up`
2. Navigate to http://localhost:4200
3. Click "Choose PDF File" and select a PDF document
4. Click "Process Document" to start the multi-pass workflow
5. View the induced ontology (entity and relation types)
6. Explore the interactive knowledge graph
7. Click on nodes or edges to see evidence details

## License

MIT

## Contributing

Contributions are welcome! Please open an issue or submit a pull request.
