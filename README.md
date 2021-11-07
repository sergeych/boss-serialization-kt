# boss-serialization-kt

> Alpha, not yet fully functional. As for now, deserialization module seems to work.

kotlinx.serialization module for [BOSS](https://kb.universablockchain.com/boss_serialization_protocol/307) space-effective typed binary format used in most Universa and [iCodici](https://icodici.com) products and other places. 

Note that it is based on experimental API of the kotlinx serialization that could be changed, so it is expermiental.

## Usage notes

As for now, you should build linrary and install it into your project.

## Deserialization

~~~
val binaryData: ByteArray
val decoded: MyDataClass = binaryData.decodeBoss()
~~~

Also you can decode from `Boss.Reader` _stream_:

~~~
val r = Boss.Reader(inputStream)
val decoded: MyDataClass = r.deserialize()
~~~

__Important note__. The root object of the data to decode _must be a boss map_. E.g. you can't deserialize primitive types and lists as root objects. These compond types could be contained in fields onlym while the root object should still be a map. In other ford, you decode only to classes.

If you need to decode boss to a map rather than a class instance, use `BossStruct` for field types and `binaryData.decodeBossStruct`.

### Aallowed fields

- simple types: `Int, Long, Float, Double, Boolean, null`
- `ZonedDateTime` for boss datetime field type
- `ByteArray` for boss binary field type
- `List<T>` for boss arrays, any serializable item type (null included)
- `BossStruct` for boss maps, string keys, any serializable content (null included)

Note that due to some design limitation of kotlinx.serialization library you __must use `BossStruct`__ whereever you need a Map. It is a wrap around a `Map<String,Any?>` that allows library to properly deserialize it.

## License

MIT as for now, should be included in the repository. Since it is early alfa, the license might also be changed, but to some free one.
