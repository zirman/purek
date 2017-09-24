@file:Suppress("unused")

package functional.io

import functional.either.Either

sealed class IO<out A> {
    abstract fun run(): A
}

private class Fmap<A, out B>(private val io: IO<A>, private val f: (A) -> B) : IO<B>() {
    override fun run(): B = f(io.run())
}

private class ToIO<out A>(private val v: A) : IO<A>() {
    override fun run(): A = v
}

private class Ap<A, out B>(private val af: IO<(A) -> B>, private val av: IO<A>) : IO<B>() {
    override fun run(): B = af.run()(av.run())
}

private class Join<out A>(private val io: IO<IO<A>>) : IO<A>() {
    override fun run(): A = io.run().run()
}

private class Bind<A, out B>(private val io: IO<A>, private val f: (A) -> IO<B>) : IO<B>() {
    override fun run(): B = f(io.run()).run()
//    override fun run(): B = f.toIO().ap(io).join().run()
//    override fun run(): B = io.fmap(f).join().run()
}

private class PrintLn(private val s: String) : IO<Unit>() {
    override fun run(): Unit = println(s)
}

private object ReadLineIO : IO<String?>() {
    override fun run(): String? = readLine()
}

private class Repeat<A>(private val f: (A) -> IO<A>, private val i: A) : IO<Nothing>() {
    override fun run(): Nothing {
        var v: A = i

        while (true) {
            v
                .toIO()
                .bind(f)
                .bind { n ->
                    v = n
                    ioUnit
                }
                .run()
        }

        @Suppress("UNREACHABLE_CODE")
        throw Exception()
    }
}

private class WhileRight<out L, R>(
    private val f: (R) -> IO<Either<L, R>>,
    private val s: R
) : IO<L>() {
    override fun run(): L {
        var b = true
        var r: R = s
        var l: L? = null

        while (b) {
            r
                .toIO()
                .bind(f)
                .bind { e ->
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

fun <A, B> IO<A>.fmap(f: (A) -> B): IO<B> = Fmap(this, f)

// applicative functions for IO

fun <A> A.toIO(): IO<A> = ToIO(this)

fun <A, B> IO<(A) -> B>.ap(av: IO<A>): IO<B> = Ap(this, av)

// monad functions for IO

fun <A> IO<IO<A>>.join(): IO<A> = Join(this)

fun <A, B> IO<A>.bind(f: (A) -> IO<B>): IO<B> = Bind(this, f)

fun <A, B, C> ((A) -> IO<B>).fish(f: (B) -> IO<C>): (A) -> IO<C> = { x -> this(x).fmap(f).join() }

// Side effect functions.

fun printlnIO(s: String): IO<Unit> = PrintLn(s)

val readLineIO: IO<String?> = ReadLineIO

// Repeatedly binds IO<A> to itself.

fun <A> ((A) -> IO<A>).repeat(i: A): IO<Nothing> = Repeat(this, i)

// Binds IO<Either<L, R>> to itself while Right value is produced.
// Stops binding when Left value is produced.

fun <L, R> ((R) -> IO<Either<L, R>>).whileRight(i: R): IO<L> = WhileRight(this, i)

// Repeatedly binds IO<A> to itself using recursion.
// This will eventually run out of stack space because
// the JVM does not have tail call optimization. :0(

fun <A> ((A) -> IO<A>).repeatRec(i: A): IO<A> = i
    .toIO()
    .bind(this)
    .bind { v -> repeatRec(v) }

// Binds IO<Either<L, R>> to itself while Right value is produced.
// Stops binding when Left value is produced.
// This may run out of stack space because
// the JVM does not have tail call optimization. :0(

fun <L, R> ((R) -> IO<Either<L, R>>).whileRightRec(r: R): IO<L> = r
    .toIO()
    .bind(this)
    .bind { e ->
        e.with(
            { it.toIO() },
            { whileRightRec(it) })
    }
