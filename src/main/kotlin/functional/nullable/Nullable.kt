@file:Suppress("unused")

package functional.nullable

// functor function for nullable type

inline fun <A : Any, B : Any> A?.fmap(f: (A) -> B): B? = this?.let(f)

// applicative function for nullable type

infix fun <A : Any, B : Any> ((A) -> B)?.ap(av: A?): B? = this?.let { f -> av?.let(f) }
infix fun <A : Any, B : Any> A?.apRight(r: B?): B? = this?.let { r }
infix fun <A : Any, B : Any> A?.apLeft(r: B?): A? = r?.let { this }

// monad function for nullable type is
// fun <A : Any, B : Any> A.let(f: (A) -> B?): B?

//inline fun <A : Any, B : Any, C : Any> ((A) -> B?).fish(crossinline f: (B) -> C?): (A) -> C? =
//    { w -> invoke(w)?.let(f) }

fun Boolean.toNullableUnit(): Unit? = if (this) Unit else null
