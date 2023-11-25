package org.blocovermelho.mod

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }
}

object DurationSerializer: KSerializer<Duration> {
    @Serializable
    data class RustDuration(val secs: Long, val nanos: Long);

    override val descriptor = PrimitiveSerialDescriptor("RUSTDURATION", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Duration {
        val rust = RustDuration.serializer().deserialize(decoder);
        return  rust.secs.seconds + rust.nanos.nanoseconds
    }

    override fun serialize(encoder: Encoder, value: Duration) {
        val rust = RustDuration(value.inWholeSeconds, 0)
        RustDuration.serializer().serialize(encoder, rust)
    }
}
