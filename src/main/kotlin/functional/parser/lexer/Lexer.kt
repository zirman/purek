package functional.parser.lexer

typealias Lexer<T> = (CharSequence) -> Sequence<T>

fun <T> makeLexer(ls: List<Pair<Regex, (MatchResult) -> T>>): Lexer<T> =
    { cs ->
        ls
            .firstOrNull { (r, _) -> r.find(cs) != null }
            ?.let { (r, f) ->
                f(r.find(cs)!!)
//                Pair(foo?.let(f), )

            }
        TODO()
    }
