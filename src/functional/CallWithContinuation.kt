package com.outsitenetworks.allpointsrewards.functional.experimental

class CurrentContinuationThrowable(var value: Any?) : Throwable()

inline fun <reified T : Any> callCc(block: ((T) -> Nothing) -> T): T {
    val throwable = CurrentContinuationThrowable(null)

    return try {
        block {
            throwable.value = it
            throw throwable
        }
    } catch (error: CurrentContinuationThrowable) {
        if (error === throwable) error.value as T else throw error
    }
}
