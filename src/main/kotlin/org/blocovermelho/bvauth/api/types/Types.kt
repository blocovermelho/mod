package org.blocovermelho.bvauth.api.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.blocovermelho.bvauth.api.types.serde.PlaytimeSerializer
import org.blocovermelho.bvauth.api.types.serde.UUIDSerializer
import java.util.*
import kotlin.time.Duration
import kotlin.time.Instant

@Serializable
sealed class Login {
    @Serializable
    @SerialName("new_ip")
    object NewIp : Login()

    @Serializable
    @SerialName("allowed_ip")
    object AllowedIp : Login()

    @Serializable
    @SerialName("banned_ip")
    object BannedIp : Login()

    @Serializable
    @SerialName("blocked_ip")
    object BlockedIp : Login()
}

@Serializable
data class KeepAlive(val players: List<String>, val motd: String?)

@Serializable
sealed class Logout {
    @Serializable
    @SerialName("server_offline")
    object ServerOffline : Logout()

    @Serializable
    @SerialName("profile_not_in_server")
    object ProfileNotInServer : Logout()

    @Serializable
    @SerialName("logged_out")
    object LoggedOut : Logout()
}

@Serializable
sealed class Authenticate {
    @Serializable
    @SerialName("server_offline")
    object ServerOffline : Authenticate()

    @Serializable
    @SerialName("invalid_password")
    data class InvalidPassword(val attempts: Int, val maxAttempts: Int) : Authenticate()

    @Serializable
    @SerialName("invalid_profile")
    object InvalidProfile : Authenticate()

    @Serializable
    @SerialName("logged_in")
    object LoggedIn : Authenticate()
}

@Serializable
sealed class PasswordUpdate {
    @Serializable
    @SerialName("server_offline")
    object ServerOffline : PasswordUpdate()

    @Serializable
    @SerialName("profile_not_in_server")
    object ProfileNotInServer : PasswordUpdate()

    @Serializable
    @SerialName("invalid_player_state")
    object InvalidPlayerState : PasswordUpdate()

    @Serializable
    @SerialName("invalid_password")
    object InvalidPassword : PasswordUpdate()

    @Serializable
    @SerialName("password_changed")
    object PasswordChanged : PasswordUpdate()
}

@Serializable
sealed class CreateProfile {
    @Serializable
    @SerialName("username_exists")
    object UsernameExists : CreateProfile()

    @Serializable
    @SerialName("created")
    data class Created(@Serializable(with = UUIDSerializer::class) val id: UUID) : CreateProfile()
}

@Serializable
data class Server(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    val name: String,
    val game: String,
    val versions: List<String>,
    val maxPlayers: Int,
    val staff: List<Profile>
)

@Serializable
data class Profile(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    val username: String,
    val discordId: String,
    val connections: List<Connection>
)

@Serializable
data class NewProfile(
    val password: String,
    val discordId: String
)

@Serializable
data class Connection(
    @Serializable(with = UUIDSerializer::class) val issuer: UUID?, val extra: ConnectionData
)

@Serializable
sealed class ConnectionData {
    @Serializable
    @SerialName("bedrock_username")
    data class BedrockUsername(val name: String, val xuid: Long?) : ConnectionData()

    @Serializable
    @SerialName("mojang_uuid")
    data class MojangUuid(
        val name: String, @Serializable(with = UUIDSerializer::class) val id: UUID
    ) : ConnectionData()

    @Serializable(with = PlaytimeSerializer::class)
    @SerialName("playtime")
    data class Playtime(val times: Map<UUID, Duration>) :
        ConnectionData()
}

@Serializable
sealed class WebSocketMessage() {
    @Serializable
    @SerialName("discord_link")
    data class DiscordLink(
        val username: String,
        val discordId: String,
        val discordHandle: String,
        val isMember: Boolean,
        val memberSince: Instant? = null,
        val extras: DiscordExtras? = null
    ) : WebSocketMessage()
}



@Serializable
data class DiscordExtras(
    val nickname: String? = null,
    val roleColor: String? = null,
    val roleName: String? = null
)

@Serializable
sealed class MojangAccountStanding {
    @Serializable
    @SerialName("known_profile")
    data class KnownProfile(val profile: Profile) : MojangAccountStanding()

    @Serializable
    @SerialName("renamed_profile")
    data class RenamedProfile(val profile: Profile, val mojangName: String) : MojangAccountStanding()

    @Serializable
    @SerialName("unknown_user")
    data class UnknownUser(@Serializable(with = UUIDSerializer::class) val mojangUuid: UUID, val mojangName: String) :
        MojangAccountStanding()

    @Serializable
    @SerialName("invalid_name")
    object InvalidName : MojangAccountStanding()
}

@Serializable
sealed class BedrockAccountStanding {
    @Serializable
    @SerialName("known_profile")
    data class KnownProfile(val profile: Profile) : BedrockAccountStanding()

    @Serializable
    @SerialName("renamed_profile")
    data class RenamedProfile(val profile: Profile, val gamertag: String) : BedrockAccountStanding()

    @Serializable
    @SerialName("unknown_user")
    data class UnknownUser(val gamertag: String, val xuid: Long?) : BedrockAccountStanding()
}
