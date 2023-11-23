package org.blocovermelho.mod.api.models

import kotlinx.serialization.Serializable

@Serializable
data class Pos(val x: Int, val z: Int, val y: Int, val dim: String)
