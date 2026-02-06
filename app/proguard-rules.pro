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

# Keep Apache POI
-dontwarn org.apache.poi.**
-dontwarn org.apache.xmlbeans.**
-dontwarn org.openxmlformats.schemas.**

# Keep OpenCSV
-dontwarn com.opencsv.**
