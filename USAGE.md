# DocGraph POC - Usage Guide

This guide provides detailed instructions for using the DocGraph POC application.

## Table of Contents

1. [Getting Started](#getting-started)
2. [Using the Application](#using-the-application)
3. [Understanding the Results](#understanding-the-results)
4. [Development Guide](#development-guide)
5. [API Reference](#api-reference)
6. [Troubleshooting](#troubleshooting)

## Getting Started

### Prerequisites

- Docker and Docker Compose (recommended)
- OR individual setup:
  - Node.js 20+
  - Java 17+
  - Neo4j 5.x

### Quick Start with Docker

The easiest way to run the application is using Docker Compose:

```bash
# Clone the repository
git clone https://github.com/benjiiDELPECH/doc-graph-toolkit.git
cd doc-graph-toolkit

# Start all services
docker compose up --build

# Wait for all services to start (about 2-3 minutes)
# You'll see messages indicating services are ready
```

Once started, access the application at:
- **Frontend UI**: http://localhost:4200
- **Backend API**: http://localhost:8080
- **Neo4j Browser**: http://localhost:7474 (username: neo4j, password: password)

### Individual Service Setup

#### Backend Only

```bash
cd backend
./gradlew bootRun
```

#### Frontend Only

```bash
cd frontend
npm install
npm start
```

#### Neo4j

```bash
docker run -d \
  --name neo4j \
  -p 7474:7474 -p 7687:7687 \
  -e NEO4J_AUTH=neo4j/password \
  neo4j:5.15.0
```

## Using the Application

### 1. Prepare Your PDF Document

The application works best with PDF documents that contain:
- Text content (not scanned images)
- Structured information with entities (names, organizations, locations)
- Clear relationships between entities

Example documents that work well:
- Research papers
- Business reports
- Legal documents
- News articles
- Technical documentation

### 2. Upload and Process

1. Open the application at http://localhost:4200
2. Click **"Choose PDF File"** button
3. Select your PDF file from your computer
4. Click **"Process Document"** button
5. Wait for processing to complete (typically 5-30 seconds depending on document size)

### 3. View Results

After processing, you'll see three main sections:

#### Induced Ontology
- **Entity Types**: Categories of entities detected (Person, Organization, Location, etc.)
- **Relation Types**: Types of relationships found between entities

#### Knowledge Graph
- **Interactive visualization** showing nodes (entities) and edges (relationships)
- **Color-coded nodes** by entity type:
  - 🔴 Red: Person
  - 🔵 Teal: Organization
  - 🔵 Blue: Location
  - 🟠 Orange: Date
  - 🟢 Green: Event/Concept/Term

#### Evidence Panel
- Click any **node** or **edge** to view evidence details
- Shows the **page number** where the entity/relation was found
- Displays a **text snippet** with context

## Understanding the Results

### Multi-Pass Workflow

The application processes documents through 6 passes:

1. **PDF Parsing**: Extracts text and metadata using Apache Tika
2. **Ontology Induction**: Analyzes content to identify entity and relation types
3. **Entity Extraction**: Finds entities with page numbers and evidence snippets
4. **Relation Detection**: Identifies relationships between entities
5. **Validation**: Checks graph structure and consistency
6. **Storage & Retrieval**: Saves to Neo4j and returns for visualization

### Entity Types

The system dynamically identifies these entity types:

- **Person**: Names of individuals (e.g., "John Smith")
- **Organization**: Companies and institutions (e.g., "Acme Corp")
- **Location**: Places and addresses (e.g., "New York, NY")
- **Date**: Temporal references (e.g., "2024")
- **Event**: Significant occurrences
- **Concept**: Important ideas or topics
- **Term**: Key terminology

### Relation Types

Common relation types detected:

- **mentions**: General co-occurrence
- **relates-to**: Generic relationship
- **part-of**: Hierarchical relationship
- **associated-with**: Association
- **located-in**: Spatial relationship
- **works-for**: Employment relationship
- **occurs-at**: Temporal/spatial event relationship

### Graph Visualization Features

- **Pan**: Click and drag the background
- **Zoom**: Mouse wheel or pinch gesture
- **Select**: Click nodes or edges to view details
- **Drag nodes**: Click and drag nodes to rearrange
- **Hover**: Hover over nodes/edges to see tooltips

## Development Guide

### Backend Development

```bash
cd backend

# Run with hot reload
./gradlew bootRun --continuous

# Run tests
./gradlew test

# Build JAR
./gradlew build
```

### Frontend Development

```bash
cd frontend

# Development server with hot reload
npm start

# Run tests
npm test

# Build for production
npm run build
```

### Adding Custom Entity Extractors

Edit `backend/src/main/kotlin/com/docgraph/poc/service/EntityExtractionService.kt`:

```kotlin
// Add new entity type pattern
if (ontology.entityTypes.contains("Email")) {
    val emailPattern = Regex("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b")
    emailPattern.findAll(text).forEach { match ->
        // Extract email entities
    }
}
```

### Customizing Ontology Induction

Edit `backend/src/main/kotlin/com/docgraph/poc/service/OntologyInductionService.kt`:

```kotlin
private fun identifyEntityTypes(content: String): List<String> {
    // Add your custom entity type detection logic
    if (content.contains("specific pattern")) {
        types.add("CustomType")
    }
    return types.toList()
}
```

## API Reference

### POST /api/process

Upload and process a PDF document.

**Request:**
```
Content-Type: multipart/form-data
Parameter: file (PDF file)
```

**Response:**
```json
{
  "ontology": {
    "entityTypes": ["Person", "Organization", "Location"],
    "relationTypes": ["mentions", "relates-to"],
    "schema": {}
  },
  "graph": {
    "nodes": [
      {
        "id": "John Smith",
        "label": "John Smith",
        "type": "Person",
        "evidence": {
          "page": 1,
          "snippet": "...John Smith is the CEO..."
        }
      }
    ],
    "edges": [
      {
        "source": "John Smith",
        "target": "Acme Corp",
        "label": "works-for",
        "evidence": {
          "page": 1,
          "snippet": "...John Smith at Acme Corp..."
        }
      }
    ]
  },
  "status": "success",
  "message": "Document processed successfully"
}
```

### GET /api/graph

Retrieve the current knowledge graph.

**Response:**
```json
{
  "nodes": [...],
  "edges": [...]
}
```

### GET /api/health

Health check endpoint.

**Response:**
```json
{
  "status": "UP",
  "service": "docgraph-backend"
}
```

## Troubleshooting

### Docker Issues

**Services won't start:**
```bash
# Check if ports are already in use
lsof -i :4200  # Frontend
lsof -i :8080  # Backend
lsof -i :7474  # Neo4j HTTP
lsof -i :7687  # Neo4j Bolt

# Stop and remove containers
docker compose down -v

# Rebuild and restart
docker compose up --build --force-recreate
```

**Neo4j connection errors:**
```bash
# Wait for Neo4j to fully start (check health)
docker compose logs neo4j

# Verify Neo4j is accessible
curl http://localhost:7474
```

### Backend Issues

**Build failures:**
```bash
# Clean build
cd backend
./gradlew clean build

# Check Java version
java -version  # Should be 17+
```

**Runtime errors:**
```bash
# Check Neo4j connection
# Edit backend/src/main/resources/application.properties
spring.neo4j.uri=bolt://localhost:7687
```

### Frontend Issues

**Build failures:**
```bash
# Clear node modules and reinstall
cd frontend
rm -rf node_modules package-lock.json
npm install

# Check Node version
node --version  # Should be 20+
```

**CORS errors:**
```bash
# Verify backend is running
curl http://localhost:8080/api/health

# Check CORS configuration in backend
# backend/src/main/kotlin/com/docgraph/poc/config/WebConfig.kt
```

### Processing Issues

**No entities extracted:**
- Ensure PDF contains extractable text (not just images)
- Check PDF is not password-protected
- Verify the PDF has meaningful content (names, organizations, etc.)

**Incorrect entity detection:**
- Entity extraction uses pattern matching heuristics
- For production use, consider integrating NLP libraries like Stanford CoreNLP or spaCy
- Customize patterns in `EntityExtractionService.kt`

**Graph not displaying:**
- Check browser console for JavaScript errors
- Verify nodes and edges are returned in API response
- Try clearing browser cache

## Advanced Configuration

### Environment Variables

Backend (`backend/src/main/resources/application.properties`):
```properties
# Server
server.port=8080

# Neo4j
spring.neo4j.uri=bolt://neo4j:7687
spring.neo4j.authentication.username=neo4j
spring.neo4j.authentication.password=password

# Logging
logging.level.com.docgraph=DEBUG
```

Frontend (`frontend/src/app/api.service.ts`):
```typescript
private apiUrl = 'http://localhost:8080/api';
```

Docker Compose (`docker compose.yml`):
```yaml
environment:
  - NEO4J_dbms_memory_pagecache_size=512M
  - NEO4J_dbms_memory_heap_max__size=1G
```

## Performance Tips

1. **Large PDFs**: Processing time increases with document size. Consider breaking very large documents into sections.

2. **Neo4j Memory**: Increase Neo4j memory for better performance:
   ```yaml
   NEO4J_dbms_memory_heap_max__size=2G
   ```

3. **Concurrent Requests**: The backend uses Kotlin coroutines for efficient multi-pass processing.

4. **Graph Rendering**: Large graphs (1000+ nodes) may be slow to render. Consider filtering or pagination for production use.

## Next Steps

- Integrate advanced NLP libraries for better entity recognition
- Add support for more document formats (Word, TXT, HTML)
- Implement graph querying with Cypher
- Add export functionality (GraphML, JSON, CSV)
- Implement user authentication and multi-tenancy
- Add graph analytics and metrics
- Create custom visualization layouts

## Support

For issues and questions:
- GitHub Issues: https://github.com/benjiiDELPECH/doc-graph-toolskit/issues
- Documentation: See README.md in the repository root
