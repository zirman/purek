@file:Suppress("unused")

package functional.parser.sequence

import functional.nullable.apRight
import functional.nullable.toNullableUnit
import functional.sequence.toSequence

typealias Parser<Token, A> = (ParserIn<Token>) -> ParserOut<Token, A>
typealias ParserIn<Token> = Sequence<Token>
typealias ParserOut<Token, A> = Sequence<Pair<Sequence<Token>, A>>

fun <T, A, B> Parser<T, A>.map(f: (A) -> B): Parser<T, B> =
    { ts -> invoke(ts).map { (ts1, x) -> Pair(ts1, f(x)) } }

fun <T, A> Parser<T, Parser<T, A>>.flatten(): Parser<T, A> =
    { ts -> invoke(ts).map { (ts1, f) -> f(ts1) }.flatten() }

fun <T, A, B> Parser<T, (A) -> B>.ap(av: Parser<T, A>): Parser<T, B> =
    { ts -> invoke(ts).map { (ts1, f) -> av(ts1).map { (ts2, x) -> Pair(ts2, f(x)) } }.flatten() }

fun <T, A, B> Parser<T, A>.apRight(r: Parser<T, B>): Parser<T, B> =
    { ts -> invoke(ts).map { (ts1, _) -> r(ts1) }.flatten() }

fun <T, A, B> Parser<T, A>.apLeft(r: Parser<T, B>): Parser<T, A> =
    { ts -> invoke(ts).map { (ts1, x) -> r(ts1).map { (ts2, _) -> Pair(ts2, x) } }.flatten() }

fun <T, A> A.toParser(): Parser<T, A> = { ts -> sequenceOf(Pair(ts, this)) }

fun <T, A, B> Parser<T, A>.flatMap(f: (A) -> Parser<T, B>): Parser<T, B> =
    { ts -> invoke(ts).map { (ts1, x) -> f(x)(ts1) }.flatten() }

fun <T, A, B, C> ((A) -> Parser<T, B>).fish(f: (B) -> Parser<T, C>): (A) -> Parser<T, C> =
    { x -> invoke(x).flatMap(f) }

fun <T, A> Parser<T, A>.and(p: Parser<T, A>): Parser<T, A> =
    { ts -> sequenceOf(invoke(ts), p(ts)).flatten() }

fun <T, A> Parser<T, A>.or(p: Parser<T, A>): Parser<T, A> =
    { ts ->
        val es = invoke(ts)
        if (es.none()) p(ts)
        else es
    }

//fun <T, A> Parser<T, A>.repeat(): Parser<T, List<A>> = repeatR()

fun <T, A> Parser<T, A>.oneOrMore(): Parser<T, List<A>> =
    flatMap { s ->
        repeat()
            .flatMap { ss ->
                listOf(listOf(s), ss)
                    .flatten()
                    .toParser<T, List<A>>()
            }
    }

fun <T, A> Parser<T, A>.repeat(): Parser<T, List<A>> =
    { ts ->
        val acc = mutableListOf<A>()

        tailrec fun repeatR(ts1: ParserIn<T>): ParserOut<T, List<A>> {
            val r = invoke(ts1)

            return if (r.none()) {
                sequenceOf(Pair(ts1, acc))
            } else {
                val (ts2, x) = r.first()
                acc.add(x)
                repeatR(ts2)
            }
        }

        repeatR(ts)
    }

inline fun <T, reified A : Any> match(): Parser<T, A> =
    { ts ->
        ts
            .none()
            .not()
            .toNullableUnit()
            ?.apRight(ts.first() as? A)
            ?.let { x -> sequenceOf(Pair(ts.drop(1), x)) }
            ?: emptySequence()
    }

fun <T> matchNothing(): Parser<T, Unit> =
    { ts -> Pair(ts, Unit).toSequence() }
