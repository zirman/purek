package com.outsitenetworks.allpointsrewards.functional

import kotlin.collections.map as collectionsMap

// partial application functions

fun <T, U, P> ((T, U) -> P).partial(t: T): (U) -> P =
    { u -> this(t, u) }

fun <T, U, P, L> ((T, U, P) -> L).partial(t: T, u: U): (P) -> L =
    { p -> this(t, u, p) }

fun <T, U, P, L, E> ((T, U, P, L) -> E).partial(t: T, u: U, p: P): (L) -> E =
    { l -> this(t, u, p, l) }

fun <T, U, P, L, E, S> ((T, U, P, L, E) -> S).partial(t: T, u: U, p: P, l: L): (E) -> S =
    { e -> this(t, u, p, l, e) }

fun <T, U, P, L> ((T, U, P) -> L).partial(t: T): (U, P) -> L =
    { u, p -> this(t, u, p) }

fun <T, U, P, L, E> ((T, U, P, L) -> E).partial(t: T, u: U): (P, L) -> E =
    { p, l -> this(t, u, p, l) }

fun <T, U, P, L, E, S> ((T, U, P, L, E) -> S).partial(t: T, u: U, p: P): (L, E) -> S =
    { l, e -> this(t, u, p, l, e) }

// curry functions

fun <T, U, P> ((T, U) -> P).curry(): (T) -> (U) -> P = { t -> { u -> this(t, u) } }

fun <T, U, P, L> ((T, U, P) -> L).curry(): (T) -> (U) -> (P) -> L =
    { t -> { u -> { p -> this(t, u, p) } } }

fun <T, U, P, L, E> ((T, U, P, L) -> E).curry(): (T) -> (U) -> (P) -> (L) -> E =
    { t -> { u -> { p -> { l -> this(t, u, p, l) } } } }

fun <T, U, P, L, E, S> ((T, U, P, L, E) -> S).curry(): (T) -> (U) -> (P) -> (L) -> (E) -> S =
    { t -> { u -> { p -> { l -> { e -> this(t, u, p, l, e) } } } } }

// compose function

inline fun <A : Any, B : Any, C : Any> ((B) -> C).o(crossinline g: (A) -> B): (A) -> C =
    { x -> this(g(x)) }

// application function

fun <A : Any, B : Any> ((A) -> B).s(x: A): B = this(x)

// pipe function

inline infix fun <T : Any, U : Any> T.then(t: (T) -> U): U = t(this)

// Pair and Triple constructors

infix fun <A : Any, B : Any> A.et(b: B): Pair<A, B> = Pair(this, b)

infix fun <A : Any, B : Any, C : Any> Pair<A, B>.et(c: C): Triple<A, B, C> =
    Triple(first, second, c)

// Iterable extensions

// applicative function for Iterable

fun <A : Any, B : Any> Iterable<(A) -> B>.app(av: Iterable<A>): Iterable<B> =
    collectionsMap { f -> av.collectionsMap { v -> f(v) } }.flatten()

// applicative function for Iterable

inline fun <A : Any, B : Any> Iterable<A>.bind(f: (A) -> Iterable<B>): Iterable<B> =
    collectionsMap(f).flatten()

// memoize functions

fun <A : Any> memoize(f: () -> A): () -> A = lazy(f)::value

inline fun <A : Any, B : Any> memoize(crossinline f: (A) -> B): (A) -> B {
    val map = mutableMapOf<A, B>()
    return { key -> map.getOrPut(key) { f(key) } }
}
