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

# eventbus
# https://github.com/greenrobot/EventBus/blob/master/HOWTO.md#proguard-configuration
-keepclassmembers class ** {
    public void onEvent*(**);
}

# Only required if you use AsyncExecutor
#-keepclassmembers class * extends de.greenrobot.event.util.ThrowableFailureEvent {
#    <init>(java.lang.Throwable);
#}

# greenDAO
-keepclassmembers class * extends de.greenrobot.dao.AbstractDao {
    public static java.lang.String TABLENAME;
}
-keep class **$Properties

-dontwarn com.sina.weibo.**
-keep class com.sina.weibo.** { *; }
-keepnames class com.sina.weibo.** { *; }

-dontwarn umeng.sdk.**
-keep class umeng.sdk.** { *;}


# for transitions everywhere
-keep class android.transitions.everywhere.** { *; }
-keep class android.transitions.everywhere.**.** { *; }

# support-v7-appcompat
-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }

-keep public class * extends android.support.v4.view.ActionProvider {
    public <init>(android.content.Context);
}

# support-v7-cardview
-keep class android.support.v7.widget.RoundRectDrawable { *; }

# for OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

# for retrofit
#-keep class com.squareup.okhttp.** { *; }
#-keep interface com.squareup.okhttp.** { *; }
#-dontwarn com.squareup.okhttp.**

-dontwarn rx.**
-dontwarn retrofit.**
-dontwarn okio.**
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}

# for steamsupport
-dontwarn java8.**
#-keep class java8.**

# for realm
-keep @io.realm.annotations.RealmModule class *
-dontwarn javax.**
-dontwarn io.realm.**