package org.blocovermelho.mod.api.models

import kotlinx.serialization.Serializable

object Cidr {
    @Serializable
    enum class Response {
        Allowed,
        Banned,
        Unknown
    }
}
