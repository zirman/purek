package com.robertchrzanowski.main

import functional.parser.json.jsonP

fun main(args: Array<String>) {
    val parsed = jsonP("""[[[[],[],[],[]]]]""".asSequence()).toList()
    //expP(sequenceOf(FunT, SymT("x"), SymT("y"), ArrowT, LetT, SymT("x"), EqualT, LetT, SymT("y"), EqualT, IntT(2), SemiT, SymT("z"), EqualT, IntT(3), InT, SymT("y"), PlusT, SymT("y"), InT, SymT("x"), PlusT, SymT("x")))
    //expP(sequenceOf(LParenT, SymbolT("asdf"), LParenT, SymbolT("foo"), SymbolT("BAR"), RParenT, RParenT))
    //
    // expP(linkedListOf(SymT("x"), PlusT, SymT("y")))
    // expP(linkedListOf(LetT, SymT("x"), EqualT, IntT(2), SemiT, SymT("y"), EqualT, IntT(2), InT, SymT("x"), PlusT, SymT("y")))
    // expP(linkedListOf(LetT, SymT("x"), EqualT, LetT, SymT("y"), EqualT, IntT(2), SemiT, SymT("z"), EqualT, IntT(3), InT, SymT("y"), PlusT, SymT("y"), InT, SymT("x"), PlusT, SymT("x")))

    // let x = let y = 2 in y + y in x + x

    if (parsed.none()) {
        println("Failed at: End")
    } else {
        parsed
            .map { (ts, e) ->
                if (ts.none()) println("Success: $e")
                else println("Failed at: ${ts.toList()}")
            }
            .forEach { }
    }
}
