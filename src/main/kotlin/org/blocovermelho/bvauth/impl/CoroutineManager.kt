package org.blocovermelho.bvauth.impl

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import org.blocovermelho.bvauth.BvAuthMod
import java.util.concurrent.Executors

object CoroutineManager {
    val dispatcher: CoroutineDispatcher = Executors.newCachedThreadPool {
        val thread = Thread(it)
        thread.name = "BV-Thread-Handler"
        thread
    }.asCoroutineDispatcher()
    val supervisor = SupervisorJob()
    val scope: CoroutineScope = CoroutineScope(supervisor + dispatcher)

    init {
        BvAuthMod.Logger.info("Initialized Coroutine Manager.")
    }
}