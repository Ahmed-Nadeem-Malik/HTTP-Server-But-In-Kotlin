package org.example

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

typealias AsyncHandler = suspend (HTTP, ByteWriteChannel) -> Unit

fun main() = runBlocking {
    val selectorManager = SelectorManager(Dispatchers.IO)
    val server = aSocket(selectorManager).tcp().bind("0.0.0.0", 8080)
    val routes = initAsyncRoutes()

    println("Server running on http://localhost:8000 (ktor async sockets)")

    while (true) {
        val socket = server.accept()
        launch {
            handleClientAsync(socket, routes)
        }
    }
}

suspend fun handleClientAsync(socket: Socket, routes: Map<Pair<String, String>, AsyncHandler>) {
    try {
        socket.use {
            val readChannel = socket.openReadChannel()
            val writeChannel = socket.openWriteChannel(autoFlush = false)

            val request = parseRequestAsync(readChannel)
            val route = request.method.uppercase() to request.path

            val handler = routes[route]
            if (handler != null) {
                handler(request, writeChannel)
            } else {
                writeChannel.writeFully(CachedResponses.notFound)
            }
            writeChannel.flush()
        }
    } catch (_: Exception) {
    }
}

suspend fun parseRequestAsync(channel: ByteReadChannel): HTTP {
    val requestLine = channel.readUTF8Line() ?: throw IllegalStateException("Empty")
    val firstSpace = requestLine.indexOf(' ')
    val secondSpace = requestLine.indexOf(' ', firstSpace + 1)
    val method = requestLine.substring(0, firstSpace)
    val path = requestLine.substring(firstSpace + 1, secondSpace)
    val headers = mutableMapOf<String, String>()
    var line = channel.readUTF8Line()
    while (!line.isNullOrEmpty()) {
        val colon = line.indexOf(':')
        headers[line.substring(0, colon)] = line.substring(colon + 2)
        line = channel.readUTF8Line()
    }

    val body = headers["Content-Length"]?.toIntOrNull()?.let { len ->
        val buffer = ByteArray(len)
        channel.readFully(buffer)
        String(buffer)
    } ?: ""

    return HTTP(method, path, headers, body)
}

fun initAsyncRoutes(): Map<Pair<String, String>, AsyncHandler> = mapOf(
    ("GET" to "/") to { _, channel -> channel.writeFully(CachedResponses.index) },
    ("GET" to "/css/style.css") to { _, channel -> channel.writeFully(CachedResponses.css) },
    ("GET" to "/js/script.js") to { _, channel -> channel.writeFully(CachedResponses.js) },
    ("GET" to "/favicon.ico") to { _, channel -> channel.writeFully(CachedResponses.favicon) },
    ("POST" to "/echo") to { request, channel ->
        val response = buildResponse(201, "application/json", """{"received":"${request.body}"}""")
        channel.writeFully(response)
    })
