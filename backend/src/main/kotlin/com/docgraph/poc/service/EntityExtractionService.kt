package com.docgraph.poc.service

import com.docgraph.poc.model.*
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

@Service
class EntityExtractionService {
    private val logger = LoggerFactory.getLogger(EntityExtractionService::class.java)

    fun extractEntitiesAndRelations(
        document: ParsedDocument,
        ontology: Ontology
    ): List<Entity> {
        logger.info("Starting entity and relation extraction")
        
        val entities = mutableListOf<Entity>()
        val entityMap = mutableMapOf<String, Entity>()
        
        // Extract entities from each page
        document.pageContents.forEachIndexed { index, pageContent ->
            val pageEntities = extractEntitiesFromPage(pageContent, ontology)
            pageEntities.forEach { entity ->
                entityMap[entity.name] = entity
            }
        }
        
        // Extract relations between entities
        document.pageContents.forEachIndexed { index, pageContent ->
            val relations = extractRelationsFromPage(pageContent, entityMap.keys.toList(), ontology)
            relations.forEach { (sourceName, relation) ->
                entityMap[sourceName]?.let { sourceEntity ->
                    val updatedRelations = sourceEntity.relations.toMutableSet()
                    updatedRelations.add(relation)
                    entityMap[sourceName] = sourceEntity.copy(relations = updatedRelations)
                }
            }
        }
        
        entities.addAll(entityMap.values)
        logger.info("Extracted ${entities.size} entities with relations")
        
        return entities
    }
    
    private fun extractEntitiesFromPage(
        pageContent: PageContent,
        ontology: Ontology
    ): List<Entity> {
        val entities = mutableListOf<Entity>()
        val text = pageContent.text
        
        // Extract Person entities
        if (ontology.entityTypes.contains("Person")) {
            val personPattern = Regex("\\b([A-Z][a-z]+ [A-Z][a-z]+)\\b")
            personPattern.findAll(text).forEach { match ->
                val name = match.value
                val snippet = extractSnippet(text, match.range.first)
                entities.add(Entity(
                    name = name,
                    type = "Person",
                    evidence = Evidence(pageContent.pageNumber, snippet)
                ))
            }
        }
        
        // Extract Organization entities
        if (ontology.entityTypes.contains("Organization")) {
            val orgPattern = Regex("\\b([A-Z][a-zA-Z]+ (?:Inc|Corp|LLC|Ltd|Company))\\b")
            orgPattern.findAll(text).forEach { match ->
                val name = match.value
                val snippet = extractSnippet(text, match.range.first)
                entities.add(Entity(
                    name = name,
                    type = "Organization",
                    evidence = Evidence(pageContent.pageNumber, snippet)
                ))
            }
        }
        
        // Extract Location entities
        if (ontology.entityTypes.contains("Location")) {
            val locPattern = Regex("\\b([A-Z][a-z]+, [A-Z]{2})\\b")
            locPattern.findAll(text).forEach { match ->
                val name = match.value
                val snippet = extractSnippet(text, match.range.first)
                entities.add(Entity(
                    name = name,
                    type = "Location",
                    evidence = Evidence(pageContent.pageNumber, snippet)
                ))
            }
        }
        
        // Extract key concepts (capitalized terms)
        if (ontology.entityTypes.contains("Concept") || ontology.entityTypes.contains("Term")) {
            val conceptPattern = Regex("\\b([A-Z][a-z]{3,}(?:\\s+[A-Z][a-z]{3,})?)\\b")
            conceptPattern.findAll(text).take(10).forEach { match ->
                val name = match.value
                if (!entities.any { it.name == name }) {
                    val snippet = extractSnippet(text, match.range.first)
                    entities.add(Entity(
                        name = name,
                        type = "Concept",
                        evidence = Evidence(pageContent.pageNumber, snippet)
                    ))
                }
            }
        }
        
        return entities
    }
    
    private fun extractRelationsFromPage(
        pageContent: PageContent,
        entityNames: List<String>,
        ontology: Ontology
    ): List<Pair<String, EntityRelation>> {
        val relations = mutableListOf<Pair<String, EntityRelation>>()
        val text = pageContent.text
        
        // Find co-occurrences of entities (simple relation detection)
        for (i in entityNames.indices) {
            for (j in i + 1 until entityNames.size) {
                val entity1 = entityNames[i]
                val entity2 = entityNames[j]
                
                // Check if both entities appear in the same page
                if (text.contains(entity1) && text.contains(entity2)) {
                    val index1 = text.indexOf(entity1)
                    val index2 = text.indexOf(entity2)
                    
                    // If they're close to each other (within 200 characters)
                    if (Math.abs(index1 - index2) < 200) {
                        val snippet = extractSnippet(text, minOf(index1, index2))
                        
                        relations.add(entity1 to EntityRelation(
                            targetEntityName = entity2,
                            relationType = "relates-to",
                            evidence = Evidence(pageContent.pageNumber, snippet)
                        ))
                    }
                }
            }
        }
        
        return relations
    }
    
    private fun extractSnippet(text: String, index: Int, contextSize: Int = 100): String {
        val start = maxOf(0, index - contextSize)
        val end = minOf(text.length, index + contextSize)
        return "..." + text.substring(start, end).trim() + "..."
    }
}
