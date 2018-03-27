import functional.either.*
import functional.io.IO
import functional.io.flatMap
import functional.io.ioPrintLn
import functional.io.ioReadLine
import functional.io.toIO
import functional.io.whileRight
import kotlinx.coroutines.experimental.newSingleThreadContext

typealias Guess = IntRange

typealias Next = Either<Unit, Guess>

val startingGuess: Guess = 1..100

val guess: (Guess) -> Int = { g -> (g.first + g.last) / 2 }

val guessingGame: (Guess) -> IO<Next> = { s: Guess ->
    val min = s.first
    val max = s.last

    if (min == max) {
        ioPrintLn("Your number is $min").flatMap { askIfDone }
    } else {
        val g = guess(s)

        ioPrintLn("Is your number $g? [y/g/l/q]")
            .flatMap { ioReadLine }
            .flatMap { a ->
                when (a) {
                    "y" -> askIfDone
                    "g" -> nextGuess(Math.min(g + 1, max)..max)
                    "l" -> nextGuess(min..Math.max(g - 1, min))
                    "q" -> done
                    else -> ioPrintLn("Invalid option").flatMap { nextGuess(s) }
                }
            }
    }
}

val askIfDone: IO<Next> = ioPrintLn("Do you want to play again? [y/_]")
    .flatMap { ioReadLine }
    .flatMap { a2 ->
        if (a2 == "y") nextGuess(startingGuess)
        else done
    }

val done: IO<Next> = Left(Unit).toIO()

val nextGuess: (Guess) -> IO<Next> = { s -> Right(s).toIO() }

val prog: IO<Unit> =
    ioPrintLn("Pick a number between 1 and 100.")
        .flatMap { guessingGame.whileRight(startingGuess) }
        .flatMap { ioPrintLn("Play again soon!") }

//val forever1: IO<Int> = { i: Int -> ioPrintLn(i.toString()).flatMap { (i + 1).toIO() } }.repeatRec(0)

//val forever2: IO<Nothing> = { i: Int ->
//    ioPrintLn(i.toString()).flatMap { Right<Nothing, Int>(i + 1).toIO() }
//}.whileRightRec(0)

fun main(args: Array<String>): Unit {
    prog.run(newSingleThreadContext("main"))
}
