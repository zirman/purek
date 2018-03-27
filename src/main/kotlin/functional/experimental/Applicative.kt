package functional.experimental

interface Applicative<This, out A> : Functor<This, A> {
    fun <B, C> Applicative<This, (B) -> C>.ap(av: Applicative<This, B>): This
    fun <B> B.unit(): Applicative<This, B>

    fun <B> apLeft(r: Applicative<This, B>): This =
        (fmap { x -> { _: B -> x } } as Applicative<This, (B) -> A>).ap(r)

    fun <B> apRight(r: Applicative<This, B>): This =
        (fmap { _ -> { x: B -> x } } as Applicative<This, (B) -> B>).ap(r)
}
