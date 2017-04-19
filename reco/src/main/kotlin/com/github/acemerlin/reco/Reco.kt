package com.github.acemerlin.reco

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import retrofit2.Call
import retrofit2.HttpException

/**
 * Created by merlin on 12/15/16.
 */
fun <T> WithJob.async(job: Job = this.job, start: Boolean = true, block: suspend CoroutineScope.() -> T): Deferred<T> {
    return kotlinx.coroutines.experimental.async(CommonPool + job, start, block)
}

fun <T> async(job: Job = Job(), start: Boolean = true, block: suspend CoroutineScope.() -> T): Deferred<T> {
    return kotlinx.coroutines.experimental.async(CommonPool + job, start, block)
}

fun WithJob.launch(job: Job = this.job, block: suspend CoroutineScope.() -> Unit): Job =
        kotlinx.coroutines.experimental.launch(UI + job, true, block)

fun launch(job: Job = Job(), block: suspend CoroutineScope.() -> Unit): Job =
        launch(UI + job, true, block)

suspend fun <T> Call<T>.await(handler: (Result.() -> Unit)? = null): T
        = suspendCancellableCoroutine { cont ->
    async(cont.context + CommonPool) {
        try {
            if (cont.isCancelled) {
                return@async
            }
            launch {
                val dsl = Result.Empty()
                if (handler != null) {
                    dsl.handler()
                }
                dsl.beforeHelper?.invoke()
            }.cancel()
            val ret = execute()
            if (ret.isSuccessful && ret.body() != null) {
                launch {
                    val dsl = Result.Succ(ret.body())
                    if (handler != null) {
                        dsl.handler()
                    }
                    cont.resume(ret.body())
                    dsl.afterHelper?.invoke()
                }.cancel()
            } else {
                launch {
                    val dsl = Result.Error(HttpException(ret), this@await)
                    if (handler != null) {
                        dsl.handler()
                    }
                    dsl.errorHelper?.invoke(dsl.e, dsl.call)
                    dsl.afterHelper?.invoke()
                }.cancel()
            }
        } catch (e: Throwable) {
            launch {
                val dsl = Result.Exception(e)
                if (handler != null) {
                    dsl.handler()
                }
                dsl.exceptionHelper?.invoke(dsl.e)
                dsl.afterHelper?.invoke()
            }.cancel()
        }
    }
}