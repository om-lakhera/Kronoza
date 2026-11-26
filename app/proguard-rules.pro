# Kronoza ProGuard / R8 Rules
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Preserve Kronoza model enums and Room entities / DAOs
-keepclassmembers class * extends java.lang.Enum {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class com.omlakhera.kronoza.model.** { *; }
-keep class com.omlakhera.kronoza.data.local.** { *; }
-keep class com.omlakhera.kronoza.data.backup.** { *; }

# Preserve AndroidManifest BroadcastReceivers & WorkManager Workers
-keep class com.omlakhera.kronoza.reminders.** { *; }
-keepclassmembers class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Room & SQLite
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Strip verbose debug logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
