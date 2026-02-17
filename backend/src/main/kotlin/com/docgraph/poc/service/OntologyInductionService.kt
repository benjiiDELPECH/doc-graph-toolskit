package com.docgraph.poc.service

import com.docgraph.poc.model.Ontology
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

@Service
class OntologyInductionService {
    private val logger = LoggerFactory.getLogger(OntologyInductionService::class.java)

    fun induceOntology(document: ParsedDocument): Ontology {
        logger.info("Starting dynamic ontology induction")
        
        // Analyze document to identify entity and relation types
        val entityTypes = identifyEntityTypes(document.content)
        val relationTypes = identifyRelationTypes(document.content)
        
        val schema = mapOf(
            "domain" to "document-analysis",
            "version" to "1.0",
            "entities" to entityTypes.map { mapOf("type" to it, "properties" to listOf("name", "evidence")) },
            "relations" to relationTypes.map { mapOf("type" to it, "properties" to listOf("evidence")) }
        )
        
        logger.info("Ontology induced: ${entityTypes.size} entity types, ${relationTypes.size} relation types")
        
        return Ontology(
            entityTypes = entityTypes,
            relationTypes = relationTypes,
            schema = schema
        )
    }
    
    private fun identifyEntityTypes(content: String): List<String> {
        // Simple heuristic-based entity type identification
        val types = mutableSetOf<String>()
        
        // Look for common patterns
        if (content.contains(Regex("\\b[A-Z][a-z]+ [A-Z][a-z]+\\b"))) {
            types.add("Person")
        }
        if (content.contains(Regex("\\b[A-Z][a-zA-Z]+ (Inc|Corp|LLC|Ltd)\\b"))) {
            types.add("Organization")
        }
        if (content.contains(Regex("\\b[A-Z][a-z]+, [A-Z]{2}\\b"))) {
            types.add("Location")
        }
        if (content.contains(Regex("\\b\\d{4}\\b"))) {
            types.add("Date")
        }
        if (content.contains(Regex("\\b[A-Z][a-z]+ \\d+\\b"))) {
            types.add("Event")
        }
        
        // Default types
        types.add("Concept")
        types.add("Term")
        
        return types.toList()
    }
    
    private fun identifyRelationTypes(content: String): List<String> {
        // Common relation types based on document analysis
        return listOf(
            "mentions",
            "relates-to",
            "part-of",
            "associated-with",
            "located-in",
            "works-for",
            "occurs-at"
        )
    }
}
