package com.docgraph.poc.model

import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship

@Node
data class Entity(
    @Id @GeneratedValue
    val id: Long? = null,
    val name: String,
    val type: String,
    val evidence: Evidence,
    @Relationship(type = "RELATES_TO", direction = Relationship.Direction.OUTGOING)
    val relations: Set<EntityRelation> = emptySet()
)

data class Evidence(
    val page: Int,
    val snippet: String
)

data class EntityRelation(
    val targetEntityName: String,
    val relationType: String,
    val evidence: Evidence
)

data class GraphData(
    val nodes: List<NodeData>,
    val edges: List<EdgeData>
)

data class NodeData(
    val id: String,
    val label: String,
    val type: String,
    val evidence: Evidence
)

data class EdgeData(
    val source: String,
    val target: String,
    val label: String,
    val evidence: Evidence
)

data class Ontology(
    val entityTypes: List<String>,
    val relationTypes: List<String>,
    val schema: Map<String, Any>
)

data class ProcessingResult(
    val ontology: Ontology,
    val graph: GraphData,
    val status: String,
    val message: String
)
