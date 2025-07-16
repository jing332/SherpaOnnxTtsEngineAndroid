# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

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

# Keep all sherpa-onnx classes and their methods
-keep class com.k2fsa.sherpa.onnx.** { *; }

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep classes that are accessed from JNI
-keep class com.k2fsa.sherpa.onnx.OfflineTts {
    *;
}

# Keep callback-related methods
-keep class * {
    *** generateWithCallbackImpl(...);
}

# Keep Kotlin function types used in JNI
-keep class kotlin.jvm.functions.Function1 { *; }

# Ignore missing optional dependencies for Net library (com.github.liangjingkanji:Net)
-dontwarn com.drake.brv.PageRefreshLayout
-dontwarn com.drake.statelayout.StateLayout

# Ignore missing optional dependencies for Apache Commons Compress
-dontwarn com.github.luben.zstd.ZstdInputStream
-dontwarn com.github.luben.zstd.ZstdOutputStream
-dontwarn org.brotli.dec.BrotliInputStream
-dontwarn org.objectweb.asm.AnnotationVisitor
-dontwarn org.objectweb.asm.Attribute
-dontwarn org.objectweb.asm.ClassReader
-dontwarn org.objectweb.asm.ClassVisitor
-dontwarn org.objectweb.asm.FieldVisitor
-dontwarn org.objectweb.asm.MethodVisitor

# Keep classes that might be accessed reflectively
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Keep serialization classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}


#-------------- 去掉所有打印 -------------

-assumenosideeffects class android.util.Log {
public static *** d(...);

# public static *** e(...);

public static *** i(...);

public static *** v(...);

public static *** println(...);

public static *** w(...);

public static *** wtf(...);

}

-assumenosideeffects class android.util.Log {
public static *** d(...);

public static *** v(...);

}

-assumenosideeffects class android.util.Log {
# public static *** e(...);

public static *** v(...);

}

-assumenosideeffects class android.util.Log {
public static *** i(...);

public static *** v(...);

}

-assumenosideeffects class android.util.Log {
public static *** w(...);

public static *** v(...);

}

-assumenosideeffects class java.io.PrintStream {
public *** println(...);

public *** print(...);

}

# Keep `Companion` object fields of serializable classes.
# This avoids serializer lookup through `getDeclaredClasses` as done for named companion objects.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Keep `serializer()` on companion objects (both default and named) of serializable classes.
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep `INSTANCE.serializer()` of serializable objects.
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# @Serializable and @Polymorphic are used at runtime for polymorphic serialization.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault