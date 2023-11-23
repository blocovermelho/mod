package org.blocovermelho.mod.api.models

import kotlinx.serialization.Serializable
import org.blocovermelho.mod.UUIDSerializer
import java.util.*

@Serializable
data class Server(
    @Serializable(with = UUIDSerializer::class) val uuid: UUID,
    val name: String,
    val supportedVersions: List<String>,
    val currentModpack: Modpack?,
    val places: List<Place>,
    val available: Boolean
)

@Serializable
data class CreateServer(
    val name: String,
    val supportedVersions: List<String>,
    val currentModpack: Modpack?,
)

@Serializable
data class Modpack(
    val name: String,
    val source: ModpackSource,
    val version: String,
    val uri: String
) {
    enum class ModpackSource {
        Modrinth, Curseforge, Other
    }
}

@Serializable
data class Place(
    val pos: Pos,
    val name: String,
    val tags: List<String>
)


