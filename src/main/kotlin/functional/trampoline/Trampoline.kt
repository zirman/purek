package functional.trampoline

sealed class Trampoline<A, B> {
    fun <C> flatMap(f: (B) -> Trampoline<B, C>): Trampoline<B, C> = FlatMap(this, f)
    fun <C> fmap(f: (B) -> C): Trampoline<B, C> = flatMap { x -> Pure<B, C>(f(x)) }
}

data class Pure<A, B>(val x: B) : Trampoline<A, B>()
data class Suspend<A, B>(val resume: () -> B) : Trampoline<A, B>()

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

fun main(foo: Array<String>): Unit {
    println(run(Pure<Nothing, Int>(1).flatMap<String> { Suspend { readLine()!! } }))
}
