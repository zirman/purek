@file:Suppress("unused")

package functional.parser.linkedList

import functional.linkedList.Cell
import functional.linkedList.End
import functional.linkedList.LinkedList
import functional.linkedList.join
import functional.linkedList.linkedListOf
import functional.linkedList.reverse
import functional.parser.sequence.ParserIn
import functional.parser.sequence.ParserOut

//typealias Parser<Token, A> = (ParserIn<Token>) -> ParserOut<Token, A>
//typealias ParserIn<Token> = LinkedList<Token>
//typealias ParserOut<Token, A> = LinkedList<Pair<LinkedList<Token>, A>>
//
//fun <T, A, B> Parser<T, A>.fmap(f: (A) -> B): Parser<T, B> =
//    { ts -> invoke(ts).map { (ts1, x) -> Pair(ts1, f(x)) } }
//
//fun <T, A> Parser<T, Parser<T, A>>.join(): Parser<T, A> =
//    { ts -> invoke(ts).map { (ts1, f) -> f(ts1) }.flatten() }
//
//fun <T, A, B> Parser<T, (A) -> B>.ap(av: Parser<T, A>): Parser<T, B> =
//    { ts -> invoke(ts).map { (ts1, f) -> av(ts1).map { (ts2, x) -> Pair(ts2, f(x)) } }.flatten() }
//
//fun <T, A, B> Parser<T, A>.apRight(r: Parser<T, B>): Parser<T, B> =
//    { ts -> invoke(ts).map { (ts1, _) -> r(ts1) }.flatten() }
//
//fun <T, A, B> Parser<T, A>.apLeft(r: Parser<T, B>): Parser<T, A> =
//    { ts -> invoke(ts).map { (ts1, x) -> r(ts1).map { (ts2, _) -> Pair(ts2, x) } }.flatten() }
//
//fun <T, A> A.toParser(): Parser<T, A> = { ts -> linkedListOf(Pair(ts, this)) }
//
//fun <T, A, B> Parser<T, A>.bind(f: (A) -> Parser<T, B>): Parser<T, B> =
//    { ts -> invoke(ts).map { (ts1, x) -> f(x)(ts1) }.flatten() }
//
//fun <T, A, B, C> ((A) -> Parser<T, B>).fish(f: (B) -> Parser<T, C>): (A) -> Parser<T, C> =
//    { x -> invoke(x).bind(f) }
//
//fun <T, A> Parser<T, A>.and(p: Parser<T, A>): Parser<T, A> =
//    { ts -> linkedListOf(invoke(ts), p(ts)).flatten() }
//
//fun <T, A> Parser<T, A>.or(p: Parser<T, A>): Parser<T, A> =
//    { ts ->
//        val es = invoke(ts)
//        if (es is End) p(ts)
//        else es
//    }
//
//fun <T, A> Parser<T, A>.repeat(): Parser<T, LinkedList<A>> = repeatR(End)
//
//fun <T, A> Parser<T, A>.oneOrMore(): Parser<T, LinkedList<A>> =
//    bind { s ->
//        repeat()
//            .bind { ss ->
//                Cell(s, ss).toParser<T, LinkedList<A>>()
//            }
//    }
//
//fun <T, A> Parser<T, A>.repeatR(acc: LinkedList<A>): Parser<T, LinkedList<A>> =
//    { ts ->
//        val r = invoke(ts)
//        if (r is End) linkedListOf(Pair(ts, acc.reverse()))
//        else r.map { (ts, x) -> repeatR(Cell(x, acc)).invoke(ts) }.join()
//    }
//
//inline fun <T, reified A> match(): Parser<T, A> =
//    { ts ->
//        if (ts is Cell && ts.head is A) Pair(ts.tail, ts.head as A).toLinkedList()
//        else End
//    }
