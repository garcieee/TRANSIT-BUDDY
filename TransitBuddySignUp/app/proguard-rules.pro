# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep MongoDB driver classes
-keep class org.bson.** { *; }
-keep class com.mongodb.** { *; }
-dontwarn org.bson.**
-dontwarn com.mongodb.**
-dontwarn org.slf4j.**
-dontwarn com.sun.jna.**
-dontwarn javax.annotation.**

# Desugaring specific rules
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Record class information
-keep class java.lang.Record { *; }
-keep @java.lang.Record class * { *; }
-keepclassmembers @java.lang.Record class * { *; }

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile