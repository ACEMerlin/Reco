package com.github.acemerlin.reco

import retrofit2.Call
import retrofit2.HttpException

/**
 * Created by merlin on 19/04/2017.
 */
internal typealias ExceptionType = (e: Throwable) -> Unit
internal typealias ErrorType = (e: HttpException, call: Call<*>) -> Unit
internal typealias AfterType = () -> Unit
internal typealias BeforeType = () -> Unit

sealed class Result {

    internal data class Succ<out T>(val value: T) : Result() {
        //TODO Use Monad to simplify stuff
        operator fun <E> get(ele: T.(v: T) -> E): E {
            return value.ele(value)
        }
    }

    internal data class Error(val e: HttpException, val call: Call<*>) : Result()

    internal data class Exception(val e: Throwable) : Result()

    internal class Empty : Result()

    internal var errorHelper: ErrorType? = null
    internal var afterHelper: AfterType? = null
    internal var exceptionHelper: ExceptionType? = null
    internal var beforeHelper: BeforeType? = null

    open fun error(error: ErrorType) {
        this.errorHelper = error
    }

    open fun after(after: AfterType) {
        this.afterHelper = after
    }

    open fun exception(exception: ExceptionType) {
        this.exceptionHelper = exception
    }

    open fun before(before: BeforeType) {
        this.beforeHelper = before
    }
}