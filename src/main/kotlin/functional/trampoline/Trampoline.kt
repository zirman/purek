package functional.trampoline

sealed class Trampoline<out A, B> {
    fun <C> flatMap(f: (B) -> Trampoline<B, C>): Trampoline<B, C> = FlatMap(this, f)
    fun <C> fmap(f: (B) -> C): Trampoline<B, C> = flatMap { x -> Pure<B, C>(f(x)) }
}

data class Pure<out A, B>(val x: B) : Trampoline<A, B>()
data class Suspend<out A, B>(val resume: () -> B) : Trampoline<A, B>()

data class FlatMap<A, B>(
    val prev: Trampoline<*, A>,
    val next: (A) -> Trampoline<A, B>
) : Trampoline<A, B>()

tailrec fun <A, B> run(t: Trampoline<A, B>): B {
    return when (t) {
        is Pure -> t.x
        is Suspend -> t.resume()
        is FlatMap<A, B> -> {
            when (t.prev) {
                is Pure -> run(t.next(t.prev.x))
                is Suspend -> run(t.next(t.prev.resume()))
                is FlatMap<*, A> -> {
                    t.prev as FlatMap<A, A>
                    run(t.prev.prev.flatMap { x -> t.prev.next(x).flatMap(t.next) })
                }
            }
        }
    }
}

fun <A> pure(x: A): Pure<Nothing, A> = Pure(x)
fun <A> suspendF(f: () -> A): Suspend<Nothing, A> = Suspend(f)

fun putStrLn(x: String?) = suspendF { println(x) }
val read = suspendF { readLine() }

val prog =
    pure(1)
        .flatMap { n -> pure((n + 1).toString()) }
        .flatMap(::putStrLn)
        .flatMap { read }
        .flatMap(::putStrLn)

fun loop(): Trampoline<Unit, Unit> =
    suspendF { println("Stacks drool, Trampolines RULE!!") }
        .flatMap { loop() }

fun main(foo: Array<String>): Unit {
    println(prog.let(::run))
}
