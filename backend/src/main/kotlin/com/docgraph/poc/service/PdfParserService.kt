package com.docgraph.poc.service

import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.ParseContext
import org.apache.tika.sax.BodyContentHandler
import org.springframework.stereotype.Service
import java.io.InputStream
import org.slf4j.LoggerFactory

data class ParsedDocument(
    val content: String,
    val pageContents: List<PageContent>,
    val metadata: Map<String, String>
)

data class PageContent(
    val pageNumber: Int,
    val text: String
)

@Service
class PdfParserService {
    private val logger = LoggerFactory.getLogger(PdfParserService::class.java)

    fun parsePdf(inputStream: InputStream): ParsedDocument {
        logger.info("Starting PDF parsing with Apache Tika")
        
        val handler = BodyContentHandler(-1) // No limit
        val metadata = Metadata()
        val parser = AutoDetectParser()
        val context = ParseContext()
        
        try {
            parser.parse(inputStream, handler, metadata, context)
            
            val fullContent = handler.toString()
            val pageContents = extractPages(fullContent)
            
            val metadataMap = metadata.names().associate { name ->
                name to (metadata.get(name) ?: "")
            }
            
            logger.info("PDF parsing completed. Pages: ${pageContents.size}")
            
            return ParsedDocument(
                content = fullContent,
                pageContents = pageContents,
                metadata = metadataMap
            )
        } catch (e: Exception) {
            logger.error("Error parsing PDF", e)
            throw RuntimeException("Failed to parse PDF: ${e.message}", e)
        }
    }
    
    private fun extractPages(content: String): List<PageContent> {
        // Simple page extraction - split by form feed or chunks
        val pageSize = 2000 // Approximate characters per page
        val pages = mutableListOf<PageContent>()
        
        var pageNumber = 1
        var startIndex = 0
        
        while (startIndex < content.length) {
            val endIndex = minOf(startIndex + pageSize, content.length)
            val pageText = content.substring(startIndex, endIndex)
            pages.add(PageContent(pageNumber, pageText))
            pageNumber++
            startIndex = endIndex
        }
        
        return pages
    }
}
