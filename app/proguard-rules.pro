# Add project specific ProGuard rules here.

# Keep data classes
-keep class com.forestry.counter.data.local.entity.** { *; }
-keep class com.forestry.counter.domain.model.** { *; }

# Keep Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.forestry.counter.**$$serializer { *; }
-keepclassmembers class com.forestry.counter.** {
    *** Companion;
}
-keepclasseswithmembers class com.forestry.counter.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Apache POI (only XSSF/HSSF we actually use)
-dontwarn org.apache.poi.**
-dontwarn org.apache.xmlbeans.**
-dontwarn org.openxmlformats.schemas.**
-dontwarn org.apache.commons.**
-dontwarn org.apache.batik.**
-dontwarn javax.xml.**
-dontwarn org.w3c.**

# Keep OpenCSV
-dontwarn com.opencsv.**

# exp4j (formula parser)
-dontwarn net.objecthunter.exp4j.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Keep MapLibre / Mapbox
-keep class com.mapbox.mapboxsdk.** { *; }
-keep class com.mapbox.geojson.** { *; }
-keep class com.mapbox.turf.** { *; }
-dontwarn com.mapbox.**
-keep class org.maplibre.** { *; }
-dontwarn org.maplibre.**

# Strip debug logging in release
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}
