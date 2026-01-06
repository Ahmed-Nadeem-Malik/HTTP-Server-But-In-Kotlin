package org.example

import java.net.ServerSocket
import java.util.concurrent.Executors

fun main() {
    val server = ServerSocket(8080, 1000)
    server.reuseAddress = true
    val routes = initBlockingRoutes()
    val executor = Executors.newVirtualThreadPerTaskExecutor()

    println("Server running on http://localhost:8000 (virtual threads)")

    while (true) {
        val client = server.accept()
        executor.submit { handleClientBlocking(client, routes) }
    }
}
