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

# --- REGLAS DE SEGURIDAD PARA HILT Y VIEWMODELS ---
# Evitar que R8 elimine o renombre los constructores de ViewModels y Repositorios
# Esto es vital para que la Inyección de Dependencias funcione en Release.
-keep class com.adrencina.enchu.ui.**ViewModel { *; }
-keep class com.adrencina.enchu.data.repository.** { *; }
-keep class com.adrencina.enchu.di.** { *; }
-keep class com.adrencina.enchu.domain.use_case.** { *; }

# --- REGLAS PARA FIREBASE APP CHECK Y PLAY INTEGRITY ---
-keep class com.google.firebase.appcheck.** { *; }
-keep class com.google.android.play.core.integrity.** { *; }

# --- REGLAS PARA SQLCIPHER Y ROOM (CRÍTICO PARA CRASHES) ---
# SQLCipher usa clases nativas y reflexión. Si se ofuscan, la DB no abre y crashea.
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
# Room necesita mantener sus clases para mapear entidades y DAOs
-keep class androidx.room.** { *; }
-keep class androidx.sqlite.db.** { *; }
# Evitar advertencias que pueden romper el build
-dontwarn net.sqlcipher.**
-dontwarn androidx.room.**

# --- ELIMINACIÓN DE LOGS EN PRODUCCIÓN (BLINDAJE DE INFORMACIÓN) ---
# Esta regla elimina físicamente las llamadas a Log de depuración del código compilado en Release.
# Mantenemos W (Warning) y E (Error) para que Crashlytics pueda capturar contextos de error.
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}