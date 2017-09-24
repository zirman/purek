@file:Suppress("unused")

package functional.monoid

import functional.list.Cell
import functional.list.LinkedList
import functional.list.End
import functional.list.foldr

interface Monoid<A> {
    fun mempty(): A
    fun mconcat(a1: A, a2: A): A
}

object StringMonoid : Monoid<String> {
    override fun mempty(): String = ""
    override fun mconcat(a1: String, a2: String): String = a1 + a2
}

class ListMonoid<A> : Monoid<LinkedList<A>> {
    override fun mempty(): LinkedList<A> = End
    override fun mconcat(a1: LinkedList<A>, a2: LinkedList<A>): LinkedList<A> = a1.foldr(a2, ::Cell)
}

object IntAdditionMonoid : Monoid<Int> {
    override fun mempty(): Int = 0
    override fun mconcat(a1: Int, a2: Int): Int = a1 + a2
}

object IntProductMonoid : Monoid<Int> {
    override fun mempty(): Int = 1
    override fun mconcat(a1: Int, a2: Int): Int = a1 * a2
}

object DoubleAdditionMonoid : Monoid<Double> {
    override fun mempty(): Double = 0.0
    override fun mconcat(a1: Double, a2: Double): Double = a1 + a2
}

object DoubleProductMonoid : Monoid<Double> {
    override fun mempty(): Double = 1.0
    override fun mconcat(a1: Double, a2: Double): Double = a1 * a2
}

object FloatAdditionMonoid : Monoid<Float> {
    override fun mempty(): Float = 0.0f
    override fun mconcat(a1: Float, a2: Float): Float = a1 + a2
}

object FloatProductMonoid : Monoid<Float> {
    override fun mempty(): Float = 1.0f
    override fun mconcat(a1: Float, a2: Float): Float = a1 * a2
}
