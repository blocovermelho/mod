package org.blocovermelho.mod.api.models

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.blocovermelho.mod.DurationSerializer

import org.blocovermelho.mod.UUIDSerializer
import java.util.*
import kotlin.time.Duration

@Serializable
data class User(
    @Serializable(with = UUIDSerializer::class) val uuid: UUID,
    val username: String,
    @SerialName("discord_id")
    val discordId: String,
    @SerialName("created_at")
    val createdAt: Instant,
    val pronouns: List<Pronoun>,
    @SerialName("last_server")
    @Serializable(with = UUIDSerializer::class) val lastServer: UUID?
)

@Serializable
data class Pronoun (val pronoun: String, val color: String)

@Serializable
data class CreateUser(
    @Serializable(with = UUIDSerializer::class) val uuid: UUID,
    val username: String,
    @SerialName("discord_id")
    val discordId: String
)
