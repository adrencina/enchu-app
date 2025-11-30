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

# -----------------------------------------------------------------------------------
# Reglas para los Modelos de Datos de Firebase
# Mantiene todas las clases en el paquete data.model y todos sus miembros (campos y métodos).
# Esto evita que R8/ProGuard los renombre o elimine.
# Es CRÍTICO para Firebase, que usa los nombres de estas clases y campos para mapear los datos.
# Si fueran renombrados, Firebase no los encontraría y la app fallaría al leer/escribir datos.
-keep class com.adrencina.enchu.data.model.** { *; }

# Esta regla es una seguridad adicional. Asegura que los nombres de las clases y sus
# miembros no sean ofuscados, incluso si R8 pensara que puede hacerlo.
-keepnames class com.adrencina.enchu.data.model.** { *; }

# Reglas explicitas para Avance y Tarea para evitar problemas de serializacion
-keep class com.adrencina.enchu.data.model.Avance { *; }
-keep class com.adrencina.enchu.data.model.Tarea { *; }
# -----------------------------------------------------------------------------------

