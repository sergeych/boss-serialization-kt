@file:Suppress("EXPERIMENTAL_IS_NOT_ENABLED")

package net.sergeych.boss_serialization

import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.sergeych.boss.Boss
import net.sergeych.utils.Bytes
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

/**
 * Serialization for ZonedDateTime. In Boss serialization, it falls back to Boss-native datetime type, otherwise
 * serializes to long with unix epoch second. The time is always truncated to second (as stated, unix
 * epoch second).
 *
 * Please use this serializer explicitly, for example by adding
 * ```
 * @file:UseSerializers(ZonedDateTimeSerializer::class)
 * ```
 * to the top of any file that serializes `ZonedDateTime`. Using any other serializer for this type
 * may break binary compatibility with other Boss consumers.
 */
object ZonedDateTimeSerializer : KSerializer<ZonedDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ZDT", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: ZonedDateTime) =
        encoder.encodeLong(value.toEpochSecond())

    override fun deserialize(decoder: Decoder): ZonedDateTime {
        val i = Instant.ofEpochSecond(decoder.decodeLong())
        return ZonedDateTime.ofInstant(i, ZoneOffset.UTC)
    }
}

/**
 * Decode boss object from this binary data
 */
@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> ByteArray.decodeBoss(): T = BossDecoder.decodeFrom(this)

/**
 * Convenience method: decode boss binary data to struct.
 */
fun ByteArray.decodeBossStruct(): BossStruct = BossStruct(Boss.load(this) as MutableMap<String, Any?>)

/**
 * read and deserialize object from boss reader
 */
@Suppress("unused")
@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> Boss.Reader.deserialize(): T = BossDecoder.decodeFrom(this)

/**
 * ASCII dump representation for a binary data, with address, hex and ascii fields, following the
 * old tradition
 */
fun ByteArray.dump(): String =
    Bytes(this).toDump()

/**
 * Wrap for Map<String,Any?> to allow de/serialization of the Maps with arbitrary content. Due to some
 * architectural limitations of `kotlinx.serialization` module, this is the only way to include such
 * map fields in the objects to be de/serialized, otherwise it won't be processed properly. It also has
 * few convenience methods to access typed data.
 */
@Serializable
@Suppress("UNCHECKED_CAST")
class BossStruct(private val __source: MutableMap<String, @Contextual Any?> = HashMap()) :
    MutableMap<String, Any?> by __source {
    /**
     * Get the element as a BossStruct, creating if necessary, as wrap around any other Map that
     * presents. E.g. if you need a BossStruct, whatever data is held here (e.g. Boss.Dictionary, is most often
     * used while decoding boss binaries), use this method.
     */
    @Suppress("unused")
    fun getStruct(key: String): BossStruct? = get(key)?.let {
        if( it is BossStruct ) it else BossStruct(it as MutableMap<String, Any?>)
    }

    /**
     * Get and cast to a given type. Utility method.
     */
    fun <T> getAs(key: String): T = get(key) as T
    override fun toString(): String {
        return __source.toString()
    }
}
