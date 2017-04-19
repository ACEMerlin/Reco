package com.github.acemerlin.reco

import kotlinx.coroutines.experimental.Job

/**
 * Created by merlin on 17/04/2017.
 */
interface WithJob {
    val job: Job
}