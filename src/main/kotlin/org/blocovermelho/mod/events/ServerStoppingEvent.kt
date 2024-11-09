package org.blocovermelho.mod.events

import org.blocovermelho.mod.BVQuilt
import org.blocovermelho.mod.api.Routes
import java.util.*

suspend fun disableServer() {
    val uuid = UUID.fromString(BVQuilt.SERVER_DATA.id.value())
    if (uuid == UUID(0L, 0L)) {
        return
    }

    Routes.Server.Disable(uuid)
}
