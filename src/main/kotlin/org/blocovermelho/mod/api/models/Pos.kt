package org.blocovermelho.mod.api.models

import kotlinx.serialization.Serializable

@Serializable
data class Loc(val x: Double, val y: Double, val z: Double, val dim: String)

@Serializable
data class Viewport(val loc: Loc, val yaw: Double, val pitch: Double)
