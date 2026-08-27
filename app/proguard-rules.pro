# ProGuard rules for Room & Serialization
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}
-keep class * implements kotlinx.serialization.KSerializer {
    <init>(...);
}
