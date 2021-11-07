@file:UseSerializers(ZonedDateTimeSerializer::class)
package net.sergeych.boss_serialization

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.sergeych.boss.Boss
import net.sergeych.utils.Bytes
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.test.*

@Suppress("ArrayInDataClass")
@Serializable
data class SimpleData(
//    val intValue: Int,
    val foo: String,
    val bar: String,
    val miss: String?,
    val inner: InnerData,
    val strarr: List<String>,
    val objarr: List<InnerData>,
    val intValue: Int,
    val longValue: Long,
    val moment: ZonedDateTime,
    val binaryData: ByteArray,
    val booleanValue: Boolean,
    val doubleValue: Double,
    val floatValue: Float,
//    val longValue: Long,
//    val doubleValue: Double,
//    val booleanValue: Boolean
)

@Suppress("ArrayInDataClass")
@Serializable
data class SimpleDataWithStruct(
//    val intValue: Int,
    val foo: String,
    val bar: String,
    val miss: String?,
    val inner: BossStruct,
    val strarr: List<String>,
    val objarr: List<InnerData>,
    val intValue: Int,
    val longValue: Long,
    val moment: ZonedDateTime,
    val binaryData: ByteArray,
    val booleanValue: Boolean,
    val doubleValue: Double,
    val floatValue: Float,
//    val longValue: Long,
//    val doubleValue: Double,
//    val booleanValue: Boolean
)


@Serializable
data class InnerData(val str: String)

internal class BossSerializerTest {

    val someDate = ZonedDateTime.now().minusHours(1).truncatedTo(ChronoUnit.SECONDS)

    fun testSimpleBoss(): ByteArray =
        Boss.dumpToArray(
            mapOf(
                "foo" to "string_foo",
                "bar" to "string_bar",
                "miss" to null,
                "inner" to mapOf("str" to "string value"),
                "strarr" to listOf("foo", "bar", "buzz"),
                "objarr" to listOf(mapOf("str" to "strarr1"), mapOf("str" to "strarr2")),
                "intValue" to 42,
                "longValue" to 42_000_000_042L,
                "moment" to someDate,
                "binaryData" to byteArrayOf(1,2,3,4,5),
                "booleanValue" to true,
                "doubleValue" to 3.1415926,
                "floatValue" to 3.14f
            )
        )

    @Test
    fun testBasicSerialization() {
        val data = testSimpleBoss()
        Bytes.dump(data)
        val r = testSimpleBoss().decodeBoss<SimpleData>()
        println(r)
        assertEquals(42, r.intValue)
        assertEquals(42_000_000_042, r.longValue)
        assertEquals("string_foo", r.foo)
        assertEquals("string_bar", r.bar)
        assertNull(r.miss)
        assertContentEquals(listOf("foo", "bar", "buzz"), r.strarr)
        assertContentEquals(listOf(InnerData("strarr1"), InnerData("strarr2")), r.objarr)
        assertEquals(someDate, r.moment)
        assertContentEquals(byteArrayOf(1,2,3,4,5), r.binaryData)
        assertTrue(r.booleanValue)
        assertEquals(3.1415926,r.doubleValue)
        assertEquals(3.14f,r.floatValue)
    }

    @Test fun testArrayOfExtendedTypes() {
        @Serializable
        data class Dd(val dates: List<ZonedDateTime>)
        val d1 = someDate
        val d2 = someDate.plusMinutes(7)
        val data = Boss.dumpToArray(mapOf("dates" to listOf(d1, d2)))
        println(data.dump())
        val r: Dd = data.decodeBoss()
        assertEquals(d1, r.dates[0])
        assertEquals(d2, r.dates[1])
    }

    @Test fun testDeserializeMap() {
        val data = testSimpleBoss()
//        println(data.dump())
        val r = data.decodeBoss<SimpleDataWithStruct>()
        println(r.inner)
        assertEquals("string value", r.inner.getAs("str"))
        val s = data.decodeBossStruct()
        println(s)
    }

//    @Test fun testDecodeArray() {
//        val data = Boss.dumpToArray(arrayOf(5,4,3))
//        println(data.dump())
//        val r = data.fromBoss<List<Int>>()
//    }

}