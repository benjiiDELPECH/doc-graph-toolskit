package com.docgraph.poc.controller

import com.docgraph.poc.model.*
import com.docgraph.poc.service.DocumentProcessingService
import com.docgraph.poc.service.Neo4jGraphService
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.slf4j.LoggerFactory

@RestController
@RequestMapping("/api")
class DocumentController(
    private val documentProcessingService: DocumentProcessingService,
    private val neo4jGraphService: Neo4jGraphService
) {
    private val logger = LoggerFactory.getLogger(DocumentController::class.java)

    @PostMapping("/process", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun processDocument(@RequestParam("file") file: MultipartFile): ResponseEntity<ProcessingResult> {
        logger.info("Received document upload: ${file.originalFilename}")
        
        if (file.isEmpty) {
            return ResponseEntity.badRequest().body(
                ProcessingResult(
                    ontology = Ontology(emptyList(), emptyList(), emptyMap()),
                    graph = GraphData(emptyList(), emptyList()),
                    status = "error",
                    message = "File is empty"
                )
            )
        }
        
        return try {
            val result = runBlocking {
                documentProcessingService.processDocument(file.inputStream)
            }
            ResponseEntity.ok(result)
        } catch (e: Exception) {
            logger.error("Error processing document", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ProcessingResult(
                    ontology = Ontology(emptyList(), emptyList(), emptyMap()),
                    graph = GraphData(emptyList(), emptyList()),
                    status = "error",
                    message = "Processing failed: ${e.message}"
                )
            )
        }
    }
    
    @GetMapping("/graph")
    fun getGraph(): ResponseEntity<GraphData> {
        logger.info("Retrieving graph")
        
        return try {
            val graph = neo4jGraphService.retrieveGraph()
            ResponseEntity.ok(graph)
        } catch (e: Exception) {
            logger.error("Error retrieving graph", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                GraphData(emptyList(), emptyList())
            )
        }
    }
    
    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf(
            "status" to "UP",
            "service" to "docgraph-backend"
        ))
    }
}
