# Room ProGuard rules
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}

# Media3 ProGuard rules
-keep class androidx.media3.session.** { *; }
-keep class androidx.media3.exoplayer.** { *; }

# Coil ProGuard rules
-keep class coil.** { *; }

# Kotlinx Serialization
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit & OkHttp
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature, Exceptions
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**

# Dagger Hilt & ViewModel
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}


