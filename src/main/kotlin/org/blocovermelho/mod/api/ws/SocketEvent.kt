package org.blocovermelho.mod.api.ws

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.blocovermelho.mod.api.models.User
import org.blocovermelho.mod.api.ws.messages.LinkResponse
import org.blocovermelho.mod.api.ws.messages.LinkResult
import java.util.UUID


@Serializable

sealed class SocketEvent {
    @Serializable
    @SerialName("error")
    class ERROR(val event: String, val data: SocketError): SocketEvent()

    @Serializable
    @SerialName("link_request")
    class LINK_REQUEST(val event: String, val data: String): SocketEvent()
    @Serializable
    @SerialName("link_response")
    class LINK_RESPONSE(val event: String, val data: LinkResult): SocketEvent()

    @Serializable
    @SerialName("cidr_syn")
    class CIDR_SYN(val event: String, val data: String): SocketEvent()
    @Serializable
    @SerialName("cidr_awk")
    class CIDR_AWK(val event: String, val data: String): SocketEvent()
    @Serializable
    @SerialName("cidr_syn_awk")
    class CIDR_SYN_AWK(val event: String,val data: User) : SocketEvent()
}


@Serializable
data class SocketError(@SerialName("source_event") val source : String, val error: String) {
}

object SocketEventBuilders {
    fun linkRequest(playerUUID: UUID) : SocketEvent.LINK_REQUEST =
        SocketEvent.LINK_REQUEST("link_request", playerUUID.toString());
    object CIDR {
        fun awk(nonce: String): SocketEvent.CIDR_AWK =
            SocketEvent.CIDR_AWK("cidr_awk", nonce);
    }
}

object SocketEventSerializer : JsonContentPolymorphicSerializer<SocketEvent>(SocketEvent::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<SocketEvent> = when {
        "event" in element.jsonObject -> {
            val type = element.jsonObject["event"]!!;
            when (val kind = type.jsonPrimitive.content) {
                "error" -> SocketEvent.ERROR.serializer()
                "link_request" -> SocketEvent.LINK_REQUEST.serializer()
                "link_response" -> SocketEvent.LINK_RESPONSE.serializer()
                "cidr_syn" -> SocketEvent.CIDR_SYN.serializer()
                "cidr_awk" -> SocketEvent.CIDR_AWK.serializer()
                "cidr_syn_awk" -> SocketEvent.CIDR_SYN_AWK.serializer()
                else -> throw SerializationException("Unknown event: $kind")
            }
        }
        else -> throw UnsupportedOperationException()
    }

}
