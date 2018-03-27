package functional.experimental

interface Monad<This, out A> : Applicative<This, A> {
    fun <B> bind(f: (A) -> Monad<This, B>): Monad<This, B> = f.unit().ap(this) as Monad<This, B>
}

fun <This, B, C, D> ((B) -> Monad<This, C>).fish(f: (C) -> Monad<This, D>): (B) -> Monad<This, D> =
    { b -> invoke(b).bind(f) }

data class MonadList<out T>(val list: List<T>) : Monad<MonadList<*>, T> {
    override fun <B> fmap(f: (T) -> B): Functor<MonadList<*>, B> = MonadList(list.map(f))

    override fun <B, C> Applicative<MonadList<*>, (B) -> C>.ap(
        av: Applicative<MonadList<*>, B>
    ): MonadList<C> =
        ((this as MonadList<(B) -> C>)
            .fmap { f ->
                (av as MonadList<B>)
                    .fmap { v ->
                        f.invoke(v)
                    }
            } as MonadList<MonadList<C>>)
            .join()

    override fun <B> B.unit(): Applicative<MonadList<*>, B> = monadListOf(this)

    override fun <B> actual(): MonadList<B> = this as MonadList<B>
}

fun <A> MonadList<MonadList<A>>.join(): MonadList<A> = MonadList(list.map { it.list }.flatten())

fun <A> monadListOf(vararg xs: A): MonadList<A> = MonadList(listOf(*xs))
