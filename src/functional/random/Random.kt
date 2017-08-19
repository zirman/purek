@file:Suppress("unused")

package functional.random

import java.util.Random as Rand
import functional.state.State

typealias Random<A> = State<Long, A>

fun randBoolean(): Random<Boolean> = { s -> random(Rand(s).nextBoolean(), s) }
fun randDouble(): Random<Double> = { s -> random(Rand(s).nextDouble(), s) }
fun randFloat(): Random<Float> = { s -> random(Rand(s).nextFloat(), s) }
fun randInt(): Random<Int> = { s -> random(Rand(s).nextInt(), s) }
fun randLong(): Random<Long> = { s -> random(Rand(s).nextLong(), s) }

private fun <A> random(v: A, s: Long): Pair<A, Long> = Pair(v, Rand(s).nextLong())
