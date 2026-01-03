package org.blocovermelho.bvauth.ext

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

fun String.UrlEncode() : String {
    return URLEncoder.encode(this,StandardCharsets.UTF_8)
}