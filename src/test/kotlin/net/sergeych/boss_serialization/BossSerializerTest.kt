@file:UseSerializers(ZonedDateTimeSerializer::class)
package net.sergeych.boss_serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.sergeych.boss.Boss
import net.sergeych.utils.Bytes
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.test.*

enum class FoobarEnum{ FOO, BAR, BUZZ }

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

@ExperimentalSerializationApi
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

    @Test fun encodeSimpleClass() {
        @Serializable
        data class FooBar(val foo: String,val bar: Int,val time: ZonedDateTime)
        val d1 = FooBar("bar", 42, ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS))
        val data = BossEncoder.encode(d1)
        println(data.dump())
        val d2 = data.decodeBoss<FooBar>()
        assertEquals(d1, d2)
    }

    @Test fun encodeNullableSimpleClass() {
        @Serializable
        data class FooBar(val foo: String,val bar: Int,val time: ZonedDateTime?)
        val d1 = FooBar("bar", 42, null)
        val data = BossEncoder.encode(d1)
        println(data.dump())
        val d2 = data.decodeBoss<FooBar>()
        assertEquals(d1, d2)
    }

    @Test fun encodeLists() {
        @Serializable
        data class FooBar(val foos: List<String>)
        @Serializable
        data class Buzz(val foobars: List<FooBar>)

        val fb1 = FooBar(listOf("foo","bar"))
        val data = BossEncoder.encode(fb1)
        println(data.decodeBossStruct())
        assertIs<List<String>>(data.decodeBossStruct()["foos"])
        val fb2: FooBar = data.decodeBoss()
        assertEquals(fb1, fb2)
        val fb3 = FooBar(listOf("bar","buzz"))
        val b1 = Buzz(listOf(fb3, fb1))
        val data2 = BossEncoder.encode(b1)
        println(data2.dump())
        println(data2.decodeBossStruct())
        val b2 = data2.decodeBoss<Buzz>()
        assertEquals(b1, b2)
    }

    @Test fun decodeEnums() {
        @Serializable
        data class Foobar(val foo: FoobarEnum)
        val fb1 = Foobar(FoobarEnum.BAR)
        val data = BossEncoder.encode(fb1)
        println(data.dump())
        println(data.decodeBossStruct())
        assertEquals(1, data.decodeBossStruct().getAs<Int>("foo"))

        val fb2 = data.decodeBoss<Foobar>()
        assertEquals(fb1, fb2)
    }

    @Test fun deserializeUnit() {
        val x = BossDecoder.decodeFrom<Unit>(BossStruct.EMPTY)
        assertIs<Unit>(x)
    }

}