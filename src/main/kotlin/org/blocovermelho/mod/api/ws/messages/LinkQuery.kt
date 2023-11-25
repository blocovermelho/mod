package org.blocovermelho.mod.api.ws.messages

import kotlinx.serialization.Serializable
import org.blocovermelho.mod.UUIDSerializer
import java.util.UUID

@Serializable
data class LinkQuery(@Serializable(with = UUIDSerializer::class) val uuid: UUID)
