@file:Suppress("unused")

package functional.list

sealed class LinkList<out A>

data class Link<out A> internal constructor(val head: A, val tail: LinkList<A>) : LinkList<A>() {
    override fun toString(): String = (tail.buildString(StringBuilder()
        .append("[")
        .append(head)))
        .append("]")
        .toString()
}

object Nil : LinkList<Nothing>() {
    override fun toString(): String = "[]"
}

data class LinkListBuilder<A> internal constructor(val next: LinkListBuilder<A>?, private val head: A) {
    internal fun build(tail: LinkList<A>): LinkList<A> = Link(head, tail).let { next?.build(it) ?: it }
}

infix fun <T> T.l(next: T): LinkListBuilder<T> = LinkListBuilder(LinkListBuilder(null, this), next)
infix fun <T> T.l(next: LinkList<Nothing>): LinkList<T> = Link(this, next)
infix fun <T> LinkListBuilder<T>.l(next: T): LinkListBuilder<T> = LinkListBuilder(this, next)
infix fun <T> LinkListBuilder<T>.l(next: LinkList<Nothing>): LinkList<T> = build(next)

fun <T> LinkList<T>.buildString(stringBuilder: StringBuilder): StringBuilder = when (this) {
    Nil -> stringBuilder
    is Link -> tail.buildString(stringBuilder.append(", ").append(head))
}

fun <T> LinkList<T>.filter(p: (T) -> Boolean): LinkList<T> = when (this) {
    Nil -> Nil
    is Link -> if (p(head)) Link(head, tail.filter(p)) else tail.filter(p)
}

fun <T, U> LinkList<T>.foldl(f: (U, T) -> U, acc: U): U = when (this) {
    Nil -> acc
    is Link -> tail.foldl(f, f(acc, head))
}

fun <T, U> LinkList<T>.foldr(f: (T, U) -> U, acc: U): U = when (this) {
    Nil -> acc
    is Link -> f(head, tail.foldr(f, acc))
}

fun <T> Link<T>.reduce(f: (T, T) -> T): T = foldl(f, head)

fun <T> LinkList<T>.reverse(): LinkList<T> = foldr(::Link, Nil)

fun <T> LinkList<T>.concat(to: LinkList<T>): LinkList<T> = foldr(::Link, to)

fun <T, U> LinkList<T>.flatMap(f: (T) -> LinkList<U>): LinkList<U> = when (this) {
    Nil -> Nil
    is Link -> f(head).concat(tail.bind(f))
}

fun <T, U> LinkList<T>.mapNorec(f: (T) -> U): LinkList<U> =
    reverse().foldl<T, LinkList<U>>({ tail, head -> Link(f(head), tail) }, Nil)

fun <T> LinkList<T>.filterNorec(p: (T) -> Boolean): LinkList<T> =
    reverse().foldl<T, LinkList<T>>(
        { tail, head -> if (p(head)) Link(head, tail.filter(p)) else tail.filter(p) },
        Nil)

fun <T, U> LinkList<T>.foldlNorec(f: (U, T) -> U, a: U): U {
    var acc = a
    var link = this

    while (link is Link) {
        acc = f(acc, link.head)
        link = link.tail
    }

    return acc
}

fun <T, U> LinkList<T>.foldrNorec(f: (T, U) -> U, acc1: U): U =
    reverse().foldlNorec({ acc2, head -> f(head, acc2) }, acc1)

fun <T> Link<T>.reduceNorec(f: (T, T) -> T): T = foldlNorec(f, head)

fun <T> LinkList<T>.reverseNorec(): LinkList<T> =
    foldlNorec<T, LinkList<T>>({ tail, head -> Link(head, tail) }, Nil)

fun <T> LinkList<T>.concatNorec(to: LinkList<T>): LinkList<T> =
    reverse().foldlNorec({ tail, head -> Link(head, tail) }, to)

fun <T, U> LinkList<T>.flatMapNorec(f: (T) -> LinkList<U>): LinkList<U> = when (this) {
    Nil -> Nil
    is Link -> f(head).concatNorec(tail.flatMapNorec(f))
}

// list functor function

fun <A, B> LinkList<A>.fmap(f: (A) -> B): LinkList<B> = when (this) {
    Nil -> Nil
    is Link -> Link(f(head), tail.fmap(f))
}

// list applicative functions

fun <A> A.lift(): LinkList<A> = Link(this, Nil)

fun <A, B> LinkList<(A) -> B>.ap(av: LinkList<A>): LinkList<B> =
    foldr<(A) -> B, LinkList<B>>({ f, a1 -> av.foldr({ v, a2 -> Link(f(v), a2) }, a1) }, Nil)

// list monad functions

fun <A> LinkList<LinkList<A>>.join(): LinkList<A> =
    foldr<LinkList<A>, LinkList<A>>({ x, acc -> x.foldr(::Link, acc) }, Nil)

fun <A, B> LinkList<A>.bind(f: (A) -> LinkList<B>): LinkList<B> =
    foldr<A, LinkList<B>>({ x, acc -> f(x).foldr(::Link, acc) }, Nil)

//fun <A, B> LinkList<A>.bind2(f: (A) -> LinkList<B>): LinkList<B> = f.lift().ap(this).join()

//fun <A, B> LinkList<A>.bind3(f: (A) -> LinkList<B>): LinkList<B> = fmap(f).join()
