@file:Suppress("unused")

package functional.rpnsolver

import functional.either.Either
import functional.either.Left
import functional.either.Right
import functional.either.bind
import functional.linkedList.Cell
import functional.linkedList.End
import functional.linkedList.LinkedList
import functional.linkedList.foldM

fun String.words(): LinkedList<String> = split(" ").foldRight(End, ::Cell)

fun foldingFunction(stack: LinkedList<Double>, op: String): Either<Exception, LinkedList<Double>> =
    try {
        when (op) {
            "*" -> {
                stack as Cell<Double>
                stack.tail as Cell<Double>
                Right(Cell(stack.head * stack.tail.head, stack.tail.tail))
            }
            "+" -> {
                stack as Cell<Double>
                stack.tail as Cell<Double>
                Right(Cell(stack.head + stack.tail.head, stack.tail.tail))
            }
            "-" -> {
                stack as Cell<Double>
                stack.tail as Cell<Double>
                Right(Cell(stack.head - stack.tail.head, stack.tail.tail))
            }
            else -> {
                Right(Cell(op.toDouble(), stack))
            }
        }
    } catch (e: Exception) {
        Left(e)
    }

fun String.solveRPN(): Either<Exception, Double> =
    words()
        .foldM(End, ::foldingFunction)
        .bind { stack ->
            try {
                stack as Cell<Double>
                if (stack.tail == End) Right(stack.head)
                else Left(Exception("ads"))
            } catch (e: Exception) {
                Left(e)
            }
        }
