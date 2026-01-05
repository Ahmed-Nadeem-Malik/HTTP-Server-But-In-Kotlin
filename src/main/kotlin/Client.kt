package org.example

import java.net.Socket

fun main() {
    var count = 50
    val times = ArrayList<Long>()
    while (count > 0) {
        val startTime = System.nanoTime()
        val socket = Socket("learnxinyminutes.com", 80)
        val reader = socket.getInputStream().bufferedReader()
        val writer = socket.getOutputStream().bufferedWriter()


        writer.write("GET /kotlin/ HTTP/1.1\r\n")
        writer.write("Host: learnxinyminutes.com\r\n")
        writer.write("Connection: close\r\n")
        writer.write("\r\n")
        writer.flush()
        val finishTime = System.nanoTime()
        --count
        socket.close()
        val finalTime =finishTime - startTime
        times.add(finalTime)
    }

    times.forEach { println(it) }

    /*

    val writer = socket.getOutputStream().bufferedWriter()
    writer.write("hello world")
    writer.newLine() // submits it to the buffer
    writer.flush() // sends buffer to the server

    socket.close()

     */
}
