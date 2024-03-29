@file:UseSerializers(ZonedDateTimeSerializer::class)
@file:Suppress("EXPERIMENTAL_IS_NOT_ENABLED")

package net.sergeych.boss_serialization

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.internal.NamedValueEncoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import net.sergeych.boss.Boss

/**
 * Please do not instantiate this class directly. Use instead a companion object's [encode] method
 * or extension function on Boss.Writer [Boss.Writer.encode]
 */
@OptIn(InternalSerializationApi::class)
@ExperimentalSerializationApi
class BossEncoder(private val currentObject: MutableMap<String, Any?>) : NamedValueEncoder() {

    override val serializersModule: SerializersModule = EmptySerializersModule

    override fun encodeTaggedNull(tag: String) {
        currentObject[tag] = null
    }

    override fun encodeTaggedEnum(tag: String, enumDescriptor: SerialDescriptor, ordinal: Int) {
        currentObject[tag] = ordinal
    }

    override fun encodeTaggedValue(tag: String, value: Any) {
        currentObject[tag] = value
    }

    override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
        when (serializer.descriptor) {
            BossDecoder.bossStructSerializerDescriptor,
            ZonedDateTimeSerializer.descriptor,
            BossDecoder.byteArraySerializerDescriptor -> {
                currentObject[currentTag] = value
                popTag()
            }
            else ->
                super.encodeSerializableValue(serializer, value)
        }
    }

    override fun beginCollection(
        descriptor: SerialDescriptor,
        collectionSize: Int,
    ): CompositeEncoder {
        return currentTagOrNull?.let { tag ->
            BossListEncoder(ArrayList<Any?>().also {
                currentObject[tag] = it
                popTag()
            })
        }
            ?: throw SerializationException("can't encode lists as root object (root must be a class)")
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        return currentTagOrNull?.let { tag -> BossEncoder(BossStruct().also { currentObject[tag] = it }) }
            ?: this
    }

    companion object {
        /**
         * Encode some `@Serializable` value to a packed binary boss data
         */
        inline fun <reified T> encode(value: T): ByteArray =
            Boss.Writer().encode(value).toByteArray()

        /**
         * Encode some `@Serializable` value into a [BossStruct], a map wrap (thus properly seializable
         * itself with this encoder), in the form that could be serialized with low-level boss packer or
         * deserialized from it using matching [BossDecoder.decodeFrom] method.
         */
        inline fun <reified T: Any> encodeToStruct(value: T): BossStruct =
            encodeToStruct(T::class.java, value)

        fun <T: Any> encodeToStruct(cls: Class<T>,value: T): BossStruct =
            if (value is Map<*, *>)
                BossStruct.from(value)
            else
                BossStruct().also {
                    BossEncoder(it).encodeSerializableValue(
                        EmptySerializersModule.serializer(cls),
                        value
                    )
                }
    }
}

/**
 * Encode and write object to a `Boss.Writer`
 */
@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> Boss.Writer.encode(value: T): Boss.Writer {
    if (value is BossStruct)
        writeObject(value)
    else {
        val serializer: KSerializer<T> = EmptySerializersModule.serializer<T>()
        val bs = BossStruct()
        BossEncoder(bs).encodeSerializableValue(serializer, value)
        writeObject(bs)
    }
    return this
}


@OptIn(ExperimentalSerializationApi::class)
internal class BossListEncoder(private val collection: MutableList<Any?>) : AbstractEncoder() {

    override val serializersModule = EmptySerializersModule

    override fun encodeValue(value: Any) {
        collection.add(value)
    }

    override fun encodeNull() {
        collection.add(null)
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        collection.add(index)
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        return BossEncoder(BossStruct().also { collection.add(it) })
    }

    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        return BossListEncoder(ArrayList<Any?>().also { collection.add(it) })
    }
}