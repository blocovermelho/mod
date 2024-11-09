package org.blocovermelho.mod.async

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import org.blocovermelho.mod.BVQuilt
import java.util.concurrent.Executors

object CoroutineManager {
    lateinit var dispatcher: CoroutineDispatcher;
    lateinit var scope: CoroutineScope;

    fun init() {
        // Code from: Lnet/minecraft/util/Util;createIoWorker()Ljava/util/concurrent/ExecutorService;
        // This is how mojang handles IO threads for each new player join.
        // It should make things run on a separate thread instead of blocking main since it is a separate thread group.
        dispatcher = Executors.newCachedThreadPool {
            val thread = Thread(it)
            thread.name = "BV-Thread-Handler"
            thread
        }.asCoroutineDispatcher()
        scope = CoroutineScope(SupervisorJob() + dispatcher)
        BVQuilt.LOGGER.info("Initialized Coroutine Manager.")
    }
}
