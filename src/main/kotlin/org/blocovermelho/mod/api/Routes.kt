package org.blocovermelho.mod.api

import org.blocovermelho.mod.api.models.*
import java.util.*

object Routes {
    object Auth {
        val BASE_PATH = "/auth"
        suspend fun Exists(uuid: UUID): HttpResult<Boolean> = BVClient.Get(BASE_PATH + "/exists?uuid=$uuid")

        suspend fun Create(account: CreateAccount): HttpResult<Boolean> = BVClient.Post(BASE_PATH, account)

        suspend fun Delete(uuid: UUID): HttpResult<Boolean> = BVClient.Delete(BASE_PATH + "/$uuid")

        suspend fun ChangePassword(changePassword: ChangePassword): HttpResult<Boolean> =
            BVClient.Patch(BASE_PATH + "/changepw", changePassword)

        suspend fun Resume(playerUUID: UUID): HttpResult<Boolean> =
            BVClient.Patch<Boolean, Any>(BASE_PATH + "/$playerUUID/resume")

        suspend fun Login(serverUUID: UUID, loginRequest: LoginRequest): HttpResult<Boolean> =
            BVClient.Post(BASE_PATH + "/$serverUUID/login", loginRequest)

        suspend fun Logoff(serverUUID: UUID, userUUID: UUID, userIP: String, pos: Pos): HttpResult<Boolean> =
            BVClient.Post(BASE_PATH + "/$serverUUID/logoff?uuid=$userUUID&ip=$userIP", pos)

        suspend fun Session(userUUID: UUID, userIP: String): HttpResult<Boolean> =
            BVClient.Get(BASE_PATH + "/session?uuid=$userUUID&ip=$userIP")
    }

    object Server {
        val BASE_PATH = "/server"
        suspend fun Disable(uuid: UUID): HttpResult<Boolean> =
            BVClient.Patch<Boolean, Any>(BASE_PATH + "/$uuid/disable")

        suspend fun Enable(uuid: UUID): HttpResult<Boolean> = BVClient.Patch<Boolean, Any>(BASE_PATH + "/$uuid/enable")

        suspend fun Create(server: CreateServer): HttpResult<org.blocovermelho.mod.api.models.Server> =
            BVClient.Post(BASE_PATH, server)

        suspend fun Delete(uuid: UUID): HttpResult<org.blocovermelho.mod.api.models.Server> = BVClient.Delete(BASE_PATH + "/$uuid")

        suspend fun Get(uuid: UUID): HttpResult<org.blocovermelho.mod.api.models.Server> =
            BVClient.Get(BASE_PATH + "/$uuid")
    }


    object User {
        val BASE_PATH = "/user"

        suspend fun Exists(uuid: UUID): HttpResult<Boolean> = BVClient.Get(BASE_PATH + "/exists?uuid=$uuid")

        suspend fun Create(user: CreateUser): HttpResult<org.blocovermelho.mod.api.models.User> =
            BVClient.Post(BASE_PATH, user)

        suspend fun Delete(uuid: UUID): HttpResult<org.blocovermelho.mod.api.models.User> =
            BVClient.Delete(BASE_PATH + "/$uuid")

        suspend fun Get(uuid: UUID): HttpResult<org.blocovermelho.mod.api.models.User> =
            BVClient.Get(BASE_PATH + "/$uuid")

    }

    object Discord {
        val BASE_PATH = "/oauth"

        suspend fun GetLink(uuid: UUID): HttpResult<String> = BVClient.Get(BASE_PATH + "?uuid=$uuid")
    }
}
