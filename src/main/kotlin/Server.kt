package org.example

import java.io.BufferedReader
import java.io.BufferedWriter
import java.net.ServerSocket
import java.net.Socket

fun main() {
    val server = ServerSocket(8000)
    val routes = initRoutes()

    while (true) {
        handleClient(server.accept(), routes)
    }
}

fun handleClient(client: Socket, routes: Map<Pair<String, String>, Handler>) {
    client.use { socket ->
        val writer = socket.getOutputStream().bufferedWriter()
        val reader = socket.getInputStream().bufferedReader()

        val request = parsing(reader)
        val route = request.method.uppercase() to request.path

        val handler = routes[route]

        if (handler != null) {
            handler(request, writer)
        } else {
            sendResponse(writer, 404, "text/html", "<h1>404 Not Found</h1>")
        }
    }
}

fun sendResponse(
    writer: BufferedWriter,
    status: Int = 200,
    contentType: String = "text/html",
    body: String
) {
    val statusText = mapOf(
        200 to "OK",
        201 to "Created",
        404 to "Not Found",
        500 to "Internal Server Error"
    )[status] ?: "Unknown"

    writer.write("HTTP/1.1 $status $statusText\r\n")
    writer.write("Content-Type: $contentType\r\n")
    writer.write("Content-Length: ${body.toByteArray().size}\r\n")
    writer.write("\r\n")
    writer.write(body)
    writer.flush()
}

fun parsing(reader: BufferedReader): HTTP {
    val requestLine = reader.readLine().split(" ")
    val method = requestLine[0]
    val path = requestLine[1]

    val headers: MutableMap<String, String> = mutableMapOf()
    var headerLine = reader.readLine()
    while (headerLine.isNotBlank()) {
        val colon = headerLine.indexOf(":")
        val key = headerLine.substring(0, colon).trim()
        val value = headerLine.substring(colon + 1).trim()
        headers[key] = value
        headerLine = reader.readLine()
    }

    val body = headers["Content-Length"]?.toIntOrNull()?.let { length ->
        val buffer = CharArray(length)
        reader.read(buffer, 0, length)
        String(buffer)
    } ?: ""

    return HTTP(method, path, headers, body)
}

data class HTTP(
    val method: String,
    val path: String,
    val headers: MutableMap<String, String>,
    val body: String
)

// Handler takes request and writer
typealias Handler = (HTTP, BufferedWriter) -> Unit

fun initRoutes(): Map<Pair<String, String>, Handler> {
    val routes = mutableMapOf<Pair<String, String>, Handler>()

    // GET /
    routes["GET" to "/"] = { _, writer ->
        val html = loadResource("/index.html") ?: "<h1>Not found</h1>"
        sendResponse(writer, 200, "text/html", html)
    }

    // GET /about
    routes["GET" to "/about"] = { _, writer ->
        sendResponse(writer, 200, "text/html", "<h1>About page</h1>")
    }

    // POST /users
    routes["POST" to "/users"] = { request, writer ->
        val body = request.body
        sendResponse(writer, 201, "application/json", """{"received": "$body"}""")
    }

    // Ignore favicon requests
    routes["GET" to "/favicon.ico"] = { _, writer ->
        sendResponse(writer, 204, "text/plain", "")
    }

    return routes
}

fun loadResource(path: String): String? {
    return object {}.javaClass.getResource(path)?.readText()
}
