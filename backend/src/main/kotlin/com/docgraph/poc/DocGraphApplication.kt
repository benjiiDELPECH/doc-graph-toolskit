package com.docgraph.poc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DocGraphApplication

fun main(args: Array<String>) {
    runApplication<DocGraphApplication>(*args)
}
