package org.blocovermelho.mod.events

import org.blocovermelho.mod.BVQuilt
import org.blocovermelho.mod.api.Routes
import org.blocovermelho.mod.api.handleErr
import org.blocovermelho.mod.api.models.CreateServer
import org.blocovermelho.mod.api.models.Modpack
import java.util.*

suspend fun createNewServer() {
    val uuid = UUID.fromString(BVQuilt.SERVER_DATA.id.value())

    if (uuid == UUID(0L, 0L)) {
        val pack = Modpack(
            BVQuilt.SERVER_DATA.modpack.name.value(),
            BVQuilt.SERVER_DATA.modpack.source.value(),
            BVQuilt.SERVER_DATA.modpack.version.value(),
            BVQuilt.SERVER_DATA.modpack.uri.value()
        )

        val s = Routes.Server.Create(CreateServer(BVQuilt.SERVER_DATA.name.value(), BVQuilt.SERVER_DATA.supportedVersions.value().toList(), pack)).handleErr {
            BVQuilt.LOGGER.error("Failed creating new server. Error: [${it.first.value} - ${it.first.description}] ${it.second.error} (${it.second.inner})")
        } ?: return

        BVQuilt.LOGGER.info("Created new server with UUID: ${s.uuid}")

        BVQuilt.SERVER_DATA.id.setValue(s.uuid.toString(), true)
        BVQuilt.Store.ServerUUID = s.uuid
    }
}
