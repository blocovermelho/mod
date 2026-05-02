package org.blocovermelho.bvauth.api.routes

import org.blocovermelho.bvauth.impl.ApiClient

object Root {
    const val BASE_PATH = "/"
    suspend fun GetVersionRanges(versions: List<String>) =
        ApiClient.Post<List<String>, List<String>>("${BASE_PATH}get_version_ranges", versions)

    suspend fun BadNames() =
        ApiClient.Get<List<String>>("${BASE_PATH}bad_names")

}