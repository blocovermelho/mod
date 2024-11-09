package org.blocovermelho.mod

import kotlinx.serialization.*
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.*
import org.blocovermelho.mod.api.models.*
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


object ServerJoinSerializer: JsonContentPolymorphicSerializer<ServerJoin>(ServerJoin::class) {
    override val descriptor = PrimitiveSerialDescriptor("RUSTENUM_SERVERJOIN", PrimitiveKind.STRING)
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ServerJoin> = when(element) {
        is JsonPrimitive -> FirstJoinSerializer
        is JsonObject -> ResumeSerializer
        else -> FirstJoinSerializer
    }
}

object FirstJoinSerializer: KSerializer<FirstJoin> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("RUSTENUM_FIRSTJOIN", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): FirstJoin {
        decoder.decodeString()
        return FirstJoin
    }

    override fun serialize(encoder: Encoder, value: FirstJoin) {
        encoder.encodeString("FirstJoin")
    }
}

@OptIn(InternalSerializationApi::class)
object ResumeSerializer: KSerializer<Resume> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Resume") {
        element<Viewport>("Resume")
    }


    override fun deserialize(decoder: Decoder): Resume {
        return decoder.decodeStructure(descriptor) {
            val viewport = decodeSerializableElement(descriptor, 0, Viewport::class.serializer())
            Resume(viewport)
        }
    }

    override fun serialize(encoder: Encoder, value: Resume) {
        return encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, Viewport::class.serializer(), value.viewport)
        }
    }

}
