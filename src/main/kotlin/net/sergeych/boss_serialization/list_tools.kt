@file:Suppress("unused", "UNCHECKED_CAST")

package net.sergeych.boss_serialization

import net.sergeych.utils.Bytes

/**
 * Get element at index as Int or null if it is null.
 */
fun <T> List<T>.intAt(index: Int) = (get(index) as Number?)?.toInt()

/**
 * Get element at index as Int or null if it is null.
 */
fun <T> List<T>.longAt(index: Int) = (get(index) as Number?)?.toLong()



/**
 * Get element at index and try to convert it to byte array using following strategy:
 *
 * - if it is null, return null
 * - if it is ByteArray, return it
 * - if it is a List<>, try to create a byte array with a copy of its contents
 * - if it is Bytes instance, get a byte array out of it
 *
 * Otherwise, throw exception.
 * @throws ClassCastException if the element at index can't be converted to a ByteArray
 */
fun <T> List<T>.bytesAt(index: Int): ByteArray? =
    get(index)?.let { makeByteArray(it) }

/**
 * Boss-specific conversion (if need) to anything that is in fact arry of bytes
 * to a Kotlin ByteArray.
 */
fun <T> makeByteArray(it: T): ByteArray = when (it) {
    is ByteArray -> it
    is Bytes -> it.toArray()
    is List<*> -> (it as List<Byte>).toByteArray()
    else -> throw ClassCastException("cannot convert to byte array: $it")
}

/**
 * Get element at index and return it if it is a `BossStruct` instance, otherwise convert it to
 * `BossStruct`, in particular:
 *
 * - if it is null, returns null
 * - otherwise, uses the strategy described in [BossStruct.from]
 *
 * @param index index of the element
 * @return BossStruct instance as described above
 * @throws ClassCastException if can't perform necessary conversion
 */
fun <T> List<T>.structAt(index: Int): BossStruct? =
    get(index)?.let { BossStruct.from(it) }

