@file:Suppress("unused")

package functional.io

typealias IO<A> = () -> A

// functor function for IO

inline fun <A, B> IO<A>.fmap(crossinline f: (A) -> B): IO<B> = { f(this()) }

// applicative functions for IO

fun <A> A.lift(): IO<A> = { this }

inline fun <A, B> IO<(A) -> B>.ap(crossinline av: IO<A>): IO<B> = { this()(av()) }

// monad functions for IO

fun <A> IO<IO<A>>.join(): IO<A> = { this()() }

inline fun <A, B> IO<A>.bind(crossinline f: (A) -> IO<B>): IO<B> = { f(this())() }

//fun <A, B> IO<A>.bind2(f: (A) -> IO<B>): IO<B> = f.lift().ap(this).join()

//inline fun <A, B> IO<A>.bind3(crossinline f: (A) -> IO<B>): IO<B> = fmap(f).join()

// side effect functions

fun printlnIO(s: String): IO<Unit> = { println(s) }

fun readLineIO(): IO<String?> = { readLine() }

fun <A> IO<A>.repeat(): IO<Unit> = { while (true) this() }

// Recursively binds IO monad passing an updated value.
// This will eventually run out of stack space because
// there's no tail call optimization. :0(

fun <A> IO<A>.repeatUnsafe(): IO<A> = bind { repeatUnsafe() }
