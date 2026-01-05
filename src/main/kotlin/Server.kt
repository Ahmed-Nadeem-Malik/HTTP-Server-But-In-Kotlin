package org.example

import java.io.File
import java.net.ServerSocket

fun main() {
    val server = ServerSocket(8000)
    
    while (true) {
        val client = server.accept()

        val reader = client.getInputStream().bufferedReader()
        val writer = client.getOutputStream().bufferedWriter()

        val htmlFile = File("/Users/ahmed/Coding/Projects/Multithreaded-HTTP-Server-But-In-Kotlin/src/main/resources/index.html")
        val htmlContent = htmlFile.readText()

        writer.write("HTTP/1.1 200 OK\r\n")
        writer.write("Content-Type: text/html\r\n")
        writer.write("Content-Length: ${htmlContent.length}\r\n")
        writer.write("\r\n")
        writer.write(htmlContent)
        writer.flush()

        client.close()
    }
}
