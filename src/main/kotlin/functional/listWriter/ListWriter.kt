@file:Suppress("unused")

package functional.listWriter

import functional.diffList.DiffList
import functional.diffList.append
import functional.diffList.emptyDiffList
import functional.diffList.unitDiffList

data class ListWriter<W, out A>(val ws: DiffList<W>, val x: A)

// functor function for ListWriter

inline fun <W, A, B> ListWriter<W, A>.fmap(crossinline f: (A) -> B): ListWriter<W, B> =
    ListWriter(ws, f(x))

// applicative functions for ListWriter

fun <W, A, B> ListWriter<W, (A) -> B>.ap(av: ListWriter<W, A>): ListWriter<W, B> =
    ListWriter(ws.append(av.ws), x(av.x))

fun <W, A, B> ListWriter<W, A>.apRight(r: ListWriter<W, B>): ListWriter<W, B> =
    { _: A -> { x: B -> x } }.unitListWriter<W, (A) -> (B) -> B>().ap(this).ap(r)

fun <W, A, B> ListWriter<W, A>.apLeft(r: ListWriter<W, B>): ListWriter<W, A> =
    { x: A -> { _: B -> x } }.unitListWriter<W, (A) -> (B) -> A>().ap(this).ap(r)

// monad functions for ListWriter

fun <W, A> A.unitListWriter(): ListWriter<W, A> = ListWriter(emptyDiffList(), this)

fun <W, A> ListWriter<W, ListWriter<W, A>>.join(): ListWriter<W, A> =
    ListWriter(ws.append(x.ws), x.x)

inline fun <W, A, B> ListWriter<W, A>.bind(
    crossinline f: (A) -> ListWriter<W, B>
): ListWriter<W, B> {
    val (ws2, y) = f(x)
    return ListWriter(ws.append(ws2), y)
}

inline fun <W, A, B, C> ((A) -> ListWriter<W, B>).fish(
    crossinline f: (B) -> ListWriter<W, C>
): (A) -> ListWriter<W, C> =
    { w -> invoke(w).bind(f) }

fun <W, A> A.write(w: W): ListWriter<W, A> = ListWriter(w.unitDiffList(), this)
