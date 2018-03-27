package com.robertchrzanowski.main

import functional.concurrent.flatMap
import functional.concurrent.flatMapIO
import functional.concurrent.map
import functional.concurrent.toConcurrentIO
import functional.concurrent.whileRight
import functional.either.EitherS
import functional.either.RightS
import functional.io.IO
import functional.io.IOServerSocket
import functional.io.IOSocket
import functional.io.flatMap
import functional.io.ioAccept
import functional.io.ioClose
import functional.io.ioPrintLn
import functional.io.ioRead
import functional.io.ioServerSocket
import functional.io.ioWrite
import functional.io.map
import kotlinx.coroutines.experimental.newSingleThreadContext

fun main(args: Array<String>) {
    ioServerSocket(4000)
        .toConcurrentIO()
        .flatMap { serverSocket -> whileRight(Unit, socketListener2(serverSocket)) }
        .flatMapIO(::socketEcho2)
        .map(Int::toString)
        .flatMapIO(::ioPrintLn)
        .run(newSingleThreadContext("node.js concurrency"))

//        .eval(newFixedThreadPoolContext(
//            Runtime.getRuntime().availableProcessors(),
//            "multicore parallelism"))


//        .toConcurrentIO()
//        .flatMap { serverSocket -> whileRight(Unit, socketListener2(serverSocket)) }
//        .flatMapIO(::socketEcho2)
//        .map(Int::toString)
//        .flatMapIO(::ioPrintLn)
//        .eval(newSingleThreadContext("node.js concurrency"))

//    concurrent(CommonPool) {
//        val listenSocket = ioServerSocket(4000).listen()
//        val inSocket = listenSocket.bind()
//
//        while (true) {
//            val str = inSocket.read().bind()
//
//            listenSocket.bind { out -> out.write(str) `}
//        }
//    }
}

//fun limitedSocketListener(
//    serverSocket: IOServerSocket
//): (Int) -> IO<EitherS<Unit, Pair<Int, IOSocket>>> =
//    { n ->
//        if (n <= 0) leftS<Unit, Pair<Int, IOSocket>>(Unit).toIO()
//        else ioAccept(serverSocket)
//            .map { socket -> rightS<Unit, Pair<Int, IOSocket>>(Pair(n - 1, socket)) }
//    }

fun socketListener2(
    serverSocket: IOServerSocket
): (Unit) -> IO<EitherS<Unit, Pair<Unit, IOSocket>>> =
    { _ ->
        ioAccept(serverSocket)
            .map { socket -> RightS(Pair(Unit, socket)) }
    }

private fun socketEcho2(socket: IOSocket): IO<Int> =
    ioRead(socket)
        .flatMap { string -> ioWrite(socket, string) }
        .flatMap { ioClose(socket) }
        .map { 0 }
