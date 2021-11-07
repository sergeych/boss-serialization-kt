@file:UseSerializers(ZonedDateTimeSerializer::class)

package net.sergeych.boss_serialization

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.internal.NamedValueDecoder
import kotlinx.serialization.modules.EmptySerializersModule
import net.sergeych.boss.Boss
import net.sergeych.utils.Bytes
import java.time.ZonedDateTime

@ExperimentalSerializationApi
@OptIn(InternalSerializationApi::class)
class BossDecoder(
    private val currentObject: Map<String, Any?>,
    descriptor: SerialDescriptor,
) : NamedValueDecoder() {

    private var currentIndex = 0
    private val isCollection = descriptor.kind == StructureKind.LIST || descriptor.kind == StructureKind.MAP
    private val size = if (isCollection) Int.MAX_VALUE else descriptor.elementsCount

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        return when (descriptor.kind) {
            is StructureKind.LIST -> BossListDecoder(currentObject[currentTag] as List<Any?>)
            is StructureKind.CLASS -> currentTagOrNull?.let {
                checkTagIsStored(it)
                BossDecoder(currentObject[it] as Map<String, Any?>, descriptor)
            } ?: this
            else -> throw SerializationException("unsupported king: ${descriptor.kind}")
        }
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        while (currentIndex < size) {
            val name = descriptor.getTag(currentIndex++)
            if (name in currentObject)
                return currentIndex - 1
            if (isCollection)
                break
        }
        return CompositeDecoder.DECODE_DONE
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T =
        when (deserializer.descriptor) {
            ZonedDateTimeSerializer.descriptor -> decodeTaggedValue(currentTag) as T
            byteArraySerializerDescriptor -> (decodeTaggedValue(currentTag) as Bytes).toArray() as T
            bossStructSerializerDescriptor -> {
                BossStruct(decodeTaggedValue(currentTag) as Map<String, @Contextual Any?>) as T
            }
            else -> super.decodeSerializableValue(deserializer)
        }

    override fun decodeTaggedNotNullMark(tag: String): Boolean =
        tag in currentObject && currentObject[tag] != null

    override fun decodeTaggedValue(tag: String): Any {
        checkTagIsStored(tag)
        return currentObject[tag]!!
    }

    override fun decodeTaggedFloat(tag: String): Float {
        return (decodeTaggedValue(tag) as Number).toFloat()
    }

    private fun checkTagIsStored(tag: String) {
        if (tag !in currentObject) throw SerializationException("missing property $tag")
    }

    companion object {
        internal val byteArraySerializerDescriptor = serializer<ByteArray>().descriptor
        internal val bossStructSerializerDescriptor = serializer<BossStruct>().descriptor
    }
}


@OptIn(ExperimentalSerializationApi::class)
internal class BossListDecoder(
    private val source: List<Any?>,
) : AbstractDecoder() {

    override val serializersModule = EmptySerializersModule

    private val values = source.iterator()
    private val size = source.size
    private var currentIndex = -1

    private var useCachedValue = false
    private var cache: Any? = null

    override fun decodeSequentially(): Boolean = true

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = size

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int =
        if (values.hasNext()) ++currentIndex else CompositeDecoder.DECODE_DONE

    override fun decodeValue(): Any {
        return if (useCachedValue) {
            useCachedValue = false
            cache!!
        } else values.next()!!
    }

    override fun decodeNotNullMark(): Boolean {
        useCachedValue = true
        cache = values.next()
        return cache != null
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T =
    when (deserializer.descriptor) {
        ZonedDateTimeSerializer.descriptor -> decodeValue() as T
        BossDecoder.byteArraySerializerDescriptor -> (decodeValue() as Bytes).toArray() as T
        else -> super.decodeSerializableValue(deserializer)
    }


    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        if (!values.hasNext())
            throw SerializationException("expected serialized class data missing")
        return when (descriptor.kind) {
            StructureKind.CLASS -> BossDecoder(values.next() as Map<String, Any?>, descriptor)
            StructureKind.LIST -> BossListDecoder(values.next() as List<Any?>)
            else -> throw SerializationException("unsupported kind: ${descriptor.kind}")
        }
    }
}
