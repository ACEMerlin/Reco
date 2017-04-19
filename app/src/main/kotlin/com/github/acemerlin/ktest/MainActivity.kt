@file:Suppress("UNUSED_VARIABLE")

package com.github.acemerlin.ktest

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.github.acemerlin.reco.*
import kotlinx.coroutines.experimental.Job
import org.jetbrains.anko.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface TestService {
    @GET("posts/{id}")
    fun repo(@Path("id") id: Int): Call<Repo>
}

data class Repo(val userId: Int, val id: Int, val title: String)

class MainActivity : AppCompatActivity(), WithJob {

    override val job = Job()

    val rightId = 1
    val wrongId = 143814

    val retrofit = Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val service = retrofit.create(TestService::class.java)

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        verticalLayout {
            textView {
                padding = dip(4)
                backgroundColor = android.R.color.black
                text = "Please Enjoy!"
            }
            button("Two Sequential Request") {
                onClick {
                    test1()
                }
            }
            button("Two Concurrent Request") {
                onClick {
                    test2()
                }
            }
            button("Request with custom handlers") {
                onClick {
                    test3()
                }
            }
            button("Failed request with error handler") {
                onClick {
                    test4()
                }
            }
            button("Two Sequential Request with one of them failed") {
                onClick {
                    test5()
                }
            }
            button("Two Concurrent Request with one of them failed") {
                onClick {
                    test6()
                }
            }
            button("Cancel activity job") {
                backgroundColor = Color.RED
                onClick {
                    job.cancel()
                    toast("Activity job canceled")
                }
            }
        }
    }

    fun test1() = launch {
        val repo1 = service.repo(rightId).await()
        toast("First Request Done!\nYou can do things with repo1 now")
        val repo2 = service.repo(rightId).await()
        toast("Second Request Done!\nYou can do things with repo2 now")
    }

    fun test2() = launch {
        val repo1Deferred = async { service.repo(rightId).await() }
        val repo2Deferred = async { service.repo(rightId).await() }
        val repo1 = repo1Deferred.await()
        val repo2 = repo2Deferred.await()
        toast("Both Request Done!\nYou can do things with them now")
    }

    fun test3() = launch {
        val repo = service.repo(rightId).await {
            before {
                toast("Handle things before request")
            }
            error { e, _ ->
                toast("Handle error here: $e")
            }
            exception { e ->
                toast("Handle exceptions here: $e")
            }
            after {
                toast("Handle things after (failed/successful) request")
            }
        }
        toast("Request Done!\nYou can do things with repo now")
    }

    fun test4() = launch {
        val repo = service.repo(wrongId).await {
            error { e, _ ->
                alert("$e", "Error!") {
                    positiveButton("Ok I know") { dismiss() }
                }.show()
            }
        }
        toast("Request Done!\nYou can do things with repo now")
    }

    fun test5() = launch {
        val repo1 = service.repo(wrongId).await()
        toast("Not gonna toast coz repo1 failed")
        val repo2 = service.repo(rightId).await()
        toast("Not gonna toast")
    }

    val myErrorHandler: Result.() -> Unit = {
        error { e, _ ->
            alert("$e", "Second Request Error!") {
                positiveButton("Ok I know") { dismiss() }
            }.show()
        }
    }

    fun test6() = launch {
        val repo1Deferred = async { service.repo(rightId).await() }
        val repo2Deferred = async { service.repo(wrongId).await(myErrorHandler) }
        val repo1 = repo1Deferred.await()
        toast("First Request Done!")
        val repo2 = repo2Deferred.await()
        toast("Not gonna toast")
    }
}