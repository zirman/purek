@file:Suppress("unused")

package functional.list

import functional.experimental.Functor

// list applicative functions

fun <A> A.toList(): List<A> = listOf(this)

fun <A, B> List<(A) -> B>.ap(av: List<A>): List<B> = flatMap { f -> av.map(f) }

fun <A, B> List<A>.apRight(r: List<B>): List<B> = flatMap { _ -> r }

fun <A, B> List<A>.apLeft(r: List<B>): List<A> = flatMap { x -> r.map { x } }

// list monad functions

fun <A, B, C> ((A) -> List<B>).fish(f: (B) -> List<C>): (A) -> List<C> =
    { x -> invoke(x).flatMap(f) }

data class FunctorList<out T>(private val list: List<T>) : Functor<FunctorList<*>, T>, List<T> by list {
    override fun <B> fmap(f: (T) -> B): FunctorList<B> = functorList(list.map(f))
    override fun <B> actual() = this as FunctorList<B>
}

fun <T> functorList(xs: List<T>): FunctorList<T> = FunctorList(xs)

//data class MonadList<out T>(private val list: List<T>) : Monad<MonadList<*>, T>, List<T> by list {
//    override fun <R> bind(f: Binder<MonadList<*>, T, R>): MonadList<R> =
//        MonadList(list.flatMap { f(DoList, it) as MonadList })
//
//    override val actual = this
//}
//
//object DoList : Do<MonadList<*>> {
//    override fun <T> returns(t: T) = monadListOf(t)
//    override val returnUnit: Monad<MonadList<*>, Unit> get() = monadListOf(Unit)
//    override val returnNothing: Monad<MonadList<*>, Nothing> get() = monadListOf()
//}
//
//fun <A> monadListOf(vararg xs: A): MonadList<A> = MonadList(listOf(*xs))
