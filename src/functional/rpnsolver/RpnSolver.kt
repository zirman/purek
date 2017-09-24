@file:Suppress("unused")

package functional.rpnsolver

import functional.either.Either
import functional.either.left
import functional.either.right
import functional.list.Cell
import functional.list.LinkedList
import functional.list.End
import functional.list.foldM
import functional.path.bind

fun String.words(): LinkedList<String> = split(" ").foldRight(End, ::Cell)

fun foldingFunction(stack: LinkedList<Double>, op: String): Either<Exception, LinkedList<Double>> =
    try {
        when (op) {
            "*" -> {
                stack as Cell<Double>
                stack.tail as Cell<Double>
                right(Cell(stack.head * stack.tail.head, stack.tail.tail))
            }
            "+" -> {
                stack as Cell<Double>
                stack.tail as Cell<Double>
                right(Cell(stack.head + stack.tail.head, stack.tail.tail))
            }
            "-" -> {
                stack as Cell<Double>
                stack.tail as Cell<Double>
                right(Cell(stack.head - stack.tail.head, stack.tail.tail))
            }
            else -> {
                right(Cell(op.toDouble(), stack))
            }
        }
    } catch (e: Exception) {
        left(e)
    }

fun String.solveRPN(): Either<Exception, Double> = words().foldM(End, ::foldingFunction).bind { stack ->
    try {
        stack as Cell<Double>
        if (stack.tail == End) right<Exception, Double>(stack.head)
        else left(Exception("ads"))
    } catch (e: Exception) {
        left<Exception, Double>(e)
    }
}
