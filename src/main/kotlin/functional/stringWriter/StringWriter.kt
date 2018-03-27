@file:Suppress("unused")

package functional.stringWriter

import functional.monoid.StringMonoid
import functional.writer.Writer
import functional.writer.toWriter
import functional.writer.write as writeW

typealias StringWriter<Value> = Writer<Value, String, StringMonoid>

fun <A> A.toStringWriter(): StringWriter<A> = toWriter(StringMonoid)

fun <A> A.write(mv: String): StringWriter<A> = writeW(mv, StringMonoid)
