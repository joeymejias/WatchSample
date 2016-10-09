# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Code\Android\sdk/tools/proguard/proguard-android.txt
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

##REMOVE
#-dontobfuscate
#-dontpreverify
#-repackageclasses ''
#-allowaccessmodification
#-optimizations !code/simplification/arithmetic
-keepattributes *Annotation*,EnclosingMethod,Signature

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.content.Context {
   public void *(android.view.View);
   public void *(android.view.MenuItem);
}

-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}
#
#-keepclassmembers class * {
#    @android.webkit.JavascriptInterface <methods>;
#}
##END REMOVE
##NEW
##-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
##-optimizationpasses 5
##-allowaccessmodification
##-dontpreverify
##-keepattributes *Annotation*,EnclosingMethod,Signature
##
### The remainder of this file is identical to the non-optimized version
### of the Proguard configuration file (except that the other file has
### flags to turn off optimization).
##
##-dontusemixedcaseclassnames
##-dontskipnonpubliclibraryclasses
##-verbose
##
##-keepattributes *Annotation*
##-keep public class com.google.vending.licensing.ILicensingService
##-keep public class com.android.vending.licensing.ILicensingService
##
### For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
##-keepclasseswithmembernames class * {
##    native <methods>;
##}
##
### keep setters in Views so that animations can still work.
### see http://proguard.sourceforge.net/manual/examples.html#beans
##-keepclassmembers public class * extends android.view.View {
##   void set*(***);
##   *** get*();
##}
##
### We want to keep methods in Activity that could be used in the XML attribute onClick
##-keepclassmembers class * extends android.app.Activity {
##   public void *(android.view.View);
##}
##
### For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
##-keepclassmembers enum * {
##    public static **[] values();
##    public static ** valueOf(java.lang.String);
##}
##
##-keepclassmembers class * implements android.os.Parcelable {
##  public static final android.os.Parcelable$Creator CREATOR;
##}
##
##-keepclassmembers class **.R$* {
##    public static <fields>;
##}
##
### The support library contains references to newer platform versions.
### Don't warn about those in case this app is linking against an older
### platform version.  We know about them, and they are safe.
##-dontwarn android.support.**
##END NEW
#-dontwarn sun.misc.**
#
#-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
#   long producerIndex;
#   long consumerIndex;
#}
#
#-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
#    rx.internal.util.atomic.LinkedQueueNode producerNode;
#}
#
#-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
#    rx.internal.util.atomic.LinkedQueueNode consumerNode;
#}
#
-keep public class io.affect.sensemojisdk.*{ *; }
-keep public class io.affect.sensemojisdk.api.* { *; }
#-keep public class io.affect.sensemojisdk.R** { *; }
#-keepclassmembers class io.affect.** { *; }
#
#

-keep class a** { *; }
#-keep class b** { *; }
-keep class com.affectiva** { *; }
#-keep class d** { *; }
#-keep class e** { *; }
#-keep class f** { *; }
#-keep class g** { *; }
#-keep class h** { *; }
#-keep class io.affect.sensemojisdk.view** { *; }
#-keep class io.affect.sensemojisdk.model** { *; }
#-keep class j** { *; }
#-keep class k** { *; }
#-keep class l** { *; }
#-keep class m** { *; }
#-keep class n** { *; }
#-keep class o** { *; }
#-keep class p** { *; }
#-keep class q** { *; }
#-keep class r** { *; }
#-keep class s** { *; }
#-keep class t** { *; }
#-keep class u** { *; }
#-keep class v** { *; }
#-keep class w** { *; }
#-keep class x** { *; }
#-keep class y** { *; }
#-keep class z** { *; }


#-keep public class com.affectiva.** { *; }

-keepclassmembers,allowobfuscation class * {
    @javax.inject.* *;
    @dagger.* *;
    <init>();
}
-keep class javax.inject.* { *; }
-keep class **$$ModuleAdapter
-keep class **$$InjectAdapter
-keep class **$$StaticInjection
-keep class dagger.** { *; }
-keep class * extends dagger.**
#
-keep class com.flurry.** { *; }
-dontwarn com.flurry.**

# Google Play Services library
-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *

-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-keepclassmembers class com.mopub.** { public *; }
-keep public class com.mopub.**

#If you are using the Google Mobile Ads SDK, add the following:
# Preserve GMS ads classes
-keep class com.google.android.gms.** { *;
}
-dontwarn com.google.android.gms.**


#If you are using the InMobi SDK, add the following:
# Preserve InMobi Ads classes
-keep class com.inmobi.** { *;
}
-dontwarn com.inmobi.**
#If you are using the Millennial Media SDK, add the following:
# Preserve Millennial Ads classes
-keep class com.millennialmedia.** { *;}
-dontwarn com.millennialmedia.**

-keep class android.** { *; }
-keep class android.support.** { *; }
-keep interface android.** { *; }
-keep class * extends android.** { *; }