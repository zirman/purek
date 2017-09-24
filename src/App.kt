import functional.either.Either
import functional.either.left
import functional.either.right
import functional.io.IO
import functional.io.bind
import functional.io.printlnIO
import functional.io.readLineIO
import functional.io.toIO
import functional.io.whileRight

typealias Guess = IntRange

typealias Next = Either<Unit, Guess>

val startingGuess: Guess = 1..100

val guess: (Guess) -> Int = { g -> (g.first + g.last) / 2 }

val guessingGame: (Guess) -> IO<Next> = { s: Guess ->
    val min = s.first
    val max = s.last

    if (min == max) {
        printlnIO("Your number is $min").bind { askIfDone }
    } else {
        val g = guess(s)

        printlnIO("Is your number $g? [y/g/l/q]")
            .bind { readLineIO }
            .bind { a ->
                when (a) {
                    "y" -> askIfDone
                    "g" -> nextGuess(Math.min(g + 1, max)..max)
                    "l" -> nextGuess(min..Math.max(g - 1, min))
                    "q" -> done
                    else -> printlnIO("Invalid option").bind { nextGuess(s) }
                }
            }
    }
}

val askIfDone: IO<Next> = printlnIO("Do you want to play again? [y/_]")
    .bind { readLineIO }
    .bind { a2 ->
        if (a2 == "y") nextGuess(startingGuess)
        else done
    }

val done: IO<Next> = left<Unit, Guess>(Unit).toIO()

val nextGuess: (Guess) -> IO<Next> = { s -> right<Unit, Guess>(s).toIO() }

val prog: IO<Unit> =
    printlnIO("Pick a number between 1 and 100.")
        .bind { guessingGame.whileRight(startingGuess) }
        .bind { printlnIO("Play again soon!") }

//val forever1: IO<Int> = { i: Int -> printlnIO(i.toString()).bind { (i + 1).toIO() } }.repeatRec(0)

//val forever2: IO<Nothing> = { i: Int ->
//    printlnIO(i.toString()).bind { Right<Nothing, Int>(i + 1).toIO() }
//}.whileRightRec(0)

fun main(args: Array<String>): Unit {
    prog.run()
}
