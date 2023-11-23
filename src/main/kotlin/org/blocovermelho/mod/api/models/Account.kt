package org.blocovermelho.mod.api.models

import kotlinx.serialization.Serializable
import org.blocovermelho.mod.UUIDSerializer
import java.util.*

@Serializable
data class CreateAccount(
    @Serializable(with = UUIDSerializer::class) val uuid: UUID,
    val password: String,
    val ip: String
)

@Serializable
data class ChangePassword(
    @Serializable(with = UUIDSerializer::class) val uuid: UUID,
    val ip: String,
    val old: String,
    val new: String
)

@Serializable
data class LoginRequest(
    @Serializable(with = UUIDSerializer::class) val uuid: UUID,
    val ip: String,
    val password: String
)
