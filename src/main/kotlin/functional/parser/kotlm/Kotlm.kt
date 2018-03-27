@file:Suppress("unused")

package functional.parser.kotlm

import functional.parser.sequence.Parser
import functional.parser.sequence.ParserIn
import functional.parser.sequence.ParserOut
import functional.parser.sequence.apLeft
import functional.parser.sequence.apRight
import functional.parser.sequence.flatMap
import functional.parser.sequence.map
import functional.parser.sequence.or
import functional.parser.sequence.repeat

import functional.parser.sequence.match as _match
import functional.parser.sequence.toParser as _toParser

sealed class Token
object LetT : Token()
object InT : Token()
object LParT : Token()
object RParT : Token()
object CommaT : Token()
object SemiT : Token()
object ArrowT : Token()
object EqualT : Token()
object PlusT : Token()
object MinusT : Token()
object FunT : Token()
data class IntT(val i: Int) : Token()
data class SymT(val s: String) : Token()

sealed class Exp
data class LetE(val ass: List<AssignSt>, val e: Exp) : Exp()
data class FunE(val ss: List<SymE>, val e: Exp) : Exp()
data class TupE(val es: List<Exp>) : Exp()
data class ApE(val es: List<Exp>) : Exp()
data class IntE(val i: Int) : Exp()
data class PlusE(val l: Exp, val r: Exp) : Exp()
data class MinusE(val l: Exp, val r: Exp) : Exp()
data class SymE(val s: String) : Exp()

data class AssignSt(val s: SymE, val e: Exp)

inline fun <reified A : Any> match(): Parser<Token, A> = _match()

fun <A> A.toParser(): Parser<Token, A> = _toParser()

// let x = let y = 2 in y ; y = 2 in x * 2
// fun x y -> x + y

fun funP(ts: ParserIn<Token>): ParserOut<Token, FunE> =
    match<FunT>()
        .apRight(::symP.repeat())
        .flatMap { ss ->
            match<ArrowT>()
                .apRight(::expP)
                .flatMap { e -> FunE(ss, e).toParser() }
        }
        .invoke(ts)

fun letP(ts: ParserIn<Token>): ParserOut<Token, LetE> =
    match<LetT>()
        .apRight(::defP)
        .flatMap { a ->
            match<SemiT>()
                .apRight(::defP)
                .repeat()
                .flatMap { ass -> listOf(listOf(a), ass).flatten().toParser() }
        }
        .flatMap { ass ->
            match<InT>()
                .apRight(::expP)
                .flatMap { e -> LetE(ass, e).toParser() }
        }
        .invoke(ts)

fun prodP(ts: ParserIn<Token>): ParserOut<Token, Exp> =
    ::letP
        .or(::funP)
        .invoke(ts)

fun termP(ts: ParserIn<Token>): ParserOut<Token, Exp> =
    match<IntT>()
        .map { i -> IntE(i.i) }
        .or(::symP)
        .invoke(ts)

fun expP(ts: ParserIn<Token>): ParserOut<Token, Exp> =
    ::prodP
        .or(::expP2.apLeft(match<PlusT>())
            .flatMap { l -> ::expP.flatMap { r -> PlusE(l, r).toParser() } })
        .or(::expP2.apLeft(match<MinusT>())
            .flatMap { l -> ::expP.flatMap { r -> MinusE(l, r).toParser() } })
        .or(::termP)
        .invoke(ts)

fun expP2(ts: ParserIn<Token>): ParserOut<Token, Exp> =
    ::prodP
        .or(::termP)
        .invoke(ts)

fun symP(ts: ParserIn<Token>): ParserOut<Token, SymE> =
    match<SymT>().map { s -> SymE(s.s) }.invoke(ts)

fun defP(ts: ParserIn<Token>): ParserOut<Token, AssignSt> =
    ::symP
        .flatMap { s ->
            match<EqualT>().apRight(::expP)
                .flatMap { e -> AssignSt(s, e).toParser() }
        }
        .invoke(ts)
