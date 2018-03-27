@file:Suppress("unused")

package functional.parser.json

import functional.nullable.toNullableUnit
import functional.nullable.apLeft as nApLeft
import functional.nullable.apRight as nApRight
import functional.parser.sequence.Parser as _Parser
import functional.parser.sequence.ParserIn as _ParserIn
import functional.parser.sequence.ParserOut as _ParserOut
import functional.parser.sequence.apLeft
import functional.parser.sequence.apRight
import functional.parser.sequence.flatMap
import functional.parser.sequence.map
import functional.parser.sequence.matchNothing
import functional.parser.sequence.oneOrMore
import functional.parser.sequence.or
import functional.parser.sequence.repeat
import functional.parser.sequence.toParser
import functional.sequence.toSequence
import functional.parser.sequence.match as _match

typealias Parser<A> = _Parser<Char, A>
typealias ParserIn = _ParserIn<Char>
typealias ParserOut<A> = _ParserOut<Char, A>

sealed class JsonV
data class StringV(val s: String) : JsonV()
data class NumberV(val i: Double) : JsonV()
data class ObjectV(val ps: List<Pair<StringV, JsonV>>) : JsonV()
data class ArrayV(val vs: List<JsonV>) : JsonV()
object TrueV : JsonV()
object FalseV : JsonV()
object NullV : JsonV()

fun matchChar(c: Char): Parser<Unit> =
    { ts ->
        ts
            .firstOrNull()
            ?.equals(c)
            ?.toNullableUnit()
            ?.nApRight(sequenceOf(Pair(ts.drop(1), Unit)))
            ?: emptySequence()
    }

fun matchCharNot(c: Char): Parser<Char> =
    { ts ->
        ts
            .firstOrNull()
            ?.equals(c)
            ?.not()
            ?.toNullableUnit()
            ?.nApRight(sequenceOf(Pair(ts.drop(1), ts.first())))
            ?: emptySequence()
    }

fun matchString(s: String): Parser<Unit> =
    { ts ->
        (ts.take(s.length) == s.toSequence())
            .toNullableUnit()
            ?.let { Pair(ts.drop(s.length), Unit).toSequence() }
            ?: emptySequence()
    }

fun escapedCharP(ts: ParserIn): ParserOut<Char> = lazyEscapedCharP.invoke(ts)
private val lazyEscapedCharP by lazy {
    matchChar('\\')
        .apRight(
            matchChar('\"').apRight('\"'.toParser())
                .or(matchChar('\\').apRight('\\'.toParser()))
                .or(matchChar('/').apRight('/'.toParser()))
                .or(matchChar('b').apRight('\b'.toParser()))
//                .or(matchChar('f').apRight('\f'.toParser()))
                .or(matchChar('n').apRight('\n'.toParser()))
                .or(matchChar('r').apRight('\r'.toParser()))
                .or(matchChar('t').apRight('\t'.toParser()))
        )
//                .or(matchChar('u').apRight('\u'.toParser()))
}

//fun stringP(ts: ParserIn): ParserOut<StringV> = lazyStringP.invoke(ts)
private val stringP by lazy {
    matchChar('"')
        //.apRight(::escapedCharP.or(matchCharNot('"')).repeat())
        .apRight(matchCharNot('"').repeat())
        .map { cs ->
            val sb = StringBuilder()
            cs.forEach { c -> sb.append(c) }
            StringV(sb.toString())
        }
        .apLeft(matchChar('"'))
}

//fun zeroP(ts: ParserIn): ParserOut<Char> = lazyZeroP.invoke(ts)
private val zeroP by lazy {
    matchChar('0').apRight('0'.toParser())
}

//fun oneToNineP(ts: ParserIn): ParserOut<Char> = lazyOneToNineP.invoke(ts)
private val oneToNineP by lazy {
    matchChar('1').apRight('1'.toParser())
        .or(matchChar('2').apRight('2'.toParser()))
        .or(matchChar('3').apRight('3'.toParser()))
        .or(matchChar('4').apRight('4'.toParser()))
        .or(matchChar('5').apRight('5'.toParser()))
        .or(matchChar('6').apRight('6'.toParser()))
        .or(matchChar('7').apRight('7'.toParser()))
        .or(matchChar('8').apRight('8'.toParser()))
        .or(matchChar('9').apRight('9'.toParser()))
}

//fun digitP(ts: ParserIn): ParserOut<Char> = lazyDigitP.invoke(ts)
private val digitP by lazy {
    zeroP
        .or(oneToNineP)
}

//fun digitsP(ts: ParserIn): ParserOut<String> = lazyDigitsP.invoke(ts)
private val digitsP by lazy {
    digitP
        .oneOrMore()
        .map { cs ->
            val sb = StringBuilder()
            cs.forEach { c -> sb.append(c) }
            sb.toString()
        }
}

//fun preDotDigitsP(ts: ParserIn): ParserOut<String> = lazyPreDotDigitsP.invoke(ts)
private val preDotDigitsP by lazy {
    matchChar('0')
        .apRight("0".toParser())
        .or(oneToNineP
            .flatMap { d ->
                digitP
                    .repeat()
                    .map { cs ->
                        val sb = StringBuilder()
                        sb.append(d)
                        cs.forEach { c -> sb.append(c) }
                        sb.toString()
                    }
            })
}

//fun dotDigitsP(ts: ParserIn): ParserOut<String> = lazyDotDigitsP.invoke(ts)
private val dotDigitsP by lazy {
    matchChar('.')
        .apRight(digitsP.map { ds -> ".$ds" })
        .or(matchNothing<Char>().apRight("".toParser()))
}

//fun signP(ts: ParserIn): ParserOut<String> = lazySignP.invoke(ts)
private val signP by lazy {
    matchChar('-')
        .apRight("-".toParser())
        .or(matchNothing<Char>().apRight("".toParser()))
}

//fun exponentP(ts: ParserIn): ParserOut<String> = lazyExponentP.invoke(ts)
private val exponentP by lazy {
    matchChar('e')
        .or(matchChar('E'))
        .apRight(
            matchChar('-')
                .apRight("-".toParser())
                .or(matchChar('+').apRight("+".toParser()))
                .or(matchNothing<Char>().apRight("".toParser()))
                .flatMap { s ->
                    digitsP.flatMap { ds ->
                        "e$s$ds".toParser<Char, String>()
                    }
                })
        .or(matchNothing<Char>().apRight("".toParser()))
}

//fun numberP(ts: ParserIn): ParserOut<NumberV> = lazyNumberP.invoke(ts)
private val numberP by lazy {
    signP
        .flatMap { sign ->
            preDotDigitsP.flatMap { preDots ->
                dotDigitsP.flatMap { dotDigits ->
                    exponentP.flatMap { exponent ->
                        NumberV("$sign$preDots$dotDigits$exponent".toDouble())
                            .toParser<Char, NumberV>()
                    }
                }
            }
        }
}

//fun trueP(ts: ParserIn): ParserOut<TrueV> = lazyTrueP.invoke(ts)
private val trueP by lazy {
    TrueV
        .toParser<Char, TrueV>()
        .apLeft(matchString("true"))
//        .apLeft(matchChar('t'))
//        .apLeft(matchChar('r'))
//        .apLeft(matchChar('u'))
//        .apLeft(matchChar('e'))
}

//fun falseP(ts: ParserIn): ParserOut<FalseV> = lazyFalseP.invoke(ts)
private val falseP by lazy {
    FalseV
        .toParser<Char, FalseV>()
        .apLeft(matchString("false"))
//        .apLeft(matchChar('f'))
//        .apLeft(matchChar('a'))
//        .apLeft(matchChar('l'))
//        .apLeft(matchChar('s'))
//        .apLeft(matchChar('e'))
}

//fun whiteP(ts: ParserIn): ParserOut<Unit> = lazyWhiteP.invoke(ts)
private val whiteP by lazy {
    matchChar(' ')
        .or(matchChar('\t'))
        .or(matchChar('\b'))
        .or(matchChar('\n'))
        .or(matchChar('\r'))
        .repeat()
        .apRight(Unit.toParser())
}

//fun nullP(ts: ParserIn): ParserOut<NullV> = lazyNullP.invoke(ts)
private val nullP by lazy {
    NullV.toParser<Char, NullV>()
        .apLeft(matchString("null"))
//        .apLeft(matchChar('n'))
//        .apLeft(matchChar('u'))
//        .apLeft(matchChar('l'))
//        .apLeft(matchChar('l'))
}

//fun valueP(ts: ParserIn): ParserOut<JsonV> = lazyValueP.invoke(ts)
private val valueP: Parser<JsonV> by lazy {
    whiteP
        .apRight(
            stringP
                .or(numberP)
                .or(objectP)
                .or(::arrayP)
                .or(trueP)
                .or(falseP)
                .or(nullP)
        )
}

fun arrayP(ts: ParserIn): ParserOut<ArrayV> = lazyArrayP.invoke(ts)
private val lazyArrayP: Parser<ArrayV> by lazy {
    matchChar('[')
        .apRight(
            valueP
                .flatMap { v ->
                    whiteP
                        .apRight(matchChar(','))
                        .apRight(valueP)
                        .repeat()
                        .flatMap { vs ->
                            ArrayV(listOf(listOf(v), vs).flatten()).toParser<Char, ArrayV>()
                        }
                }
                .or(matchNothing<Char>().apRight(ArrayV(listOf()).toParser())))
        .apLeft(whiteP)
        .apLeft(matchChar(']'))
}

//fun pairP(ts: ParserIn): ParserOut<Pair<StringV, JsonV>> = lazyPairP.invoke(ts)
private val pairP by lazy {
    whiteP
        .apRight(stringP)
        .apLeft(whiteP)
        .apLeft(matchChar(':'))
        .apLeft(whiteP)
        .flatMap { s ->
            valueP.flatMap { v ->
                Pair(s, v).toParser<Char, Pair<StringV, JsonV>>()
            }
        }
}

//fun objectP(ts: ParserIn): ParserOut<ObjectV> = lazyObjectP.invoke(ts)
private val objectP by lazy {
    matchChar('{')
        .apRight(
            pairP
                .flatMap { p ->
                    whiteP
                        .apRight(matchChar(','))
                        .apRight(pairP)
                        .repeat()
                        .flatMap { ps ->
                            ObjectV(listOf(listOf(p), ps).flatten()).toParser<Char, ObjectV>()
                        }
                }
                .or(matchNothing<Char>().apRight(ObjectV(listOf()).toParser())))
        .apLeft(whiteP)
        .apLeft(matchChar('}'))
}

//fun jsonP(ts: ParserIn): ParserOut<JsonV> = lazyJsonP.invoke(ts)
val jsonP by lazy {
    valueP.apLeft(whiteP)
}
