package org.blocovermelho.bvauth.ext

import java.net.InetSocketAddress
import java.net.SocketAddress

fun SocketAddress.getIpString(): String {
    return (this as InetSocketAddress).address.hostAddress
}

