package org.blocovermelho.bvauth.api.types.serde

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import org.blocovermelho.bvauth.api.types.ConnectionData

object PlaytimeSerializer : KSerializer<ConnectionData.Playtime> {
    private val mapSer = MapSerializer(UUIDSerializer, RustDurationSerializer)

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("playtime") {
            element("times", mapSer.descriptor)
        }

    override fun serialize(encoder: Encoder, value: ConnectionData.Playtime) {
        val jsonEncoder = encoder as? JsonEncoder ?: error("JSON only")
        val mapJson = jsonEncoder.json.encodeToJsonElement(mapSer, value.times)
        val obj = buildJsonObject {
            for ((k, v) in mapJson.jsonObject) {
                put(k, v)
            }
        }
        jsonEncoder.encodeJsonElement(obj)
    }

    override fun deserialize(decoder: Decoder): ConnectionData.Playtime {
        val jsonDecoder = decoder as? JsonDecoder ?: error("JSON only")
        val obj = jsonDecoder.decodeJsonElement().jsonObject
        val timesJson = buildJsonObject {
            obj.filterKeys { it != "kind" }.forEach { (k, v) -> put(k, v) }
        }
        val times = jsonDecoder.json.decodeFromJsonElement(mapSer, timesJson)
        return ConnectionData.Playtime(times)
    }
}
