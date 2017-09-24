@file:Suppress("unused")

package functional

sealed class Exp<out T> {
    fun <A> fold(
        lit: (Boolean) -> A,
        and: (A, A) -> A,
        not: (A) -> A,
        or: (A, A) -> A,
        lookup: (T) -> A
    ): A {
        fun go(x: Exp<T>): A = when (x) {
            is Lit -> lit(x.b)
            is And -> and(go(x.a), go(x.b))
            is Not -> not(go(x.e))
            is Or -> or(go(x.a), go(x.b))
            is Var -> lookup(x.v)
        }

        return go(this)
    }
}

class Lit<out T>(val b: Boolean) : Exp<T>()
class And<out T>(val a: Exp<T>, val b: Exp<T>) : Exp<T>()
class Not<out T>(val e: Exp<T>) : Exp<T>()
class Or<out T>(val a: Exp<T>, val b: Exp<T>) : Exp<T>()
class Var<out T>(val v: T) : Exp<T>()

fun <A> evaluate(e: Exp<A>, env: (A) -> Boolean): Boolean =
    e.fold({ x -> x }, { a, b -> a && b }, { a -> !a }, { a, b -> a || b }, env)

fun <A, B> replace(e: Exp<A>, env: (A) -> Exp<B>): Exp<B> =
    e.fold({ a -> Lit(a) }, { a, b -> And(a, b) }, { a -> Not(a) }, { a, b -> Or(a, b) }, env)
