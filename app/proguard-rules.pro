# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /opt/android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# retrolambda
-dontwarn java.lang.invoke.*

##---------------Start: proguard configuration for Guava  ----------
#-injars path/to/myapplication.jar
#-injars lib/guava-r07.jar
#-libraryjars lib/jsr305.jar
#-outjars myapplication-dist.jar

#-dontoptimize
#-dontobfuscate
#-dontwarn sun.misc.Unsafe
#-dontwarn com.google.common.collect.MinMaxPriorityQueue
#
#-keepclasseswithmembers public class * {
#    public static void main(java.lang.String[]);
#}
# from https://github.com/krschultz/android-proguard-snippets/blob/master/libraries/proguard-guava.pro
-keep class com.google.common.io.Resources {
    public static <methods>;
}
-keep class com.google.common.collect.Lists {
    public static ** reverse(**);
}
-keep class com.google.common.base.Charsets {
    public static <fields>;
}

-keep class com.google.common.base.Joiner {
    public static Joiner on(String);
    public ** join(...);
}

-keep class com.google.common.collect.MapMakerInternalMap$ReferenceEntry
-keep class com.google.common.cache.LocalCache$ReferenceEntry

-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe
# http://stackoverflow.com/questions/9120338/proguard-configuration-for-guava-with-obfuscation-and-optimization
##---------------End: proguard configuration for Guava  ----------

# picasso
-dontwarn com.squareup.picasso.*

# from gson http://google-gson.googlecode.com/svn/trunk/examples/android-proguard-example/proguard.cfg

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
#-keep class co.yishun.onemoment.app.net.result.** { *; }
#-keep class co.yishun.onemoment.app.net.request.sync.** { *; }

##---------------End: proguard configuration for Gson  ----------