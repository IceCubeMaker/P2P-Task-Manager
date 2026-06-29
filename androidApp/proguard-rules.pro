# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.p2ptaskmanager.**$$serializer { *; }
-keepclassmembers class com.p2ptaskmanager.** {
    *** Companion;
}
-keepclasseswithmembers class com.p2ptaskmanager.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# SQLDelight
-keep class com.p2ptaskmanager.db.** { *; }

# Koin
-keep class org.koin.** { *; }

# Paho MQTT
-keep class org.eclipse.paho.** { *; }

# Nearby Connections
-keep class com.google.android.gms.nearby.** { *; }

# Keep data classes
-keep class com.p2ptaskmanager.data.model.** { *; }
-keep class com.p2ptaskmanager.data.sync.** { *; }

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
