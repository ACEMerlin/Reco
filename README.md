Reco
============

Android Retrofit coroutine extension with easy to use api.

Use `launch` to start a coroutine within `UI` context
```Kotlin
launch {
    toast("I can do ui stuff here")
}
```

Use `async` to start a coroutine within `CommonPool` context(another thread)
```Kotlin
async {
    //toast("I can't do ui stuff here, coz i'm in worker thread")
}
```

Use `await` to *get* result from Retrofit calls:
```Kotlin
launch {
    val repo = service.repo(id).await()
    toast("Here is the result: $repo")
}
```

No ugly callback anymore，booya!

How to Use
-----------

All examples can be found at [MainActivity][1]，Just run `app` and check it out（I use Anko just for demonstration purpose）。There are several examples:

#### Make Sequential Requests:

```Kotlin
launch {
    val repo1 = service.repo(rightId).await()
    toast("First Request Done!\nYou can do things with repo1 now")
    val repo2 = service.repo(rightId).await()
    toast("Second Request Done!\nYou can do things with repo2 now")
}
```

#### Make Concurrent Requests

```Kotlin
launch {
    val repo1Deferred = async { service.repo(rightId).await() }
    val repo2Deferred = async { service.repo(rightId).await() }
    val repo1 = repo1Deferred.await()
    val repo2 = repo2Deferred.await()
    toast("Both Request Done!\nYou can do things with them now")
}
```

#### Request with custom handlers

```Kotlin
launch {
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
```

#### Failed request with error handler

```Kotlin
launch {
    val repo = service.repo(wrongId).await {
        error { e, _ ->
            //here we at main thread again
            alert("$e", "Error!") {
                positiveButton("Ok I know") { dismiss() }
            }.show()
        }
    }
    //This line will never run, because the request failed
    toast("Request Done!\nYou can do things with repo now")
}
```

#### Two Sequential Requests with one of them failed

```Kotlin
launch {
    val repo1 = service.repo(wrongId).await()
    toast("Not gonna toast coz repo1 failed")
    val repo2 = service.repo(rightId).await()
    toast("Not gonna toast")
}
```

#### Two Concurrent Requests with one of them failed

```Kotlin
val myErrorHandler: Result.() -> Unit = {
    error { e, _ ->
        alert("$e", "Second Request Error!") {
            positiveButton("Ok I know") { dismiss() }
        }.show()
    }
}

launch {
    val repo1Deferred = async { service.repo(rightId).await() }
    val repo2Deferred = async { service.repo(wrongId).await(myErrorHandler) }
    val repo1 = repo1Deferred.await()
    toast("First Request Done!")
    val repo2 = repo2Deferred.await()
    toast("Not gonna toast")
}
```

#### Handle Coroutine Cancellation along with Activity Lifecycle

First implement your Activity with `WithJob` interface.
```Kotlin
class MainActivity : AppCompatActivity(), WithJob {
    ...
    override val job = Job()
    ...
}
```

Then you can cancel `job` when activity is destroyed. We don't want to keep coroutines working while activity is gone because it will lead to weird bugs.

You can cancel Activity's `job` whenever you want. Example app demonstrate that by adding a button to explicitly cancel Activity's `job`. 

Install
----------

Add it in your root build.gradle at the end of repositories:
```groovy
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

Then add the dependency
```groovy
dependencies {
	compile 'com.github.ACEMerlin:Reco:v1.0-beta'
}
```

  [1]: https://github.com/ACEMerlin/Reco/blob/master/app/src/main/kotlin/com/github/acemerlin/ktest/MainActivity.kt