import functional.io.*

val startGame: IO<Unit> = numberGame(1, 100)

fun guess(min: Int, max: Int): Int = (min + max) / 2

fun numberGame(min: Int, max: Int): IO<Unit> =
    printlnIO("Is your number ${guess(min, max)}")
        .bind { readLineIO() }
        .bind { a ->
            if (a == "y") printlnIO("Do you want to play again?")
                .bind { readLineIO() }
                .bind { a2 ->
                    if (a2 == "y") startGame
                    else Unit.lift()
                }
            else askRange(min, max)
        }

val numberGame2: (IO<Unit>, Int, Int) -> IO<Unit> =
    { f: IO<Unit>, min: Int, max: Int ->
        printlnIO("Is your number ${guess(min, max)}")
            .bind { readLineIO() }
            .bind { a ->
                if (a == "y") printlnIO("Do you want to play again?")
                    .bind { readLineIO() }
                    .bind { a2 ->
                        if (a2 == "y") f
                        else Unit.lift()
                    }
                else askRange(min, max)
            }
    }

fun recur(io: (IO<Unit>) -> IO<Unit>): IO<Unit> = TODO()

fun askRange(min: Int, max: Int): IO<Unit> = printlnIO("Is it less then or greater than ${guess(min, max)}")
    .bind { readLineIO() }
    .bind { a2 ->
        when (a2) {
            "g" -> numberGame(guess(min, max), max)
            "l" -> numberGame(min, guess(min, max))
            else -> printlnIO("Sorry, please enter [g/l]").bind { askRange(min, max) }
        }
    }

val prog: IO<Unit> = startGame

fun main(args: Array<String>) = prog()
