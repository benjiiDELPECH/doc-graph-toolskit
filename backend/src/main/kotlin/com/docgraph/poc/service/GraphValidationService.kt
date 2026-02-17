package com.docgraph.poc.service

import com.docgraph.poc.model.*
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

@Service
class GraphValidationService {
    private val logger = LoggerFactory.getLogger(GraphValidationService::class.java)

    fun validateGraph(entities: List<Entity>, ontology: Ontology): ValidationResult {
        logger.info("Validating extracted graph")
        
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        // Validate entity types
        entities.forEach { entity ->
            if (!ontology.entityTypes.contains(entity.type)) {
                errors.add("Invalid entity type: ${entity.type} for entity ${entity.name}")
            }
        }
        
        // Validate relations
        entities.forEach { entity ->
            entity.relations.forEach { relation ->
                if (!ontology.relationTypes.contains(relation.relationType)) {
                    warnings.add("Unknown relation type: ${relation.relationType}")
                }
                
                // Check if target entity exists
                val targetExists = entities.any { it.name == relation.targetEntityName }
                if (!targetExists) {
                    warnings.add("Relation target not found: ${relation.targetEntityName}")
                }
            }
        }
        
        // Check for duplicate entities
        val entityNames = entities.map { it.name }
        val duplicates = entityNames.groupingBy { it }.eachCount().filter { it.value > 1 }
        if (duplicates.isNotEmpty()) {
            warnings.add("Duplicate entities found: ${duplicates.keys.joinToString()}")
        }
        
        val isValid = errors.isEmpty()
        logger.info("Validation completed. Valid: $isValid, Errors: ${errors.size}, Warnings: ${warnings.size}")
        
        return ValidationResult(isValid, errors, warnings)
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String>,
    val warnings: List<String>
)
