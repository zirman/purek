package functional.rx

import io.reactivex.Single

fun <A : Any, B : Any> Single<(A) -> B>.ap(av: Single<A>): Single<B> =
    flatMap { f -> av.map { v -> f(v) } }

fun <A : Any, B : Any> Single<A>.apRight(r: Single<B>): Single<B> =
    flatMap { _ -> r }

fun <A : Any, B : Any> Single<A>.apLeft(r: Single<B>): Single<A> =
    flatMap { x -> r.map { x } }

fun <A : Any, B : Any, C : Any> ((A) -> Single<B>).fish(
    f: (B) -> Single<C>
): (A) -> Single<C> =
    { x -> invoke(x).flatMap(f) }
