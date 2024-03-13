package org.blocovermelho.mod.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.blocovermelho.mod.UUIDSerializer
import java.util.UUID

object Ban {
    @Serializable
    enum class Response {
        Existing,
        Merged,
        New,
        Invalid
    }

    @Serializable
    data class Manual(@SerialName("Manual") @Serializable(with=UUIDSerializer::class) val manual: UUID )
}
