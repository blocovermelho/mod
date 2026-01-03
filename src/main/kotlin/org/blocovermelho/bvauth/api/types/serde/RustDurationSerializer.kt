package org.blocovermelho.bvauth.api.types.serde

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind.STRING
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

object RustDurationSerializer : KSerializer<Duration> {
    @Serializable
    data class RustDuration(val secs: Long, val nanos: Long)

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("RUSTDURATION", STRING)

    override fun deserialize(decoder: Decoder): Duration {
        val rust = RustDuration.serializer().deserialize(decoder)
        return rust.secs.seconds + rust.nanos.nanoseconds
    }

    override fun serialize(encoder: Encoder, value: Duration) {
        val rust = RustDuration(value.inWholeSeconds, 0)
        RustDuration.serializer().serialize(encoder, rust)
    }
}