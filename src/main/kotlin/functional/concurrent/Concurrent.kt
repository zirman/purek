@file:Suppress("unused")

package functional.concurrent

import functional.either.EitherS
import functional.either.LeftS
import functional.either.RightS
import functional.either.with
import functional.io.IO
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.actor
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import kotlin.coroutines.experimental.CoroutineContext

typealias Task<A> = EitherS<IO<A>, () -> A>

fun <A> task(t: () -> A): Task<A> = RightS(t)
fun <A> task(t: IO<A>): Task<A> = LeftS(t)

sealed class Concurrent<out A> {
    fun run(cc: CoroutineContext = CommonPool): Unit =
        runBlocking { launch(cc) { run(cc, {}) }.join() }

    abstract suspend fun run(cc: CoroutineContext, f: suspend (A) -> Unit): Unit
}

internal class Tasks<out A>(vararg ts: Task<A>) : Concurrent<A>() {
    private val ts: Set<Task<A>> = ts.toSet()

    override suspend fun run(cc: CoroutineContext, f: suspend (A) -> Unit): Unit {
        val jobsActor = actor<EitherS<Job, Job>>(cc) {
            val jobs = mutableSetOf<Job>()

            for (msg in channel) {
                msg.with(
                    { job -> jobs.remove(job) },
                    { job -> jobs.add(job) })
            }

            jobs.forEach { job -> job.join() }
        }

        ts
            .forEach { task ->
                var job: Job? = null

                job = launch(cc) {
                    f(task.with({ io -> io.run() }, { g -> g() }))
                    job?.let { job -> jobsActor.channel.send(LeftS(job)) }
                }

                jobsActor.channel.send(RightS(job))
            }

        jobsActor.join()
    }
}

internal class WhileRight<A, out B>(
    private val initState: A,
    private val io: (A) -> IO<EitherS<Unit, Pair<A, B>>>
) : Concurrent<B>() {
    override suspend fun run(cc: CoroutineContext, f: suspend (B) -> Unit): Unit {
        var state = initState

        val jobsActor = actor<EitherS<Job, Job>>(cc) {
            val jobs = mutableSetOf<Job>()

            for (msg in channel) {
                msg.with(
                    { job -> jobs.remove(job) },
                    { job -> jobs.add(job) })
            }

            jobs.forEach { job -> job.join() }
        }

        do {
        } while (io(state)
                .run()
                .with(
                    { _ -> false },
                    { (nextState, y) ->
                        state = nextState
                        var job: Job? = null

                        job = launch(cc) {
                            f(y)
                            job?.let { job -> jobsActor.channel.send(LeftS(job)) }
                        }

                        jobsActor.channel.send(RightS(job))
                        true
                    })
        )

        jobsActor.join()
    }
}

internal class Map<out A, out B>(
    private val c: Concurrent<A>,
    private val g: (A) -> B
) : Concurrent<B>() {
    override suspend fun run(cc: CoroutineContext, f: suspend (B) -> Unit): Unit =
        c.run(cc) { x -> f(g(x)) }
}

internal class Flatten<out A>(private val c: Concurrent<Concurrent<A>>) : Concurrent<A>() {
    override suspend fun run(cc: CoroutineContext, f: suspend (A) -> Unit) =
        c.run(cc) { c2 -> c2.run(cc) { x -> f(x) } }
}

internal class FlattenIO<out A>(private val c: Concurrent<IO<A>>) : Concurrent<A>() {
    override suspend fun run(cc: CoroutineContext, f: suspend (A) -> Unit) =
        c.run(cc) { io -> f(io.run()) }
}

internal class Ap<A, out B>(
    private val af: Concurrent<(A) -> B>,
    private val av: Concurrent<A>
) : Concurrent<B>() {
    override suspend fun run(cc: CoroutineContext, f: suspend (B) -> Unit) =
        af.run(cc) { g -> av.run(cc) { x -> f(g(x)) } }
}

internal class ApRight<out A, out B>(
    private val l: Concurrent<A>,
    private val r: Concurrent<B>
) : Concurrent<B>() {
    override suspend fun run(cc: CoroutineContext, f: suspend (B) -> Unit) =
        l.run(cc) { _ -> r.run(cc) { x -> f(x) } }
}

internal class ApLeft<out A, out B>(
    private val l: Concurrent<A>,
    private val r: Concurrent<B>
) : Concurrent<A>() {
    override suspend fun run(cc: CoroutineContext, f: suspend (A) -> Unit) =
        l.run(cc) { x -> r.run(cc) { _ -> f(x) } }
}

internal class FlatMap<A, out B>(
    private val c: Concurrent<A>,
    private val g: (A) -> Concurrent<B>
) : Concurrent<B>() {
    override suspend fun run(cc: CoroutineContext, f: suspend (B) -> Unit) =
        c.run(cc) { x -> g(x).run(cc, f) }
}

internal class FlatMapIO<A, out B>(
    private val c: Concurrent<A>,
    private val g: (A) -> IO<B>
) : Concurrent<B>() {
    override suspend fun run(cc: CoroutineContext, f: suspend (B) -> Unit) =
        c.run(cc) { x -> f(g(x).run()) }
}

fun <A, B> Concurrent<A>.map(f: (A) -> B): Concurrent<B> = Map(this, f)

fun <A> A.toConcurrent(): Concurrent<A> = Tasks(task { this })
fun <A> IO<A>.toConcurrentIO(): Concurrent<A> = Tasks(task(this))

fun <A, B> Concurrent<(A) -> B>.ap(av: Concurrent<A>): Concurrent<B> = Ap(this, av)

fun <A> Concurrent<Concurrent<A>>.flatten(): Concurrent<A> = Flatten(this)
fun <A> Concurrent<IO<A>>.flattenIO(): Concurrent<A> = FlattenIO(this)

fun <A, B> Concurrent<A>.flatMap(f: (A) -> Concurrent<B>): Concurrent<B> = FlatMap(this, f)
fun <A, B> Concurrent<A>.flatMapIO(f: (A) -> IO<B>): Concurrent<B> = FlatMapIO(this, f)

val concurrentUnit = Unit.toConcurrent()

fun <A, B> whileRight(
    initState: A,
    io: (A) -> IO<EitherS<Unit, Pair<A, B>>>
): Concurrent<B> =
    WhileRight(initState, io)
