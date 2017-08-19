package com.outsitenetworks.allpointsrewards.functional.experimental

sealed class Exp<out T : Any> {
    fun <A : Any> fold(
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

class Lit<out T : Any>(val b: Boolean) : Exp<T>()
class And<out T : Any>(val a: Exp<T>, val b: Exp<T>) : Exp<T>()
class Not<out T : Any>(val e: Exp<T>) : Exp<T>()
class Or<out T : Any>(val a: Exp<T>, val b: Exp<T>) : Exp<T>()
class Var<out T : Any>(val v: T) : Exp<T>()

fun <A : Any> evaluate(e: Exp<A>, env: (A) -> Boolean): Boolean =
    e.fold({ x -> x }, { a, b -> a && b }, { a -> !a }, { a, b -> a || b }, env)

fun <A : Any, B : Any> replace(e: Exp<A>, env: (A) -> Exp<B>): Exp<B> =
    e.fold({ a -> Lit(a) }, { a, b -> And(a, b) }, { a -> Not(a) }, { a, b -> Or(a, b) }, env)
