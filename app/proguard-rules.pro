# Preserve JNI/SDL engine entry points used by embedded native engines.
-keep class org.tvp.kirikiri2.** { *; }
-keep class com.yuri.onscripter.** { *; }
-keep class org.libsdl.app.** { *; }
-keep class org.cocos2dx.lib.** { *; }
-keep class bridge.NativeBridge { *; }
-keep class T3.** { *; }
-keep class com.akira.tyranoemu.remote.** { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}

# KamiGAL 主应用类
-keep class com.sakurajima.galsearch.** { *; }

# Gson / JSON 相关
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keep class * extends java.util.List { *; }
-keep class * extends java.util.Map { *; }

# 保留 tRPC/网络请求相关
-keep class org.json.** { *; }
-dontwarn org.json.**
-dontnote org.json.**

# 保留 Android 系统组件
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.app.Application

# 保留所有 public 方法（防止反射调用问题）
-keepclassmembers class * {
    public *;
}

# 保留行号信息方便查崩溃
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile