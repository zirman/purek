@file:Suppress("unused")

package functional.monoid

import functional.linkedList.Cell
import functional.linkedList.End
import functional.linkedList.LinkedList
import functional.linkedList.foldr

interface Monoid<A> {
    fun unit(): A
    fun merge(a1: A, a2: A): A
}

object StringMonoid : Monoid<String> {
    override fun unit(): String = ""
    override fun merge(a1: String, a2: String): String = a1 + a2
}

class ListMonoid<A> : Monoid<LinkedList<A>> {
    override fun unit(): LinkedList<A> = End
    override fun merge(a1: LinkedList<A>, a2: LinkedList<A>): LinkedList<A> = a1.foldr(a2, ::Cell)
}

object IntAdditionMonoid : Monoid<Int> {
    override fun unit(): Int = 0
    override fun merge(a1: Int, a2: Int): Int = a1 + a2
}

object IntProductMonoid : Monoid<Int> {
    override fun unit(): Int = 1
    override fun merge(a1: Int, a2: Int): Int = a1 * a2
}

object DoubleAdditionMonoid : Monoid<Double> {
    override fun unit(): Double = 0.0
    override fun merge(a1: Double, a2: Double): Double = a1 + a2
}

object DoubleProductMonoid : Monoid<Double> {
    override fun unit(): Double = 1.0
    override fun merge(a1: Double, a2: Double): Double = a1 * a2
}

object FloatAdditionMonoid : Monoid<Float> {
    override fun unit(): Float = 0.0f
    override fun merge(a1: Float, a2: Float): Float = a1 + a2
}

object FloatProductMonoid : Monoid<Float> {
    override fun unit(): Float = 1.0f
    override fun merge(a1: Float, a2: Float): Float = a1 * a2
}
