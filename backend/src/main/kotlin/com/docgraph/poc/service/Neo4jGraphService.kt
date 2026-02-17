package com.docgraph.poc.service

import com.docgraph.poc.model.*
import com.docgraph.poc.repository.EntityRepository
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

@Service
class Neo4jGraphService(
    private val entityRepository: EntityRepository
) {
    private val logger = LoggerFactory.getLogger(Neo4jGraphService::class.java)

    fun storeGraph(entities: List<Entity>) {
        logger.info("Storing graph in Neo4j: ${entities.size} entities")
        
        try {
            // Clear existing data (optional - for demo purposes)
            // entityRepository.deleteAll()
            
            // Save all entities
            entityRepository.saveAll(entities)
            
            logger.info("Graph stored successfully")
        } catch (e: Exception) {
            logger.error("Error storing graph in Neo4j", e)
            throw RuntimeException("Failed to store graph: ${e.message}", e)
        }
    }
    
    fun retrieveGraph(): GraphData {
        logger.info("Retrieving graph from Neo4j")
        
        try {
            val entities = entityRepository.findAll()
            
            // Convert to graph format
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
            
            logger.info("Retrieved ${nodes.size} nodes and ${edges.size} edges")
            
            return GraphData(nodes, edges)
        } catch (e: Exception) {
            logger.error("Error retrieving graph from Neo4j", e)
            // Return empty graph if Neo4j is not available
            return GraphData(emptyList(), emptyList())
        }
    }
}
