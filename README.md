# boss-serialization-kt

> Beta-1. Please report any issues and be ready for bugs and minor interfaces changes.

kotlinx.serialization module for [BOSS](https://kb.universablockchain.com/boss_serialization_protocol/307) space-effective typed binary format used in most Universa and [iCodici](https://icodici.com) products and other places. As for now it provides boss-compliant de/serialization of:

- fields of primitive nullable and non-nullable types: Int, Long, Boolean, Float, Double. Note that boss' variable length integers are used to save space, a standard practice for boss. E.g. you should not mind using longs as it will be compressed.

- boss-specific, cached serialization for ByteArray as boss binary data

- boss-specific cached strings

- Maps (using `BossStruct` class instances)

- Lists

- enums are serialized _using ordinals to reduce length_. This goes along with Boss philosophy.

Note that it is based on experimental API of the kotlinx serialization that could be changed, so it is also experimental.

## Usage notes

As for now, you should build the jar and install it into your project. There is a gradle task to build and copy jars into shared space `$projectRoot\..\jarlib\boss-serialization.jar`. From there it could  be included into other projects somewhat like:

~~~
implementation(files("../jarlib/kotyara.jar"))
~~~

Sorry for inconvenience, we'll push it to some maven repository as soon as it gets more tested.

## Deserialization

~~~
val binaryData: ByteArray
val decoded: MyDataClass = binaryData.decodeBoss()
~~~

Also, you can decode from `Boss.Reader` _stream_:

~~~
val r = Boss.Reader(inputStream)
val decoded: MyDataClass = r.deserialize()
~~~

__Important note__. The root object of the data to decode _must be a boss map_. E.g. you can't deserialize primitive types and lists as root objects. These compound types could be contained in fields only, the root object should still be a map. In other ford, you decode root objects only to classes.

If you need to decode boss to a map rather than a class instance, use `BossStruct` for field types and `binaryData.decodeBossStruct`.

### Allowed field types

- simple types: `Int, Long, Float, Double, Boolean`
- enums (ordinals are used)
- `ZonedDateTime` for boss datetime field type
- `ByteArray` for boss binary field type
- `List<T>` for boss arrays, any serializable item type (null included)
- `BossStruct` for boss maps, string keys, any serializable content (null included)
- nullability is supported for all fields

## Important limitations

### Use `BossStruct` as a Map in fields

Note that due to some design limitation of `kotlinx.serialization` library you __must use `BossStruct`__ wherever you need a Map. It is a wrap around a `Map<String,Any?>` that allows library to properly deserialize it.

### Use provided `ZonedDateTimeSerializer`  

Boss format natively supports timestamps, to it is important to use it. This library relies on serialization
descriptor of the mentioned above serializer. Using other serializers will break binary compatibility of boss format which successfully runs on various platforms. 

You can, for example, add the following line

```
@file:UseSerializers(ZonedDateTimeSerializer::class)
```

to the beginning any file that uses `ZonedDateTime` serializable fields.

## License

MIT as for now, should be included in the repository. Since it is early alfa, the license might also be changed, but to some free one.
