@file:Suppress("unused")

package functional.listWriter

import functional.diffList.DiffList
import functional.diffList.append
import functional.diffList.toDiffList
import functional.diffList.emptyDiffList

data class ListWriter<out X, W>(val x: X, val ws: DiffList<W>)

// functor function for ListWriter

inline fun <X, Y, W> ListWriter<X, W>.fmap(crossinline f: (X) -> Y): ListWriter<Y, W> =
    ListWriter(f(x), ws)

// applicative functions for ListWriter

fun <X, Y, W> ListWriter<(X) -> Y, W>.ap(av: ListWriter<X, W>): ListWriter<Y, W> =
    ListWriter(x(av.x), ws.append(av.ws))

// monad functions for ListWriter

fun <X, W> X.toListWriter(): ListWriter<X, W> = ListWriter(this, emptyDiffList())

fun <X, W> ListWriter<ListWriter<X, W>, W>.join(): ListWriter<X, W> =
    ListWriter(x.x, ws.append(x.ws))

inline fun <X, Y, W> ListWriter<X, W>.bind(
    crossinline f: (X) -> ListWriter<Y, W>
): ListWriter<Y, W> {
    val (y, ws2) = f(x)
    return ListWriter(y, ws.append(ws2))
}

//fun <X, Y, W> ListWriter<X, W>.bind2(f: (X) -> ListWriter<Y, W>): ListWriter<Y, W> =
//    f.toListWriter<(X) -> ListWriter<Y, W>, W>().ap(this).join()

//fun <X, Y, W> ListWriter<X, W>.bind3(f: (X) -> ListWriter<Y, W>): ListWriter<Y, W> = fmap(f).join()

fun <X, W> X.write(w: W): ListWriter<X, W> = ListWriter(this, w.toDiffList())
