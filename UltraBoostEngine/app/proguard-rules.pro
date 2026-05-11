# Add project specific ProGuard rules here.
# UltraBoost Engine ProGuard Rules

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep JNI classes
-keep class com.ultraboost.engine.jni.** { *; }

# Keep optimization engine
-keep class com.ultraboost.engine.optimization.** { *; }

# Keep monitoring classes
-keep class com.ultraboost.engine.monitoring.** { *; }

# Keep service classes
-keep class com.ultraboost.engine.service.** { *; }

# Keep model classes for Room
-keepclassmembers class com.ultraboost.engine.data.model.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }
