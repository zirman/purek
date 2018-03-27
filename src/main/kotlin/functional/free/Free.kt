@file:Suppress("unused")

package functional.free

sealed class Op<out A> {
    abstract fun <B> fmap(f: (A) -> B): Op<B>
}

private data class Output<out T, out A>(val output: T, val x: A) : Op<A>() {
    override fun <B> fmap(f: (A) -> B): Op<B> = Output(output, f(x))
}

private data class Bell<out A>(val x: A) : Op<A>() {
    override fun <B> fmap(f: (A) -> B): Op<B> = Bell(f(x))
}

private object Done : Op<Nothing>() {
    override fun <B> fmap(f: (Nothing) -> B): Op<B> = Done
}

sealed class Free<out A> {
    fun <B> prev(f: Free<B>): Free<A> = bind { x -> f.fmap { _ -> x } }
    fun <B> next(f: Free<B>): Free<B> = bind { _ -> f }
    abstract fun <B> fmap(f: (A) -> B): Free<B>
    abstract fun <B> bind(f: (A) -> Free<B>): Free<B>
}

private data class Pure<out A>(val x: A) : Free<A>() {
    override fun <B> fmap(f: (A) -> B): Free<B> = Pure(f(x))
    override fun <B> bind(f: (A) -> Free<B>): Free<B> = f(x)
}

private data class LiftOp<out A>(val op: Op<Free<A>>) : Free<A>() {
    override fun <B> fmap(f: (A) -> B): Free<B> = LiftOp(op.fmap { x -> x.fmap(f) })
    override fun <B> bind(f: (A) -> Free<B>): Free<B> = LiftOp(op.fmap { x -> x.bind(f) })
}

fun <A> Free<Free<A>>.flatten(): Free<A> =
    when (this) {
        is Pure -> x
        is LiftOp -> LiftOp(op.fmap { x -> x.bind { y -> y } })
    }

private fun <A> liftOp(op: Op<A>): Free<A> = LiftOp(op.fmap(::Pure))

fun <A> output(output: A): Free<Unit> = liftOp(Output(output, Unit))
val bell: Free<Unit> = liftOp(Bell(Unit))
val done: Free<Unit> = liftOp(Done)

val subroutine = output('a')

val program1 = subroutine.bind { bell }

val program2 =
    subroutine
        .next(bell.fmap { _ -> 3 })
        .bind(::output)
        .next(done)

fun <A> showProgram(f: Free<A>): String =
    when (f) {
        is Pure -> "return ${f.x}\n"
        is LiftOp<A> ->
            when (f.op) {
                is Done -> "done\n"
                is Bell -> "bell\n${showProgram(f.op.x)}"
                is Output<*, Free<A>> -> "output ${f.op.output}\n${showProgram(f.op.x)}"
            }
    }
