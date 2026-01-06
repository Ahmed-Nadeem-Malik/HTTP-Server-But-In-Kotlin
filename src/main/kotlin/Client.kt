package org.example

import java.net.Socket

fun main() {
    val socket = Socket("localhost", 8000)

    val reader = socket.getInputStream().bufferedReader()
    val writer = socket.getOutputStream().bufferedWriter()

    writer.write("Get / HTTP/1.1 200\r\n")
    writer.write("\r\n")
    writer.flush()

    val returnMessage = reader.readLine()
    println(returnMessage)

    socket.close()
}
