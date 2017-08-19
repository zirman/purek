package functional.nullable

// functor function for nullable type

//inline fun <T : Any, U : Any> T?.fmap(f: (T) -> U): U? = this?.let(f)

// applicative function for nullable type

infix fun <T : Any, U : Any> ((T) -> U)?.ap(av: T?): U? = this?.let { f -> av?.let(f) }

// monad function for nullable type

//inline fun <T : Any, U : Any> T?.bind(f: (T) -> U?): U? = this?.let(f)
