package functional.rx

import io.reactivex.Observable

fun <A : Any, B : Any> Observable<(A) -> B>.ap(av: Observable<A>): Observable<B> =
    flatMap { f -> av.map { v -> f(v) } }

fun <A : Any, B : Any> Observable<A>.apRight(r: Observable<B>): Observable<B> =
    flatMap { _ -> r }

fun <A : Any, B : Any> Observable<A>.apLeft(r: Observable<B>): Observable<A> =
    flatMap { x -> r.map { x } }

fun <A : Any, B : Any, C : Any> ((A) -> Observable<B>).fish(
    f: (B) -> Observable<C>
): (A) -> Observable<C> =
    { x -> invoke(x).flatMap(f) }
