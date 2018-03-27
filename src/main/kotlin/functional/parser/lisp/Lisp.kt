@file:Suppress("unused")

package functional.parser.lisp

import functional.parser.sequence.Parser
import functional.parser.sequence.ParserIn
import functional.parser.sequence.ParserOut
import functional.parser.sequence.apLeft
import functional.parser.sequence.apRight
import functional.parser.sequence.map
import functional.parser.sequence.or
import functional.parser.sequence.repeat
import functional.parser.sequence.match as _match

sealed class Token
object LParenT : Token()
object RParenT : Token()
data class SymbolT(val s: String) : Token()

sealed class Expr
data class ListE(val es: List<Expr>) : Expr()
data class SymbolE(val s: String) : Expr()

inline fun <reified A : Any> match(): Parser<Token, A> = _match()

fun expP(ts: ParserIn<Token>): ParserOut<Token, Expr> =
    match<LParenT>()
        .apRight(::expP.repeat())
        .map(::ListE)
        .apLeft(match<RParenT>())
        .or(match<SymbolT>().map { s -> SymbolE(s.s) })
        .invoke(ts)
