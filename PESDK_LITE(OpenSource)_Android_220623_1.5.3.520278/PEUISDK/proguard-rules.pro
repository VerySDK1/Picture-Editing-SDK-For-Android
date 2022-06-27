-dontoptimize
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-optimizationpasses 5
-dontusemixedcaseclassnames
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod
-dontpreverify
-verbose

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

-keepclasseswithmembers class * {
    public <init>(android.content.Context,android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context,android.util.AttributeSet,int);
}

-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

-keep class * extends android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class * extends android.os.Parcelable$Creator {
	public <methods>;
}

# Keep names - Native method names. Keep all native class/method names.
-keepclasseswithmembers class * {
    native <methods>;
}

# Also keep - Enumerations. Keep the special static methods that are required in
# enumeration classes.
-keepclassmembers enum  * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}




#keep annotation Keep
-dontwarn androidx.annotation.Keep
-keep @androidx.annotation.Keep class ** {*;}
-keep @androidx.annotation.Keep class *$* {*;}
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

#AndroidX混淆开始
-keep class com.google.android.material.** {*;}
-keep class androidx.** {*;}
-keep public class * extends androidx.**
-keep interface androidx.** {*;}
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**
-dontwarn androidx.**
#AndroidX混淆结束

-keep public class com.pesdk.uisdk.R** {*;}
-dontwarn  com.pesdk.uisdk.R**
-keep class com.pesdk.api.** {
    public <fields>;
    public <methods>;
}
-keep class com.pesdk.uisdk.widget.** {
   public <fields>;
   public <methods>;
}

-keep class com.pesdk.uisdk.bean.** {
 *;
}



-dontwarn  com.vecore.**
-keep class com.vecore.** { *; }


-dontwarn  org.apache.commons.codec.**
-keep class org.apache.commons.codec.** { *; }

# apng
-dontwarn  org.apache.commons.**
-keep class org.apache.commons.** { *; }
-dontwarn  ar.com.hjg.pngj.**
-keep class ar.com.hjg.pngj.** { *; }
# ------end apng



##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { <fields>; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

##---------------End: proguard configuration for Gson  ----------




#kotlin
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

-keepclasseswithmembernames class * {
    native <methods>;
}




#---google 人脸----
-dontwarn  com.google.mlkit.**
-keep class com.google.mlkit.** { *; }
#---google 人脸 end----

#---mnnkit 人脸----
-dontwarn com.alibaba.android.mnnkit.**
-keep class com.alibaba.android.mnnkit.**{*;}
#---mnnkit 人脸----

#lambda
-dontwarn java.lang.invoke.**
