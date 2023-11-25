package org.blocovermelho.mod.api.models

import kotlinx.serialization.Serializable
import org.blocovermelho.mod.DurationSerializer

import org.blocovermelho.mod.UUIDSerializer
import java.util.*
import kotlin.time.Duration

@Serializable
data class User(
    @Serializable(with = UUIDSerializer::class) val uuid: UUID,
    val username: String,
    val discordId: String,
    val pronouns: List<String>,
    @Serializable(with = UUIDSerializer::class) val lastServer: UUID?,
    val lastPos: Map<@Serializable(with = UUIDSerializer::class) UUID, Pos>,
    val playtime: Map<@Serializable(with = UUIDSerializer::class) UUID, @Serializable(with = DurationSerializer::class) Duration>
)

@Serializable
data class CreateUser(
    @Serializable(with = UUIDSerializer::class) val uuid: UUID,
    val username: String,
    val discordId: String
)
