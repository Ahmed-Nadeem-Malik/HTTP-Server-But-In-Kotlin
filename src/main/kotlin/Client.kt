package org.example

import java.net.Socket

fun main() {
    val socket = Socket("localhost", 8000)

    val reader = socket.getInputStream().bufferedReader()

    val returnMessage = reader.readLine()
    println(returnMessage)

    socket.close()
}
