package org.blocovermelho.mod.api.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.blocovermelho.mod.*
import java.util.*

@Serializable
data class Server(
    @Serializable(with = UUIDSerializer::class) val uuid: UUID,
    val name: String,
    @SerialName("supported_versions")
    val supportedVersions: List<String>,
    @SerialName("current_modpack")
    val currentModpack: Modpack?,
    val online: Boolean,
    val players: List<@Serializable(with = UUIDSerializer::class) UUID>
)

@Serializable
data class CreateServer(
    val name: String,
    @SerialName("supported_versions")
    val supportedVersions: List<String>,
    @SerialName("current_modpack")
    val currentModpack: Modpack?,
)

@Serializable(with = ServerJoinSerializer::class)
sealed class ServerJoin

data class Resume(val viewport: Viewport) : ServerJoin()

object FirstJoin : ServerJoin()

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

