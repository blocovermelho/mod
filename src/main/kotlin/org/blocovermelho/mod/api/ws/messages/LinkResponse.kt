package org.blocovermelho.mod.api.ws.messages

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.blocovermelho.mod.UUIDSerializer
import java.util.*

@Serializable
open class LinkResponse()

@Serializable
data class Exists(val exists: LinkResult) : LinkResponse()

@Serializable
data class Error(val error: String) : LinkResponse()

@Serializable
data class LinkResult(
    val discordId: String,
    val discordUsername: String,
    val _when : Instant,
    @Serializable(with = UUIDSerializer::class) val minecraftUuid: UUID
)


suspend fun LinkResponse.handleErr (f: suspend (Error) -> Unit) : LinkResult? {
    return when (this) {
        is Error -> {
            f(this)
            null
        }
        is Exists -> {
            this.exists
        }

        else -> null
    }
}
