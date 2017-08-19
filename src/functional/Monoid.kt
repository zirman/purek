package com.outsitenetworks.allpointsrewards.functional.experimental

interface Monoid<T> {
    fun op(left: T, right: T): T
    fun zero(): T
}

object StringConcatMonoid : Monoid<String> {
    override fun op(left: String, right: String): String = left + right
    override fun zero(): String = ""
}

fun <T> ListConcatMonoid(): Monoid<List<T>> = object : Monoid<List<T>> {
    override fun op(left: List<T>, right: List<T>): List<T> = listOf(left, right).flatten()
    override fun zero(): List<T> = emptyList()
}

object DoubleAdditionMonoid : Monoid<Double> {
    override fun op(left: Double, right: Double): Double = left + right
    override fun zero(): Double = 0.0
}

object DoubleMultiplicationMonoid : Monoid<Double> {
    override fun op(left: Double, right: Double): Double = left * right
    override fun zero(): Double = 1.0
}

object IntAdditionMonoid : Monoid<Int> {
    override fun op(left: Int, right: Int): Int = left + right
    override fun zero(): Int = 0
}

object IntMultiplicationMonoid : Monoid<Int> {
    override fun op(left: Int, right: Int): Int = left * right
    override fun zero(): Int = 1
}

object BooleanAndMonoid : Monoid<Boolean> {
    override fun op(left: Boolean, right: Boolean): Boolean = left && right
    override fun zero(): Boolean = true
}

object BooleanOrMonoid : Monoid<Boolean> {
    override fun op(left: Boolean, right: Boolean): Boolean = left || right
    override fun zero(): Boolean = false
}

fun <T> concatenate(list: List<T>, monoid: Monoid<T>): T = list.fold(monoid.zero(), monoid::op)

fun <T, U> foldMap(list: List<T>, monoid: Monoid<U>, f: (T) -> U): U =
    concatenate(list.map(f), monoid)
