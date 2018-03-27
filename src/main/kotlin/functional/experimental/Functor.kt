package functional.experimental

interface Functor<out This, out A> {
    fun <B> fmap(f: (A) -> B): Functor<This, B>
    fun <B> actual(): Functor<This, B>
}
