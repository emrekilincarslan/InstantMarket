# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/dimitar/.bin/android-sdk/tools/proguard/proguard-android.txt
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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-ignorewarnings
#-optimizationpasses 5

#To repackage classes on a single package
#-repackageclasses ''

# for gson (GSON @Expose annotation)
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.examples.android.model.** { *; }


#To remove debug logs:
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** w(...);
    public static *** i(...);
    public static *** v(...);
    public static *** e(...);
}

-keep class com.shinobicontrols.* { *; }

# Butterknife
-keep class butterknife.*
-keepclasseswithmembernames class * { @butterknife.* <methods>; }
-keepclasseswithmembernames class * { @butterknife.* <fields>; }

# RetroLambda
-dontwarn java.lang.invoke.*

# Retrofit 2.X
-dontwarn javax.annotation.**
-dontwarn retrofit2.Platform$Java8

# OkHttp
-keepattributes Signature
-dontwarn com.squareup.okhttp.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault

-dontwarn android.arch.util.paging.CountedDataSource
-dontwarn android.arch.persistence.room.paging.LimitOffsetDataSource

# Support appcompat
-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }
-keep public class * extends android.support.v4.view.ActionProvider {
    public <init>(android.content.Context);
}
-keep class yourpakganame.itemdecorator
-keep public class * extends android.support.v7.widget.RecyclerView.ItemDecoration
-keep class android.support.v7.widget.RecyclerView

-dontwarn com.google.**
-dontwarn **CompatHoneycomb
-dontwarn android.support.v4.**
-dontwarn android.support.v7.**
-keep class android.support.v4.** { *; }

# Support design
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }
-keep public class android.support.v4.view.ViewPager

# Joda Time
-dontwarn org.joda.convert.FromString
-dontwarn org.joda.convert.ToString
-keep public class org.joda.** { *; }

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepnames class * implements java.io.Serializable {
    public static final ** CREATOR;
}

#Maintain enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

