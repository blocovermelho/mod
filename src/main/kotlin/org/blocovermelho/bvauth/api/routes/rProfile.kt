package org.blocovermelho.bvauth.api.routes

import org.blocovermelho.bvauth.api.types.*
import org.blocovermelho.bvauth.ext.UrlEncode
import org.blocovermelho.bvauth.impl.ApiClient
import org.blocovermelho.bvauth.impl.expect
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*


object rProfile {
    const val BASE_PATH = "/profile"
    suspend fun Get(username: String) =
        ApiClient.Get<Profile>("$BASE_PATH?username=$username")

    suspend fun ResolveMojang(username: String) =
        ApiClient.Get<MojangAccountStanding>("$BASE_PATH/resolve_mojang?username=$username")

    // NOTE: XUID resolution isn't yet implemented. Omitted.
    suspend fun ResolveBedrock(gamertag: String) =
        ApiClient.Get<BedrockAccountStanding>("$BASE_PATH/resolve_bedrock?gamertag=$gamertag")

    suspend fun ConnectMojang(username: String, id: UUID) =
        ApiClient.Post<Connection, Any>("$BASE_PATH/$username/mojang?id=$id")

    // NOTE: XUID resolution isn't yet implemented. Omitted.
    suspend fun ConnectBedrock(username: String, gamertag: String) =
        ApiClient.Post<Connection, Any>("$BASE_PATH/$username/bedrock/?gamertag=$gamertag")

    suspend fun Authenticate(username: String, ip: String, password: String) =
        ApiClient.Post<Authenticate, Any>("$BASE_PATH/$username/authenticate?ip=$ip&password=${password.UrlEncode()}")

    suspend fun Login(username: String, ip: String) =
        ApiClient.Post<Login, Any>("$BASE_PATH/$username/login?ip=$ip")

    suspend fun Logout(username: String) =
        ApiClient.Post<Logout, Any>("$BASE_PATH/$username/logout")

    suspend fun SessionRestore(username: String) =
        ApiClient.Get<Boolean>("$BASE_PATH/$username/session").expect { "This route is infallible." }

    suspend fun PasswordChange(username: String, old: String, new: String) =
        ApiClient.Post<PasswordUpdate, Any>("$BASE_PATH/$username/password_change?old=$old&new=$new")

    suspend fun Create(username: String, newProfile: NewProfile) =
        ApiClient.Post<CreateProfile, NewProfile>("$BASE_PATH/$username", newProfile)

}
