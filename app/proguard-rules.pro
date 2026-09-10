# Keep kotlinx.serialization generated serializers
-keepclasseswithmembers class com.randomcity.app.data.model.** { kotlinx.serialization.KSerializer serializer(...); }
-keepclassmembers class com.randomcity.app.data.model.** { *** Companion; }
-keepclasseswithmembers class com.randomcity.app.data.model.** { kotlinx.serialization.KSerializer serializer(); }
-keep,includedescriptorclasses class com.randomcity.app.data.model.** { *; }
-keepattributes *Annotation*, InnerClasses

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
