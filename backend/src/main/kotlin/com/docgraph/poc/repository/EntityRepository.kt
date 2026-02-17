package com.docgraph.poc.repository

import com.docgraph.poc.model.Entity
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.stereotype.Repository

@Repository
interface EntityRepository : Neo4jRepository<Entity, Long> {
    fun findByName(name: String): Entity?
    fun findByType(type: String): List<Entity>
}
