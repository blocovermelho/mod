package org.blocovermelho.mod.api.ws.messages

import io.ktor.http.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.blocovermelho.mod.UUIDSerializer
import org.blocovermelho.mod.api.HttpFailure
import java.util.*

@Serializable
open class LinkResponse()

@Serializable
data class Exists(@SerialName("Exists") val exists: LinkResult) : LinkResponse()

@Serializable
data class Error(@SerialName("Error") val error: String) : LinkResponse() {
    fun into(): Pair<HttpStatusCode, HttpFailure> {
        return Pair(HttpStatusCode.InternalServerError, HttpFailure(error, null))
    }
}

@Serializable
data class LinkResult(
    @SerialName("discord_id")
    val discordId: String,
    @SerialName("discord_username")
    val discordUsername: String,
    @SerialName("when")
    val _when : Instant,
    @SerialName("minecraft_uuid")
    @Serializable(with = UUIDSerializer::class) val minecraftUuid: UUID
)


fun LinkResponse.handleErr (f: (Error) -> Unit) : LinkResult? {
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
