package functional.rx

import io.reactivex.Maybe

fun <A : Any, B : Any> Maybe<(A) -> B>.ap(av: Maybe<A>): Maybe<B> =
    flatMap { f -> av.map { v -> f(v) } }

fun <A : Any, B : Any> Maybe<A>.apRight(r: Maybe<B>): Maybe<B> =
    flatMap { _ -> r }

fun <A : Any, B : Any> Maybe<A>.apLeft(r: Maybe<B>): Maybe<A> =
    flatMap { x -> r.map { x } }

fun <A : Any, B : Any, C : Any> ((A) -> Maybe<B>).fish(
    f: (B) -> Maybe<C>
): (A) -> Maybe<C> =
    { x -> invoke(x).flatMap(f) }
