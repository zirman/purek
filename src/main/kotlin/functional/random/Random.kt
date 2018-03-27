@file:Suppress("unused")

package functional.random

import java.util.Random as Rand
import functional.state.State

typealias Random<A> = State<Long, A>

fun randBoolean(): Random<Boolean> = { s -> random(s, Rand(s).nextBoolean()) }
fun randDouble(): Random<Double> = { s -> random(s, Rand(s).nextDouble()) }
fun randFloat(): Random<Float> = { s -> random(s, Rand(s).nextFloat()) }
fun randInt(): Random<Int> = { s -> random(s, Rand(s).nextInt()) }
fun randLong(): Random<Long> = { s -> random(s, Rand(s).nextLong()) }

private fun <A> random(s: Long, v: A): Pair<Long, A> = Pair(Rand(s).nextLong(), v)
