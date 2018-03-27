@file:Suppress("unused")

package functional.io

import functional.either.Either
import functional.either.with
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousServerSocketChannel
import kotlinx.coroutines.experimental.nio.aAccept
import kotlinx.coroutines.experimental.nio.aRead
import kotlinx.coroutines.experimental.nio.aWrite
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.nio.charset.Charset
import kotlin.coroutines.experimental.CoroutineContext

sealed class IO<out A> {
    fun run(cc: CoroutineContext): Deferred<A> = async(cc) { run() }
    internal abstract suspend fun run(): A
}

private class Map<A, out B>(private val io: IO<A>, private val f: (A) -> B) : IO<B>() {
    override suspend fun run(): B = f(io.run())
}

private class ToIO<out A>(private val v: A) : IO<A>() {
    override suspend fun run(): A = v
}

private class Ap<A, out B>(private val af: IO<(A) -> B>, private val av: IO<A>) : IO<B>() {
    override suspend fun run(): B = af.run()(av.run())
}

private class ApRight<out A, out B>(private val l: IO<A>, private val r: IO<B>) : IO<B>() {
    override suspend fun run(): B {
        l.run()
        return r.run()
    }
}

private class ApLeft<out A, out B>(private val l: IO<A>, private val r: IO<B>) : IO<A>() {
    override suspend fun run(): A {
        val x = l.run()
        r.run()
        return x
    }
}

private class Join<out A>(private val io: IO<IO<A>>) : IO<A>() {
    override suspend fun run(): A = io.run().run()
}

private class FlatMap<A, out B>(private val io: IO<A>, private val f: (A) -> IO<B>) : IO<B>() {
    override suspend fun run(): B = f(io.run()).run()
}

private class PrintLn(private val s: String) : IO<Unit>() {
    override suspend fun run(): Unit = println(s)
}

private object IOReadLine : IO<String?>() {
    override suspend fun run(): String? = readLine()
}

private class ServerSocketChannel(private val port: Int) : IO<IOServerSocket>() {
    override suspend fun run(): IOServerSocket =
        IOServerSocket(AsynchronousServerSocketChannel.open().bind(InetSocketAddress(port), 10))
}

private class Accept(private val socket: AsynchronousServerSocketChannel) : IO<IOSocket>() {
    override suspend fun run(): IOSocket = IOSocket(socket.aAccept())
}

private class Read(private val socket: AsynchronousSocketChannel) : IO<String>() {
    override suspend fun run(): String =
        ByteBuffer.allocate(1024).let { byteBuffer ->
            val nBytes = socket.aRead(byteBuffer)
            val byteArray = ByteArray(nBytes)

            for (i in 0 until nBytes) {
                byteArray[i] = byteBuffer[i]
            }

//            byteBuffer.get(byteArray)
            byteArray.toString(Charset.forName("UTF-8"))
        }
}

private class Write(
    private val s: AsynchronousSocketChannel,
    private val byteBuffer: ByteBuffer
) : IO<Int>() {
    override suspend fun run(): Int = s.aWrite(byteBuffer)
}

private class Close(
    private val s: AsynchronousSocketChannel
) : IO<Unit>() {
    override suspend fun run() = s.close()
}

private class Repeat<A>(private val f: (A) -> IO<A>, private val i: A) : IO<Nothing>() {
    override suspend fun run(): Nothing {
        var v: A = i

        while (true) {
            v
                .toIO()
                .flatMap(f)
                .flatMap { n ->
                    v = n
                    ioUnit
                }
                .run()
        }
    }
}

private class WhileRight<out L, R>(
    private val f: (R) -> IO<Either<L, R>>,
    private val s: R
) : IO<L>() {
    override suspend fun run(): L {
        var b = true
        var r: R = s
        var l: L? = null

        while (b) {
            r
                .toIO()
                .flatMap(f)
                .flatMap { e ->
                    e.with(
                        {
                            l = it
                            b = false
                        },
                        { r = it })

                    ioUnit
                }
                .run()
        }

        return l!!
    }
}

val ioUnit: IO<Unit> = Unit.toIO()

// functor function for IO

fun <A, B> IO<A>.map(f: (A) -> B): IO<B> = Map(this, f)

// applicative functions for IO

fun <A> A.toIO(): IO<A> = ToIO(this)
fun <A, B> IO<(A) -> B>.ap(av: IO<A>): IO<B> = Ap(this, av)
fun <A, B> IO<A>.apRight(r: IO<B>): IO<B> = ApRight(this, r)
fun <A, B> IO<A>.apLeft(r: IO<B>): IO<A> = ApLeft(this, r)

// monad functions for IO

fun <A> IO<IO<A>>.flatten(): IO<A> = Join(this)

fun <A, B> IO<A>.flatMap(f: (A) -> IO<B>): IO<B> = FlatMap(this, f)

fun <A, B, C> ((A) -> IO<B>).fish(f: (B) -> IO<C>): (A) -> IO<C> = { x -> invoke(x).flatMap(f) }

// Side effect functions.

fun ioPrintLn(s: String): IO<Unit> = PrintLn(s)

val ioReadLine: IO<String?> = IOReadLine

data class IOServerSocket(internal val serverSocket: AsynchronousServerSocketChannel)
data class IOSocket(internal val socket: AsynchronousSocketChannel)

fun ioServerSocket(port: Int): IO<IOServerSocket> = ServerSocketChannel(port)

fun ioAccept(serverSocket: IOServerSocket): IO<IOSocket> = Accept(serverSocket.serverSocket)

fun ioRead(socket: IOSocket): IO<String> = Read(socket.socket)

fun ioWrite(socket: IOSocket, string: String): IO<Int> {
    val array = string.toByteArray()
    val buffer = ByteBuffer.allocate(array.size)

    for (i in 0 until array.size) {
        buffer.put(i, array[i])
    }

//    buffer.put(array)
    return Write(socket.socket, buffer)
}

fun ioClose(socket: IOSocket): IO<Unit> = Close(socket.socket)

// Repeatedly binds IO<A> to itself.

fun <A> ((A) -> IO<A>).repeat(i: A): IO<Nothing> = Repeat(this, i)

// Binds IO<Either<L, R>> to itself while Right value is produced.
// Stops binding when Left value is produced.

fun <L, R> ((R) -> IO<Either<L, R>>).whileRight(i: R): IO<L> = WhileRight(this, i)

// Repeatedly binds IO<A> to itself using recursion.
// This will eventually eval out of stack space because
// the JVM does not have tail call optimization. :0(

fun <A> ((A) -> IO<A>).repeatRec(i: A): IO<A> =
    i
        .toIO()
        .flatMap(this)
        .flatMap { v -> repeatRec(v) }

// Binds IO<Either<L, R>> to itself while Right value is produced.
// Stops binding when Left value is produced.
// This may eval out of stack space because
// the JVM does not have tail call optimization. :0(

fun <L, R> ((R) -> IO<Either<L, R>>).whileRightRec(r: R): IO<L> =
    r
        .toIO()
        .flatMap(this)
        .flatMap { e ->
            e.with(
                { it.toIO() },
                { whileRightRec(it) })
        }

// monoid operation on IO

fun <A, B, C> IO<A>.merge(io: IO<B>, f: (A) -> (B) -> IO<C>): IO<C> =
    flatMap { a -> io.flatMap { b -> f(a)(b) } }

fun <A> IO<A>.merge(io: IO<A>, f: (A, A) -> IO<A>): IO<A> =
    flatMap { a -> io.flatMap { b -> f(a, b) } }
