package org.example

import java.net.ServerSocket

fun main() {
    val server = ServerSocket(8000)
    val client = server.accept()

    val reader = client.getInputStream().bufferedReader()
    val writer = client.getOutputStream().bufferedWriter()

    val message = reader.readLine()

    writer.write(message)
    writer.newLine()
    writer.flush()

    server.close()
    client.close()
}
