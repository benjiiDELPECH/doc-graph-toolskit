package com.docgraph.poc.service

import com.docgraph.poc.model.*
import kotlinx.coroutines.*
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import java.io.InputStream

@Service
class DocumentProcessingService(
    private val pdfParserService: PdfParserService,
    private val ontologyInductionService: OntologyInductionService,
    private val entityExtractionService: EntityExtractionService,
    private val graphValidationService: GraphValidationService,
    private val neo4jGraphService: Neo4jGraphService
) {
    private val logger = LoggerFactory.getLogger(DocumentProcessingService::class.java)

    suspend fun processDocument(inputStream: InputStream): ProcessingResult = coroutineScope {
        logger.info("Starting multi-pass document processing workflow")
        
        try {
            // Pass 1: Parse PDF with Apache Tika
            val parsedDocument = async(Dispatchers.IO) {
                logger.info("Pass 1: Parsing PDF")
                pdfParserService.parsePdf(inputStream)
            }.await()
            
            // Pass 2: Dynamically induce ontology
            val ontology = async(Dispatchers.Default) {
                logger.info("Pass 2: Inducing ontology")
                ontologyInductionService.induceOntology(parsedDocument)
            }.await()
            
            // Pass 3: Extract entities and relations with evidence
            val entities = async(Dispatchers.Default) {
                logger.info("Pass 3: Extracting entities and relations")
                entityExtractionService.extractEntitiesAndRelations(parsedDocument, ontology)
            }.await()
            
            // Pass 4: Validate graph
            val validationResult = async(Dispatchers.Default) {
                logger.info("Pass 4: Validating graph")
                graphValidationService.validateGraph(entities, ontology)
            }.await()
            
            if (!validationResult.isValid) {
                logger.warn("Validation failed: ${validationResult.errors}")
            }
            
            // Pass 5: Store in Neo4j
            async(Dispatchers.IO) {
                logger.info("Pass 5: Storing graph in Neo4j")
                try {
                    neo4jGraphService.storeGraph(entities)
                } catch (e: Exception) {
                    logger.warn("Neo4j storage failed (may not be available): ${e.message}")
                }
            }.await()
            
            // Pass 6: Retrieve and return graph
            val graph = async(Dispatchers.IO) {
                logger.info("Pass 6: Retrieving graph for visualization")
                try {
                    neo4jGraphService.retrieveGraph()
                } catch (e: Exception) {
                    // If Neo4j fails, build graph from entities
                    logger.warn("Neo4j retrieval failed, building graph from memory")
                    buildGraphFromEntities(entities)
                }
            }.await()
            
            logger.info("Multi-pass workflow completed successfully")
            
            ProcessingResult(
                ontology = ontology,
                graph = graph,
                status = if (validationResult.isValid) "success" else "warning",
                message = if (validationResult.isValid) 
                    "Document processed successfully" 
                else 
                    "Document processed with warnings: ${validationResult.warnings.joinToString()}"
            )
        } catch (e: Exception) {
            logger.error("Error in document processing workflow", e)
            throw RuntimeException("Document processing failed: ${e.message}", e)
        }
    }
    
    private fun buildGraphFromEntities(entities: List<Entity>): GraphData {
        val nodes = entities.map { entity ->
            NodeData(
                id = entity.name,
                label = entity.name,
                type = entity.type,
                evidence = entity.evidence
            )
        }
        
        val edges = entities.flatMap { entity ->
            entity.relations.map { relation ->
                EdgeData(
                    source = entity.name,
                    target = relation.targetEntityName,
                    label = relation.relationType,
                    evidence = relation.evidence
                )
            }
        }
        
        return GraphData(nodes, edges)
    }
}
