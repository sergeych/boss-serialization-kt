package net.sergeych.boss_serialization

import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.serializer
import net.sergeych.boss.Boss
import net.sergeych.utils.Bytes
import java.io.Serial
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

val bossSerializersModule = EmptySerializersModule

object ZonedDateTimeSerializer : KSerializer<ZonedDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ZDT", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: ZonedDateTime) =
        encoder.encodeLong(value.toEpochSecond())

    override fun deserialize(decoder: Decoder): ZonedDateTime {
        val i = Instant.ofEpochSecond(decoder.decodeLong())
        return ZonedDateTime.ofInstant(i, ZoneOffset.UTC)
    }
}

//val ZonedDateTime.descriptor get() = ZonedDateTimeSerializer.descriptor

inline fun <reified T> ByteArray.decodeBoss(): T {
    return Boss.Reader(this).deserialize()
}

inline fun ByteArray.decodeBossStruct(): BossStruct = BossStruct(Boss.load(this) as Map<String, Any?>)

inline fun <reified T> Boss.Reader.deserialize(): T {
    val d = bossSerializersModule.serializer<T>()
    val decoder = BossDecoder(this.readMap().toMap(),
        d.descriptor)
    return d.deserialize(decoder)
}

fun ByteArray.dump(): String =
    Bytes(this).toDump()

@Serializable
@Suppress("UNCHECKED_CAST")
class BossStruct(val __source: Map<String,@Contextual Any?> = HashMap()): Map<String,Any?> by __source {
    fun getStruct(key: String): BossStruct? = get(key)?.let { BossStruct(it as Map<String,Any?>) }
    fun <T>getAs(key: String): T = get(key) as T
    override fun toString(): String {
        return __source.toString()
    }
}
